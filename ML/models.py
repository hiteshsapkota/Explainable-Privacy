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
from utils import load_attributes, load_attribute_name
from weighted_decision_tree import getRules
import os.path
import os.path as osp
import sys

"""This gives current directory"""

with open("base_dir.json") as outfile:
    base_path_file = json.load(outfile)


"""Attribute Features Python"""

base_path = base_path_file['base_dir']
attribute_map_file = load_attribute_name("user_profiles.tsv")
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

   
def getfeatureStatus(image_id):
    present_attributes = getImageAttributes(image_id)
    featureStatus=[]
    
    for feature in feature_names:
        if feature in present_attributes:
            featureStatus.append(1)
        else:
            featureStatus.append(0)
    return featureStatus
            
         

def getImageAttributes(image_id):
    """image id will be in the form of annotations/image_type/id.json"""
    with open(base_path+"Data/Collected/"+image_id) as jf:
         anno=json.load(jf)
         labels = anno['labels']
    return labels
  

def combineexp(part1, present, absent):
    
    if len(present)==0 and len(absent) == 0:
        return "Not enough evidence to generate an explanation "
    
    elif len(present) == 0 and len(absent)!=0:
        
        if len(absent)==1:
             part2= "because it does not have attribute: "
             
        else:
            part2 = "because it doesnot have attributes: "
            
        for i, absent_att in enumerate(absent):
            
            if i==0:
                part2+=attribute_map_file[absent_att]
                continue
            
            part2 = part2+','+attribute_map_file[absent_att]
    
    elif len(present) != 0 and len(absent)==0:
        
        if len(present)==1:
            part2= "because it has attribute: "
            
        else:
            part2 = "because it has attributes: "
            
        for i, present_att in enumerate(present):
            
            if i==0:
                part2 = part2+attribute_map_file[present_att]
                continue
                
            part2 = part2+','+attribute_map_file[present_att]
        
    else:
        part2="beacause it has "
        
        for i, present_att in enumerate(present):
            
            if i==0:
                part2+=attribute_map_file[present_att]
                continue
            
            part2+=','+attribute_map_file[present_att]   
            
        if len(present)==1:
                part2+=" attribute and doesnot have "
        
        else:
                part2+=" attributes and doesnot have "
                
        for i, absent_att in enumerate(absent):
            
            if i==0:
                part2+=attribute_map_file[absent_att]
                continue
            part2+=','+attribute_map_file[absent_att]
            
        if len(absent)==1:
            part2+=" attribute "
            
        else:
            part2+=" attributes "
            
    return part1+part2
                

            
def getExplanation(user_name, image_id):
    if not os.path.isfile(base_path+"Data/Generated/trees/"+user_name+".pkl"):
        explanation = "Explanation cannot be created. Please provide training data first"
        return explanation
    
    X_new = np.array(getfeatureStatus(image_id))
    
    with open(base_path+'Data/Generated/trees/'+user_name+'.pkl', 'rb') as fid:
        clf = cPickle.load(fid)  
        
    Decision = clf.predict(X_new.reshape(1, -1))[0]
    
    if clf.tree_.node_count==1:
        image_attributes = getImageAttributes(image_id)
        if len(image_attributes)==1:
                part1 = "it has attribute: "+ attribute_map_file[image_attributes[0]]
        elif len(image_attributes)>1:
             part1 = "it has attributes: "
             for attribute in image_attributes:
                if image_attributes[len(image_attributes)-1]==attribute:
                    part1 += attribute_map_file[attribute]
                    continue
                part1 += attribute_map_file[attribute]+ " "
        if Decision==0:
          return "Looks like you do not want to share because "+part1
        elif Decision==1:
           return "Looks like you want to share because "+part1
        return 
    rules = getRules(clf, feature_names, X_new)
    present_attributes = [sub_rule[0] for sub_rule in rules if sub_rule[1]==1]
    absent_attributes = [sub_rule[0] for sub_rule in rules if sub_rule[1]==0]

    with open(base_path+"Data/Generated/user_attributes.json") as outfile:
        user_attributes = json.load(outfile)
    
    attribute_sensitivity = [user_attributes[user_name][feature_names.index(sub_rule[0])] for sub_rule in rules]
    
    if Decision ==0:
        part1 = "Looks like you do not want to share "
        useful_attributes = [sub_rule[0] for i, sub_rule in enumerate(rules) if attribute_sensitivity[i]<0.5]
        present=[useful_attribute for useful_attribute in useful_attributes if useful_attribute in present_attributes]
        absent = [useful_attribute for useful_attribute in useful_attributes if useful_attribute in absent_attributes]
        explanation = combineexp(part1, present, absent)
        
    elif Decision == 1:
        part1 = "Looks like you want to share "
        useful_attributes = [sub_rule[0] for i, sub_rule in enumerate(rules) if attribute_sensitivity[i]>0.5]
        present=[useful_attribute for useful_attribute in useful_attributes if useful_attribute in present_attributes]
        absent = [useful_attribute for useful_attribute in useful_attributes if useful_attribute in absent_attributes]
        explanation = combineexp(part1, present, absent)
    
    return explanation
             
       
if __name__=="__main__":
    
    if sys.argv[1]=="getExplanation":
        explanation=getExplanation(sys.argv[2],  sys.argv[3])
        
    print(explanation)
