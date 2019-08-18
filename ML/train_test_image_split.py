#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jul 22 10:24:23 2019

@author: hxs1943
"""

from utils import * 
import random
import numpy as np

cnx = get_Connection()
cursor = cnx.cursor()
sql = "select * from temporary where user_name=%s and display_status=1 and attributes is not null and description is not null;"
cursor.execute(sql, ('hiteshsapkota@gmail.com', ))
record_hitesh = cursor.fetchall()
cursor.execute(sql, ('pradeep1@gmail.com', ))
record_pradeep = cursor.fetchall()
j=0
data = []
image_ids = []
for i, row in enumerate(record_pradeep):
    ID = row[0]
    user_name = row[1].replace('\r', '').replace('\n', '')
    image_id = row[2].replace('\r', '').replace('\n', '')
    image_ids.append(image_id)
    image_path = row[3].replace('\r', '').replace('\n', '')
    description = row[5].replace('\r', '').replace('\n', '')
    attributes = row[6].replace('\r', '').replace('\n', '').split(',')
    if len(attributes)==1 and attributes[0]=='Safe':
        continue
    if len(attributes)==0:
        continue
#    print("For the iteration", j)
#    print(ID, user_name, image_id, image_path, description, attributes)
#    data
    if attributes[0]=='':
        continue
    if image_id=='annotations/train2017/2017_81545583.json':
        print("I am here")
        print("Attribute is")
    j=j+1
    
        
    data.append([image_id, image_path, description, row[6].replace('\r', '').replace('\n', '')])
    

for i, row in enumerate(record_hitesh):
    ID = row[0]
    user_name = row[1].replace('\r', '').replace('\n', '')
    image_id = row[2].replace('\r', '').replace('\n', '')
    
    image_path = row[3].replace('\r', '').replace('\n', '')
    description = row[5].replace('\r', '').replace('\n', '')
    attributes = row[6].replace('\r', '').replace('\n', '').split(',')
    if image_id in image_ids:
       
        continue
    
    if len(attributes)==1 and attributes[0]=='Safe':
       
        continue
    if len(attributes)==0:
        continue
    if row[5].find('Skip')!=-1:
       
        continue
    image_ids.append(image_id)
    if attributes[0]=='':
        continue
    if image_id=='annotations/train2017/2017_81545583.json':
        print("I am here")
        print(attributes)
    
    data.append([image_id, image_path, description, row[6].replace('\r', '').replace('\n', '')])
    
total_idx = range(0, len(data))
train_idx = random.sample(total_idx, int(np.ceil(0.8*len(total_idx))))
test_idx = list(set(total_idx)-set(train_idx)) 
for i in train_idx:
    sql = "INSERT INTO record (image_id, image_path, image_type, description, attributes) VALUES (%s, %s, %s, %s, %s);"
    [image_id, image_path, description, attributes]=data[i]
    values = [image_id, image_path, 'train', description, attributes]
    cursor.execute(sql, values)

for i in test_idx:
    sql = "INSERT INTO record (image_id, image_path, image_type, description, attributes) VALUES (%s, %s, %s, %s, %s);"
    [image_id, image_path, description, attributes]=data[i]
    values = [image_id, image_path, 'eval', description, attributes]
    cursor.execute(sql, values)
cnx.commit()
