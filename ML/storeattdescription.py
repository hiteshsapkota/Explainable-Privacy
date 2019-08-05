#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Aug  2 10:17:06 2019

@author: hxs1943
"""

import pandas as pd
from utils import *
if __name__=="__main__":
    cnx = get_Connection()
    cursor = cnx.cursor()
    df = pd.read_csv("attributes.csv")
    IDS = df.id.tolist()
    names = df.name.tolist()
    descriptions = df.Description.tolist()
    for ID, name, description in zip(IDS, names, descriptions):
        command = "insert into attribute (id, name, description) values(%s, %s, %s);"
        values = (ID, name, description)
        cursor.execute(command, values)
    cnx.commit()
        