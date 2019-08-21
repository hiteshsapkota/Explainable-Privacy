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
sql = "update record set description=%s where image_id=%s;"
df = pd.read_csv('image_description.csv')
image_ids = df.ImageId.tolist()
descriptions = df.Description.tolist()
for image_id, description in zip(image_ids, descriptions):
    values = [description, image_id]
    cursor.execute(sql, values)
cnx.commit()

