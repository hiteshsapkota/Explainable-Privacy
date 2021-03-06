#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jul 18 18:12:25 2019

@author: hxs1943
"""
import numpy as np
from sklearn.metrics import cohen_kappa_score
from utils import *
import pandas as pd
present_attributes = []
def mapping(record, username):
    att_user = []
 
    data = {}
    remove_IDS = [62, 64, 70, 73]
    for i, row in enumerate(record):
        ID = row[0]
        attributes = row[6]
        image_id = row[2]
        attributes = row[6].replace('\r', '').replace('\n', '').split(',')
        if len(attributes)==1 and attributes[0]=='Safe':
            
            continue
        for attribute in attributes:
             if attribute not in att_user:
                  att_user.append(attribute)
        if username=='hiteshsapkota@gmail.com':
            if ID in remove_IDS:
                continue
        data[image_id]=attributes
    return [data, att_user]
            
 

       
    
if __name__=="__main__":
    cnx = get_Connection()
    cursor = cnx.cursor()
    sql = "select * from image_label where user_name=%s and display_status=1 and attributes is not null;"
    cursor.execute(sql, ('hiteshsapkota@gmail.com', ))
    record_hitesh = cursor.fetchall()
    cursor.execute(sql, ('pradeep1@gmail.com', ))
    record_pradeep = cursor.fetchall()
    [image_attribute_map_hitesh, att_hitesh] = mapping(record_hitesh, 'hiteshsapkota@gmail.com')
    [image_attribute_map_pradeep, present_attributes] = mapping(record_pradeep, 'pradeep1@gmail.com')
    present_attributes.remove('')
    att_mat_hitesh = []
    att_mat_pradeep = []
    image_ids = []
    j=0
    for image_id,attributes_hitesh in image_attribute_map_hitesh.items():
        if image_id not in image_attribute_map_pradeep:
            continue
        if j>49:
            break
        j=j+1
        image_ids.append(image_id)
        attributes_pradeep = image_attribute_map_pradeep[image_id]
        att_hitesh = np.zeros(len(present_attributes))
        att_pradeep = np.zeros(len(present_attributes))
        for i, attribute in enumerate(present_attributes):
            if attribute in attributes_hitesh:
                att_hitesh[i]=1
            if attribute in attributes_pradeep:
                att_pradeep[i]=1
        att_mat_hitesh.append(att_hitesh)
        att_mat_pradeep.append(att_pradeep)
        
attribute_similarity = {}
att_mat_hitesh = np.array(att_mat_hitesh)
att_mat_pradeep = np.array(att_mat_pradeep)
df = []
final_attributes = []
for i, attribute in enumerate(present_attributes):
     total_pradeep = np.count_nonzero(att_mat_pradeep[:, i])
     total_hitesh = np.count_nonzero(att_mat_hitesh[:, i])
     if total_pradeep==0 and total_hitesh==0:
         continue
     final_attributes.append(attribute)
     similarity = cohen_kappa_score(att_mat_hitesh[:, i], att_mat_pradeep[:, i])
     attribute_similarity[attribute] = similarity
     total_dissimilarity = len([1 for j, k in enumerate(att_mat_hitesh[:, i].tolist()) if k!=att_mat_pradeep[j, i]])
     df.append([attribute, total_pradeep, total_hitesh, total_dissimilarity, similarity])

            
#df = pd.DataFrame(df, columns = ['Attribute', 'Total Instances Pradeep', 'Total Instances Hitesh', 'Dissimilar Instances', 'Kappa Score'])
#df.to_csv('aggrement_first_phase.csv')  
df =[]
for i in range(0, att_mat_hitesh.shape[0]):
    hitesh_atts = att_mat_hitesh[i]
    pradeep_atts = att_mat_pradeep[i]
    diss_att_indices = [j for j, k in enumerate(hitesh_atts.tolist()) if k!=pradeep_atts[j]]
    if len(diss_att_indices)!=0:
        indices_present_hitesh = np.nonzero(hitesh_atts)
        indices_present_pradeep = np.nonzero(pradeep_atts)
        att_hitesh = [present_attributes[j] for j in  indices_present_hitesh[0].tolist() ]
        att_pradeep = [present_attributes[j] for j in  indices_present_pradeep[0].tolist() ]
        unique_hitesh = np.setdiff1d(att_hitesh, att_pradeep)
        unique_pradeep = np.setdiff1d(att_pradeep, att_hitesh)
        image_id = image_ids[i]
        df.append([image_id, ",".join(list(set(att_hitesh).intersection(att_pradeep))), ",".join(unique_hitesh), ",".join(unique_pradeep)])
        
df = pd.DataFrame(df, columns = ['Image Id', 'Common Attributes', 'Hitesh Attributes', 'Pradeep Attribute'])
df.to_csv('disagree_image.csv')          
        
        
    