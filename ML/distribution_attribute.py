#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jul 30 10:33:00 2019

@author: hxs1943
"""

import os
import matplotlib.pyplot as plt
plt.figure(figsize=(18,12))

if __name__=="__main__":
    _, dirs, _ = next(os.walk("Data/Generated/attribute_images/"))
    attribute_name = []
    image_count = []
    for dir_name in dirs:
        _, _, files = next(os.walk("Data/Generated/attribute_images/"+dir_name))
        attribute_name.append(dir_name)
        image_count.append(len(files))
    y_pos = range(len(attribute_name))
    plt.bar(y_pos, image_count, align='center', alpha=0.5)
    plt.xticks(y_pos, attribute_name, rotation=90)
    plt.ylabel('Number of Images')
    plt.title('Image Dsitribution over the Attributes')
    plt.savefig('image_distribution.png')
    plt.show()
    