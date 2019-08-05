#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jul  8 15:04:02 2019

@author: hxs1943
"""
import random
import json
filenames = ['train2017.txt', 'val2017.txt', 'test2017.txt']
data_path = "Data/Collected"
image_ids = []
for filename in filenames:
    file = open(data_path+'/'+filename)
    for image_id in file:
        image_ids.append(image_id)
        
sample_image_ids = random.sample(image_ids, 3000)
with open("sample_3000_image_annotation_paths.json", "w") as infile:
    json.dump(sample_image_ids, infile)