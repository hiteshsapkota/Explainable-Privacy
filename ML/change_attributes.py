#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Aug 14 11:06:45 2019

@author: hxs1943
"""


from utils import * 
import random
import numpy as np
from PIL import Image
import copy
import sys
import psutil

cnx = get_Connection()
cursor = cnx.cursor()
sql = "select * from temporary;"
cursor.execute(sql)
record = cursor.fetchall()
image_ids = []
sql = "update temporary set attributes=%s where id=%s"
for i, row in enumerate(record):
    ID=row[0]
    description = row[4].replace('\r', '').replace('\n', '')
    image_id = row[1].replace('\r', '').replace('\n', '')
    attributes = row[5].replace('\r', '').replace('\n', '').split(',')
    image_path = row[2].replace('\r', '').replace('\n', '')
    if ("Spectators" in attributes) :
        print("Working on the id", ID)
        print("\n Description:", description)
        img = Image.open(image_path)
        img.show()
        number = int(input("\n Enter 1 for Own Spectators, 0 for Others Spectators, 2 for both \n"))
        updated_attributes = copy.deepcopy(attributes)
        if "Spectators" in updated_attributes:
            updated_attributes.remove("Spectators")
       
        if number==1:
            updated_attributes=updated_attributes+["Spectators-own"]
        elif number==0:
            updated_attributes = updated_attributes+["Spectators-other"]
        elif number==2:
            updated_attributes = updated_attributes+["Spectators-own"]
            updated_attributes = updated_attributes+["Spectators-other"]
        else:
            sys.exit("Invalid Input")
        updated_attributes = ",".join(updated_attributes)
        values = [updated_attributes, ID]
        cursor.execute(sql, values)
        cnx.commit()
        img.close()
        
        
        
            
            
            
