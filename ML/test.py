#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Aug 14 09:55:27 2019

@author: hxs1943
"""

from utils import * 
import random
import numpy as np
import pandas as pd
cnx = get_Connection()
cursor = cnx.cursor()
sql = "select * from temporary where user_name='hiteshsapkota@gmail.com';"
cursor.execute(sql)
record = cursor.fetchall()
sql = "INSERT INTO incorrect (image_id, image_path, display_status, description, attributes, user_name) VALUES (%s, %s, %s, %s, %s, %s);"
df = pd.read_csv('image_description.csv')
image_ids = df.ImageId.tolist()
descriptions = df.Description.tolist()
for i, row in enumerate(record):
    image_id = row[1].replace('\r', '').replace('\n', '')
    if image_id in image_ids:
        image_path = row[2].replace('\r', '').replace('\n', '')
        display_status = row[3]
        description = descriptions[image_ids.index(image_id)]
        attributes = row[5].replace('\r', '').replace('\n', '')
        user_name = 'pradeepmcl@gmail.com'
        values = [image_id, image_path, 0, description, attributes, user_name]
        cursor.execute(sql, values)
cnx.commit()