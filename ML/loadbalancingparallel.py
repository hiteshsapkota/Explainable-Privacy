#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Feb 17 13:42:51 2018

@author: hiteshsapkota
"""
########Name:- Hitesh Sapkota
########Email:- hxs1943@g.rit.edu

import os.path
import json
import numpy as np
from utils import *
import sys
import pandas as pd
from math import *
import pickle as cPickle
from ALS import getUserMatrix
from weighted_decision_tree import weighted_tree, getRules
import multiprocessing
from multiprocessing import Pool
import time
import matplotlib.pyplot as plt
###Initialization

radius=4
r=0.4
maxiter=1000
y_max=1
y_min=-2.5
x_max=1
x_min=-1
data=np.zeros((400,400))
###Input

"""This gives current directory"""

with open("base_dir.json") as outfile:
    base_path_file = json.load(outfile)

base_path = base_path_file['base_dir']


[attr_id_to_name, _]=load_attributes()
feature_names = [k for k, v in attr_id_to_name.items()]
no_process =  multiprocessing.cpu_count()
pol=Pool(no_process)

def getImageAttributes(image_id):
    
    """image id will be in the form of annotations/image_type/id.json"""
    
    with open(base_path+"Data/Collected/"+image_id) as jf:
         anno=json.load(jf)
         labels = anno['labels']
         
    return labels


def getfeatureStatus(image_id):
    present_attributes = getImageAttributes(image_id)
    featureStatus=[]
    
    for feature in feature_names:
        
        if feature in present_attributes:
            featureStatus.append(1)
            
        else:
            
            featureStatus.append(0)
            
    return featureStatus


def square_rooted(x):
    return round(sqrt(sum([a*a for a in x])),10)


def cosineSimilarity(x, y):
    x_filter = []
    y_filter= []
    
    for i in range(0, len(x)):
        
        if x[i]==0 or y[i]==0:
            continue
        
        x_filter.append(x[i])
        y_filter.append(y[i])
        
    if len(x_filter)==0 or len(y_filter)==0:
        return 0
    
    x = np.asarray(x_filter)
    y = np.asarray(y_filter)
    numerator = sum(a*b for a,b in zip(x,y))
    denominator = square_rooted(x)*square_rooted(y)
 
    return round(numerator/float(denominator),3)

def getFeedbackMatrix(n_users, len_features, selected_users):
    feedback_vecs=np.zeros((n_users, len(feature_names)))
    
    with open(base_path+"Data/Generated/feedback.json") as outfile:
        feedback_file = json.load(outfile)
        print(len(feedback_file))
        print(n_users)
        j=0
        for i, user in enumerate(feedback_file):
            if user not in selected_users:
                continue
            feedback_vecs[j, :]=np.asarray(feedback_file[user])
            j=j+1
            
    return feedback_vecs
        

def getSimilarityGD(users):
    user_record={}
    images=[]
    
    for user_name in users:
        user_record[user_name]={}
        user_data = getUserInstances(user_name)
        input_data = user_data[feature_names]
        decision_data = user_data['Decision']
        image_ids = user_data['ImageId']
        
        for i in range(0, len(decision_data)):
            if image_ids[i] not in images:
                images.append(image_ids[i])
            user_record[user_name][image_ids[i]]=[]
            user_record[user_name][image_ids[i]].append(input_data.iloc[i, :])
            user_record[user_name][image_ids[i]].append(decision_data[i])
            
    n_users = len(users)
    n_items = len(images)
    ratings = np.zeros((n_users, n_items))
    item_vecs = np.random.random((n_items, len(feature_names)))
    
    for user_name, user_properties in user_record.items():
        i=users.index(user_name)
        
        for image_id, image_record in user_properties.items():
            j = images.index(image_id)
            decision = image_record[1]
            
            if decision==1:
                ratings[i][j]=1
                
            elif decision==0:
                ratings[i][j]=2
                
            else:
                print("Serious problem")
            item_vecs[j, :]= image_record[0]
    #Each value between 1-5, 1 most critical and 5 least critical
    feedback_vecs = getFeedbackMatrix(n_users, len(feature_names), users)
    user_charac_matrix = getUserMatrix(ratings, item_vecs, feedback_vecs)
    
    return user_charac_matrix
 
           
def storeAttribute(user_matrix, users):
    
    users_attributes={}
    #1 as most critical (sharing), 0 as least critical (not sharing)
    scale_user_matrix=(user_matrix-np.amin(user_matrix, axis=1)[:, np.newaxis])/((np.amax(user_matrix, axis=1)-np.amin(user_matrix, axis=1))[:, np.newaxis])
    scale_user_matrix=1-scale_user_matrix
    
    for i in range(0, len(users)):
        user_score = scale_user_matrix[i].tolist()
        users_attributes[users[i]]=user_score
    
        
    with open(base_path+"Data/Generated/user_attributes.json", "w") as infile:
        json.dump(users_attributes, infile)
        
        
def updateUserSimilaritySGD():
    
    with open(base_path+"Data/Generated/user_similarity_sgd.json") as outfile:
        similarity_file = json.load(outfile)
        
    users = [k for k,v in similarity_file.items()]
    selected_users = getTrainedUsers(users)
    print("Updating similarity based on Collborative filtering")
    user_matrix = getSimilarityGD(selected_users)
    storeAttribute(user_matrix, selected_users)
    
    with open(base_path+"Data/Generated/user_similarity_sgd.json", "r+") as outfile:
        user_similarity = json.load(outfile)
        
        for i in range(0, len(selected_users)):
            user_score = user_matrix[i].tolist()
            
            for j in range(i+1, len(selected_users)):
                others_score = user_matrix[j].tolist()
                cosine_sim=cosineSimilarity(user_score, others_score)
                user_similarity[selected_users[i]][selected_users[j]]=(cosine_sim+1)/2
                user_similarity[selected_users[j]][selected_users[i]]=(cosine_sim+1)/2
        outfile.seek(0)
        json.dump(user_similarity, outfile)
        outfile.truncate()
                
            
def initialization(user_name):
    
    """Updating User Similarity File Based on the Collaborative Filtering"""
   
    if os.path.isfile(base_path+"Data/Generated/user_similarity_sgd.json"):
        
        with open(base_path+"Data/Generated/user_similarity_sgd.json", "r+") as rwfile:
            similarity_file = json.load(rwfile)
            existing_users = [k for k,v in similarity_file.items()]
            
            if user_name not in existing_users:
                print("User is added to the similarity file")
                similarity_file[user_name]={}
                
                for k,v in similarity_file.items():
                    if k!=user_name:
                        similarity_file[k][user_name]=0
                        similarity_file[user_name][k]=0
            else:
                print("User is already added to the similarity file")
                
            rwfile.seek(0)
            json.dump(similarity_file, rwfile)
            rwfile.truncate()
    else:
        user_similarity = {}    
        user_similarity[user_name]={}
        with open(base_path+"Data/Generated/user_similarity_sgd.json", "w") as infile:
            json.dump(user_similarity, infile)
                    
    if os.path.isfile(base_path+"Data/Generated/feedback.json"):
        
        with open(base_path+"Data/Generated/feedback.json", "r+") as rwfile:
            feedback_file = json.load(rwfile)
            existing_users = [k for k,v in feedback_file.items()]
            
            if user_name not in existing_users:
                print("User is added to the similarity file")
                feedback_file[user_name]=np.zeros(len(feature_names)).tolist()
                
            else:
                print("User is already added to the similarity file")
                
            rwfile.seek(0)
            json.dump(feedback_file, rwfile)
            rwfile.truncate()
    else:
       feedback_file = {}    
       feedback_file[user_name]=np.zeros(len(feature_names)).tolist()
       with open(base_path+"Data/Generated/feedback.json", "w") as infile:
            json.dump(feedback_file, infile)
    
    print("Initialization completed!!!")

    
def getPrefScore(pref_share, num_points, confidence, input_features, input_decision):
    conf1_index= np.where(confidence ==1)[0]
    unmuted_pref_values = pref_share[conf1_index]
    I_FY = np.zeros(input_features.size)
    idx = [i for i,val in enumerate(input_features) if val==1]
    I_FY[idx] = 1
    
    if input_decision==1:
        pref_share=pref_share+(1/(num_points+1))*(I_FY-I_FY*pref_share)
        num_points[idx]+=1
    
    else:
        pref_share=pref_share+(1/(num_points+1))*(-I_FY*pref_share)
        num_points[idx]+=1
    pref_share[conf1_index]= unmuted_pref_values
    
    return [pref_share, num_points]


def storetree(args):
    users, feedback_factor, user_similarity, i = args
    train_data = getUserInstances(users[i])
    feedback_data = getUserInstances(users[i], 'feedback')
    frame = [train_data[feature_names], feedback_data[feature_names]]
    X = pd.concat(frame)
    Y = pd.concat([train_data['Decision'], feedback_data['Decision']])
    W = np.ones(len(Y))
    W[len(train_data['Decision']):] = feedback_factor
    for j in range(0, len(users)):
        if i==j:
            continue
        user2_train_data = getUserInstances(users[j])
        user2_feedback_data = getUserInstances(users[j], 'feedback')
        input_data = pd.concat([user2_train_data[feature_names], user2_feedback_data[feature_names]])
        decision_data = pd.concat([user2_train_data['Decision'], user2_feedback_data['Decision']])
        X_frame = [X, input_data]
        X = pd.concat(X_frame)
        Y_frame = [Y, decision_data]
        Y = pd.concat(Y_frame)
        simi_coeff = user_similarity[users[i]][users[j]]
        W2 = np.repeat(simi_coeff, len(decision_data))
        W = np.concatenate((W, W2))
    W = W/sum(W)
    Y = Y.astype(W)
    model = weighted_tree(X, Y, W)
    with open(base_path+'Data/Generated/trees/'+users[i]+'.pkl', 'wb') as fid:
            cPickle.dump(model, fid)
        
def cal(args):
    return 0    

def updateTree(pol, user_similarity, feedback_factor=2):
    
    print("Updating Tree")
    users = [k for k,v in user_similarity.items()]
    args = []
    
    for i in range(0, len(users)):
        
        args.append((users, user_similarity, feedback_factor,  i))
    
    no_tasks = len(args)
    no_iterations = int(np.ceil(no_tasks/no_process))
    
    for k in range (0, no_iterations):
            if (k==(no_iterations-1)):
                sub_args = args[k*no_process:no_tasks]
                
            else:
                sub_args = args[k*no_process:(k+1)*no_process]
            result = pol.map(storetree, sub_args)
                
    print("Finished updating")
          
    
def update(pol):
    
    """Update required fields: attributes, data instances, and similarity 
    score as new data comes in
    Input: New instance consist of dictionary with input features, input_decision
    Replace new instance by image id and open here to construct new instance along with the decision
    """
    print("Updating similarity score between user and other users based on SGD")   
    updateUserSimilaritySGD()
    
    with open(base_path+"Data/Generated/user_similarity_sgd.json") as outfile:
        user_similarity = json.load(outfile)
        
    updateTree(pol, user_similarity)
    print("Update task completed")
    return "Successful update"




##Function that determines the intensity and returns it

def findintensity(args):
    global radius
    global maxiter
    global y_max
    global y_min
    global x_max
    global x_min
    x,y=args
    global r
    x_scal=x_min+(x/400)*(x_max-x_min)
    y_scal=y_min+(y/400)*(y_max-y_min)
    prev_a=0
    prev_b=0  
    for i in range(0,maxiter+1):
        a=prev_a*prev_a-prev_b*prev_b+x_scal
        b=2*prev_a*prev_b+y_scal
        euc_dist=(a*a+b*b)
        
    
        if euc_dist>radius:
            break
        prev_a=a
        prev_b=b
    if i<maxiter:
            intensity=pow((i/maxiter),r)
            
    else:
            intensity=1
            
    result=[]
    result.append(x)
    result.append(y)
    result.append(intensity)
    return result    

def calculation(args):
    return 0

###Parallel Execution
 
    
if __name__=="__main__":
    
    initial_time=time.time()
    pol=Pool(no_process)
    update(pol)
    
    


