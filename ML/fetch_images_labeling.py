#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jul  9 18:35:25 2019

@author: hxs1943
"""

import json
import copy
import mysql
import mysql.connector
from mysql.connector import errorcode

user_name = "hiteshsapkota@gmail.com"

with open("base_dir.json") as outfile:
    base_path_file = json.load(outfile)

base_path = base_path_file['base_dir']

def get_Connection():
    
   with open(base_path+"Data/Generated/config.json") as outfile:
        config=json.load(outfile)
   
   try:
       cnx = mysql.connector.connect(**config)
       return cnx
     
   except mysql.connector.Error as err:
       
       if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
           print("Something is wrong with your user name or password")
           
       elif err.errno == errorcode.ER_BAD_DB_ERROR:
           print("Database does not exist")
           
       else:
           print(err)

#with open("Data/Generated/sample_3000_image_annotation_paths.json") as outfile:
#    annotation_paths = json.load(outfile)

cnx = get_Connection()
cursor = cnx.cursor()

sql = "SELECT * FROM image_label where display_status =1 and user_name='hiteshsapkota@gmail.com' and description is not null"
cursor.execute(sql)
record = cursor.fetchall()
count=0
for i, row in enumerate(record):
    ID = row[0]
    user_name = row[1].replace('\r', '').replace('\n', '')
    image_id = row[2].replace('\r', '').replace('\n', '')
    image_path = row[3].replace('\r', '').replace('\n', '')
    display_status = row[4]
    description = row[5].replace('\r', '').replace('\n', '').replace('\t', '')
    attributes = row[6]
    attributes = attributes.replace('\r', '').replace('\n', '')
    
    if attributes == 'Safe':
       
        continue
    #print(description)
    if row[5].find('Skip')!=-1:
        print(row[5])
        print("Skipping")
        continue
    count+=1
    
print("Total well labeled instances are:", count)    
    #sql = "INSERT INTO image_label (user_name, image_id, image_path, display_status, description) VALUES (%s, %s, %s, %s, %s)"
    #values = ('pradeep1@gmail.com' , image_id, image_path, 0, description)
    #cursor.execute(sql, values)
#cnx.commit()  
#for annotation_path in annotation_paths:
#    temp = copy.deepcopy(annotation_path).replace("\n", "")
#    with open("Data/Collected/"+temp) as outfile:
#        json_file = json.load(outfile)
#        image_path = json_file["image_path"]
#        values = (user_name, temp, image_path, 0)
#        sql = "INSERT INTO image_label (user_name, image_id, image_path, display_status) VALUES (%s, %s, %s, %s);"
#        cursor.execute(sql, values)
#cnx.commit()