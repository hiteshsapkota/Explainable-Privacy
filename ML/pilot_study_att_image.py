#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jul 30 09:47:51 2019

@author: hxs1943
"""

from utils import get_Connection, getImageAttributes
import os
from shutil import copyfile

cnx = get_Connection()

if __name__=="__main__":
    cursor = cnx.cursor()
    command = "select * from record"
    cursor.execute(command)
    data = cursor.fetchall()
    for i, row in enumerate(data):
        image_id = row[1].replace("\r", "").replace("\n", "")
        attributes = getImageAttributes(image_id, cnx)
        image_path = row[2].replace("\r", "").replace("\n", "")
        for attribute in attributes:
            print("Attribute is", attribute)
            if not os.path.isdir("Data/Generated/attribute_images/"+attribute):
                os.mkdir("Data/Generated/attribute_images/"+attribute, 0o755)
            src = image_path
            image_name = image_path.split("/")[2]
            dst = "Data/Generated/attribute_images/"+attribute+"/"+image_name
            copyfile(src, dst)
            
        