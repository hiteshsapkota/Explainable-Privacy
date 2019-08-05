#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jul  4 10:02:09 2019

@author: hxs1943
"""

from utils import *
import os
import json
import shutil
from shutil import copyfile
import pandas as pd
import copy 


DEST_PATH = "Data/Generated/attribute_samples"
with open("Data/Generated/cluster_image.json") as outfile:
    cluster_image = json.load(outfile)
cluster_number = [k for k, v in cluster_image.items()]
 
df = []

[attrs, _] = load_attributes()
for attr_id, attr_name in attrs.items():
    os.mkdir(DEST_PATH+"/"+attr_id)
    image_count = 0
    for cluster in cluster_number:
        
        if image_count>4:
            break
        image_ids = cluster_image[cluster]
        print("I am here")
        for image_id in image_ids:
                
                attributes = getImageAttributes(image_id)
                if (attr_id in attributes and len(attributes)==1):
                    image_count+=1
                    with open("Data/Collected/"+image_id) as jsonfile:
                        file = json.load(jsonfile)
                        image_path = file['image_path']
                        img_name = copy.deepcopy(image_path)
                        img_name = img_name.replace("images/", "").replace("train2017/", "").replace("val2017/", "").replace("test2017/", "")
                        copyfile(image_path, DEST_PATH+"/"+attr_id+"/"+img_name)
                        temp_attributes = copy.deepcopy(attributes)
                        temp_attributes.remove(attr_id)
                        data = [image_path, attr_id, ','.join(temp_attributes)]
                        df.append(data)
                    if image_count>4:
                        break
                        
                    
 
df = pd.DataFrame(df)
df.columns =  ['Image Name', 'Concerned Attribute', 'Other Attributes']             
df.to_csv("Data/Generated/attribute_samples/info_image_samples.csv")
                        
                        
                
            
    