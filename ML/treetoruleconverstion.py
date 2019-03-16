#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sun Jan 27 10:40:24 2019

@author: hiteshsapkota
"""
"""Imports"""

from sklearn.tree import _tree
import copy
             
def tree_to_code(tree, feature_names):
    
    rules = []
    tree_ = tree.tree_
    feature_name = [
        feature_names[i] if i != _tree.TREE_UNDEFINED else "undefined!"
        for i in tree_.feature
    ]
    pathto=dict()
    rule = {}
    global k
    k = 0
    
    def recurse(node, depth, parent):
        global k
        
        if tree_.feature[node] != _tree.TREE_UNDEFINED:
            name = feature_name[node]
            threshold = tree_.threshold[node]
            s= "{} <= {} ".format( name, threshold, node )
        
            if node == 0:
                pathto[node]=s
                rule[node]=[]
                rule[node].append((name, '<=', threshold))
                     
            else:
                pathto[node]=pathto[parent]+' & ' +s
                rule[node]=copy.deepcopy(rule[parent])
                rule[node].append((name, '<=', threshold))
               
            recurse(tree_.children_left[node], depth + 1, node)
            s="{} > {}".format( name, threshold)
            
            if node == 0:
                pathto[node]=s
                rule[node]=[]
                rule[node].append((name, '>', threshold))
              
            else:
                pathto[node]=pathto[parent]+' & ' +s
                rule[node]=copy.deepcopy(rule[parent])
                rule[node].append((name, '>', threshold))  
                
            recurse(tree_.children_right[node], depth + 1, node)
        
        else:
            k=k+1
            rules.append((rule[parent], tree_.value[node][0]))
    
    recurse(0, 1, 0) 
    return rules
           

def getRule(rules, data, classes, features): 
    
 try:
     
    for rule in rules:
        satisfied=True
        conditions = rule[0]
        
        for condition in conditions:
            feature = condition[0]
            operator = condition[1]
            threshold = condition[2]
            
            if operator == '<=':
                
                if data[features.index(feature)]<=threshold:
                    continue
                
                else:
                    satisfied = False
                    break
            else:
                
                if data[features.index(feature)]>threshold:
                    continue
                
                else:
                    satisfied = False
                    break
                
        if satisfied is True:
            return rule
    
 except ValueError:
     print(features)
                
       
    
