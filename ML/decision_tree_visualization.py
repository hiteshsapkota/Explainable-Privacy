#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jul 25 20:31:19 2019

@author: hiteshsapkota
"""

from sklearn.datasets import load_iris
from sklearn import tree
from sklearn.tree import DecisionTreeClassifier
import pydotplus
import random
import pickle as cPickle

if __name__=="__main__":
    with open('Data/Generated/trees/'+'hiteshsapkota@gmail.com'+'.pkl', 'rb') as fid:
        clf = cPickle.load(fid)  
    
#    clf = DecisionTreeClassifier(random_state=0)
#    iris = load_iris()
#    total_idx = range(len(iris.data))
#    train_num=100
#    train_idx = random.sample(total_idx, train_num)
#    test_idx = list(set(total_idx)-set(train_idx))
#    X_train = iris.data[train_idx, :]
#    X_test = iris.data[test_idx, :]
#    y_train = iris.target[train_idx]
#    y_test = iris.target[test_idx]
#    feature_names = iris.feature_names
#    clf.fit(X_train, y_train)
    # Create DOT data
    dot_data = tree.export_graphviz(clf, out_file=None, 
                                
                                )
    # Draw graph
    graph = pydotplus.graph_from_dot_data(dot_data)  
    graph.write_png("hitesh.png")
