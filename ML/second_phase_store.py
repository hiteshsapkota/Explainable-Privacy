#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Jul 19 17:24:43 2019

@author: hxs1943
"""

import numpy as np
from sklearn.metrics import cohen_kappa_score
from utils import *
import pandas as pd
image_data = pd.read_csv('second_phase_images.csv')
image_ids = image_data.Image_ID.tolist()
if __name__=="__main__":
    cnx = get_Connection()
    cursor = cnx.cursor()
    sql = "select * from image_label where user_name=%s and display_status=1;"
    cursor.execute(sql, ('hiteshsapkota@gmail.com', ))
    record_hitesh = cursor.fetchall()
    cursor.execute(sql, ('pradeep1@gmail.com', ))
    record_pradeep = cursor.fetchall()
    print("Working on the pradeep section")
    for i, row in enumerate(record_pradeep):
        image_id = row[2]
       
        if image_id in image_ids[0:25]:
            
             sql = "INSERT INTO image_label (user_name, image_id, image_path, display_status, description) VALUES (%s, %s, %s, %s, %s);"
             values = ['pradeep1@gmail.com', row[2], row[3], 0, row[5]]
             cursor.execute(sql, values)
        
             
    for i, row in enumerate(record_hitesh):
        image_id = row[2]
        if image_id in image_ids[25:30]:
             print("Successful")
             sql = "INSERT INTO image_label (user_name, image_id, image_path, display_status, description) VALUES (%s, %s, %s, %s, %s);"
             values = ['pradeep1@gmail.com', row[2], row[3], 0, row[5]]
             cursor.execute(sql, values)
    cnx.commit()
        