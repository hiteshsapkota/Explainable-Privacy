#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jan 30 16:17:32 2019

@author: hiteshsapkota
"""
"""Imports"""
from sklearn.tree import DecisionTreeClassifier
from treetoruleconverstion import *

def weighted_tree(X, y, W, criteria='entropy'):
    
    """Parameters:
        1) X input with dimension N*D; N=Number of samples, D=Number of features
        2) Y input with dimension N*1, N=Number of samples,
        3) W Weight of eacn sample, with dimension N*1
        2) Criteria used in splitting, default=entropy
         Returns model
       
       
    """
    clf = DecisionTreeClassifier(criterion=criteria)
    clf.fit(X, y, sample_weight=W)
    
    return clf

def getRules(clf, features, X_test):
    
    rule = None
    all_rules = tree_to_code(clf, features)
    rule= getRule(all_rules, X_test, [0, 1], features)
    conditions = rule[0]
    subrule_status = []
    
    for condition in conditions:
        
        if str(condition[1])=='>':
            sub_rule = [condition[0], 1]
            
        else:
            sub_rule = [condition[0], 0]
        subrule_status.append(sub_rule)
        
    return subrule_status
            
    
