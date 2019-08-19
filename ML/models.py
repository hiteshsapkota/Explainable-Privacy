#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jan 30 16:58:59 2019

@author: hiteshsapkota
"""

"""Imports"""
import numpy as np
from math import *
import json
import pickle as cPickle
from utils import load_attributes, load_attribute_name, get_Connection 
from weighted_decision_tree import getRules
import os.path as osp
import sys
import operator



"""This gives current directory"""

with open("base_dir.json") as outfile:
    base_path_file = json.load(outfile)


"""Attribute Features Python"""

base_path = base_path_file['base_dir']
attribute_map_file = load_attribute_name("user_profiles.tsv")
inv_attribute_map_file = {v:k for k,v in attribute_map_file.items()}
[attr_id_to_name, _]=load_attributes()
feature_names = [k for k, v in attr_id_to_name.items()]
test_image_anno_paths = []

with open(base_path+"Data/Collected/test2017.txt") as anno_file:
    for line in anno_file:
        anno_path = osp.join('', line.strip())
        test_image_anno_paths.append(anno_path)
            

with open(base_path+"Data/Generated/cluster_image.json") as outfile:
    cluster_image = json.load(outfile)
cluster_number = [k for k, v in cluster_image.items()]

   
def getfeatureStatus(image_id, cnx):
    cursor=cnx.cursor()
    present_attributes = getImageAttributes(image_id, cursor)
    featureStatus=[]
    
    for feature in feature_names:
        if feature in present_attributes:
            featureStatus.append(1)
        else:
            featureStatus.append(0)
    return featureStatus
            
def attributeDescription(attribute, cursor):

    command = "select description from attribute where name=%s"
    cursor.execute(command, (attribute, ))
    record = cursor.fetchall()
    description = record[0][0]
    
    return description         

def getImageAttributes(image_id, cursor):
    
    """image id will be in the form of annotations/image_type/id.json"""
    command = "select attributes from record where image_id=%s"
    cursor.execute(command, (image_id, ))
    
    record = cursor.fetchall()
    attributes = record[0][0].replace('\r', '').replace('\n', '').split(',')
    labels =[inv_attribute_map_file[k] for k in attributes]
    return labels
  

                
def generateExplanation(attributes, case, cnx):
    cursor = cnx.cursor()
    attributes = [attribute_map_file[attribute] for attribute in attributes]
    
    if case == 'NotShare_OwnSensPresent':
        
        if len(attributes)==1:
            
           explanation = "We recommend that you do not share this picture because: \n"+ attributeDescription(attributes[0], cursor)
       
        else:
            explanation = "We recommend that you do not share this picture because: \n"+ "\n".join([attributeDescription(attribute, cursor) for attribute in attributes])
    
    elif case == 'NotShare_OtherSensPresent':
        
        if len(attributes)==1:
           
            explanation = "We recommend that you do not share this picture because other users (having similar preferences as you) think that \
           : \n"+ attributeDescription(attributes[0], cursor)
        
        else:
            
            explanation = "We recommend that you do not share this picture because other users (having similar preferences as you) think that: \n"+ "\n".join([attributeDescription(attribute, cursor) for attribute in attributes])
    
    elif case == 'NotShare_Insufficient':
        
        explanation = "We recommend that you do not share this image. However, we did not find this picture to have any sensitive attributes. Please proceed \
        with caution."
        
    elif case == 'Share_OwnSensAbsent':
       
        explanation = "We recommend that you share this picture because it does not have any sensitive attributes."
    
    elif case == "Share_OwnSensPresent":
        
        if len(attributes)==1:
            
            explanation = "We recommend that you share this picture. However, we also recognize that: \n"+ attributeDescription(attributes[0], cnx)
            
        else:
            
            explanation = "We recommend that you share this picture. However, we also recognize that: \n"+ "\n".join([attributeDescription(attribute, cursor) for attribute in attributes])
    
        
    elif case == "NotShare_NoTree":
        
        explanation = "We recommend that you do not share this image. However, we do not have sufficient data to produce an explanation."
        
    elif case == "Share_NoTree":
        
        explanation = "We recommend that you share this image. However, we do not have sufficient data to produce an explanation."
            
    
    
    
    
    return explanation
    
            
        

       
def getExplanation(user_name, image_id, cnx, N=4, M=2):
    
    
    X_new = np.array(getfeatureStatus(image_id, cnx))
    
    with open(base_path+'Data/Generated/trees/'+user_name+'.pkl', 'rb') as fid:
        clf = cPickle.load(fid)  
        
        
        
    Decision = clf.predict(X_new.reshape(1, -1))[0]
    if clf.tree_.node_count==1:
       
        if Decision==0:
            explanation = generateExplanation([], 'NotShare_NoTree')
        elif Decision ==1:
            explanation = generateExplanation([], 'Share_NoTree')
        return [Decision, explanation]
    
    rules = getRules(clf, feature_names, X_new)
    present_attributes = [sub_rule[0] for sub_rule in rules if sub_rule[1]==1]
    
    
    with open(base_path+"Data/Generated/user_attributes.json") as outfile:
        user_attributes = json.load(outfile)
    
    attribute_sensitivity = [user_attributes[user_name][feature_names.index(sub_rule[0])] for sub_rule in rules]
    
    attr2sens = {}
    tree_attributes = [sub_rule[0] for sub_rule in rules]
    
    for attr, sens in zip(tree_attributes, attribute_sensitivity):
        attr2sens[attr] = sens
    
    if Decision ==0:
        
        """Write all possible cases for the do not share condition"""
        
        sensitive_attributes = [sub_rule[0] for i, sub_rule in enumerate(rules) if (attribute_sensitivity[i]>0.5 and sub_rule[0] in present_attributes)]
        
        """Case1: At least one sensitive attibute is present"""
        
        if len(sensitive_attributes)>0:
            
            
            """1. Maximum number of sensitive attributes (N)<len(sensitive_attributes)"""
            
            if len(sensitive_attributes)>N:
                sensitiveattr2sens = {sens_attr: attr2sens[sens_attr] for sens_attr in sensitive_attributes}
                sorted_attr2sens = sorted(sensitiveattr2sens.items(), key=operator.itemgetter(1), reverse=True)[:N]
                attrs_exp = [k for k, v in sorted_attr2sens.items()]
                exp_type = 'NotShare_OwnSensPresent'
                explanation = generateExplanation(attrs_exp, exp_type, cnx)
            else:
                exp_type = 'NotShare_OwnSensPresent'
                explanation = generateExplanation(sensitive_attributes, exp_type, cnx)
        else:
            
            
            """Find out all the nearest friends (similarity_coefficient>0,5) with same type of recommendation"""
            
            with open(base_path+"Data/Generated/user_similarity_sgd.json") as outfile:
                user_similarity = json.load(outfile)
                
            user2sim = user_similarity[user_name]
            candidate_users = [user for user, sim in user2sim.items() if sim>0.5]
            selected_users = []
            clfs = {}
            
            for user in candidate_users:
                with open(base_path+'Data/Generated/trees/'+user+'.pkl', 'rb') as fid:
                    clf = cPickle.load(fid) 
                clfs[user] = clf
                if Decision == clf.predict(X_new.reshape(1, -1))[0]:
                   selected_users.append(user)
            if len(selected_users)>0:
                
                if len(selected_users)==1:
                    most_similar_users = [selected_users]
                    #rules = getRules(clfs[selected_users[0]], feature_names, X_new)
                elif len(selected_users)>1:
                    selected_user2sim = {k:user2sim[k] for k in selected_users}
                    most_similar_users = sorted(selected_user2sim.items(), key=operator.itemgetter(1), reverse=True)
                
                found_similar =False
                for most_similar_user in most_similar_users:
                    rules = getRules(clfs[most_similar_user[0]], feature_names, X_new)
                    present_attributes = [sub_rule[0] for sub_rule in rules if sub_rule[1]==1]
                    attribute_sensitivity = [user_attributes[most_similar_user[0]][feature_names.index(sub_rule[0])] for sub_rule in rules]  
                    tree_attributes = [sub_rule[0] for sub_rule in rules]
                    attr2sens = {}
                    for attr, sens in zip(tree_attributes, attribute_sensitivity):
                        attr2sens[attr] = sens
                    sensitive_attributes = [sub_rule[0] for i, sub_rule in enumerate(rules) if (attribute_sensitivity[i]>0.5 and sub_rule[0] in present_attributes)]
                    if len(sensitive_attributes)>0:
                    
            
                        """1. Maximum number of sensitive attributes (N)<len(sensitive_attributes)"""
            
                        if len(sensitive_attributes)>N:
                            sensitiveattr2sens = {sens_attr: attr2sens[sens_attr] for sens_attr in sensitive_attributes}
                            sorted_attr2sens = sorted(sensitiveattr2sens.items(), key=operator.itemgetter(1), reverse=False)[:N]
                            attrs_exp = [value[0] for value in sorted_attr2sens]
                            exp_type = 'NotShare_OtherSensPresent'
                            explanation = generateExplanation(attrs_exp, exp_type, cnx)
                            found_similar = True
                            break
                        else:
                            exp_type = 'NotShare_OtherSensPresent'
                            explanation = generateExplanation(sensitive_attributes, exp_type, cnx)
                            found_similar = True
                            break
                if found_similar==False:
                    exp_type = 'NotShare_Insufficient' 
                    explanation = generateExplanation([], exp_type, cnx)
                    
            else:
                exp_type = 'NotShare_Insufficient' 
                explanation = generateExplanation([], exp_type, cnx)
                
    elif Decision ==1:
        
        """Write all possible cases for the share condition"""
        
        sensitive_attributes = [sub_rule[0] for i, sub_rule in enumerate(rules) if (attribute_sensitivity[i]<0.5 and sub_rule[0] in present_attributes)]
        
        """Case1: At least one sensitive attibute is present"""
        
        if len(sensitive_attributes)==0:
            exp_type = "Share_OwnSensAbsent"
            explanation = generateExplanation([], exp_type, cnx)
        
        if len(sensitive_attributes)>0:
            if len(sensitive_attributes)<M:
                exp_type = "Share_OwnSensPresent"
                explanation = generateExplanation(sensitive_attributes, exp_type, cnx)
            else:
                sensitiveattr2sens = {sens_attr: attr2sens[sens_attr] for sens_attr in sensitive_attributes}
                sorted_attr2sens = sorted(sensitiveattr2sens.items(), key=operator.itemgetter(1), reverse=False)[:M]
                attrs_exp = [value[0] for value in sorted_attr2sens]
                exp_type = "Share_OwnSensPresent"
                explanation = generateExplanation (attrs_exp, exp_type, cnx)
            
           
    if exp_type=="NotShare_OtherSensPresent":
        exp_type="NotShare_OwnSensPresent"
    return [Decision, explanation, exp_type]
             
       
if __name__=="__main__":
    cnx=get_Connection()
    cursor = cnx.cursor()
    if sys.argv[1]=="generateExplanation":
        user_name = sys.argv[2]
        command = "select image_id from evaluation where display_status=0 and user_name=%s;"
        cursor.execute(command, (user_name, ))
        image_ids = cursor.fetchall()
        image_ids = [row[0] for row in image_ids]
        for image_id in image_ids:
            [Decision, explanation, exp_type] = getExplanation(user_name, image_id, cnx)
            command = "update evaluation set explanation="+"'"+explanation+"'"+","+"recommendation="+"'"+str(Decision)+"'"+","+"exp_type="+"'"+exp_type+"'"+"where user_name="+"'"+sys.argv[2]+"'"+"and image_id="+"'"+image_id+"'"+";"    
            cursor.execute(command)
        cnx.commit()