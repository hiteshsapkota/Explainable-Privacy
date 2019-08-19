#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jul 24 18:36:48 2019

@author: hiteshsapkota
"""
from sklearn.datasets import load_iris
from sklearn.tree import DecisionTreeClassifier
import random
import numpy as np



def isdeltaconsistent(lt, t):

    elc = np.sqrt(np.log(2*t**2)/(2*sum(lt)))
    y_lt = lt[1]/sum(lt)
    if np.abs(y_lt-1/2)>elc:
        return [True, y_lt, elc]
    else:
        return [False, y_lt, elc]


def query_search(T, xt, nodes, t, B=0.2):
    node_id = T.apply([xt])[0]
    lt = nodes[node_id][0]
    [delta_consistent, y_lt, elc] = isdeltaconsistent(lt, t)
    if not delta_consistent:
        return ['inconsistent', True]
    else:
        print("Consistent")
        prob = (elc)/(elc+np.abs(y_lt-1/2))
        return ['consistent', random.random()<prob]
    
def findannotators(X_test, image_id, users, cnx):
    """Find users with the X_test labeled"""
    cursor = cnx.cursor()
    total_users=0
    for user in users:
        sql = "select * from training where user_name=%s and image_id=%s"
        cursor.execute(sql, (user, image_id ))
        record = cursor.fetchall()
        if len(record)>0:
            total_users+=1
    return total_users
    
def getLabelInstances(T, X_test, image_ids, nodes, cnx, users, t, batch_size=5, B=0.2):
    
    inconsistent_ids = []
    consistent_ids = []
    for i in range(X_test.shape[0]):
        [cons_status, label_status]=query_search(T, X_test[i], nodes, t, B=B)
        if cons_status=='inconsistent':
            inconsistent_ids.append(i)
        elif cons_status=='consistent' and label_status==True:
            consistent_ids.append(i)
    labelers=[]
    if len(inconsistent_ids)>batch_size:
        for j in inconsistent_ids:
            labelers.append(findannotators(X_test[j], image_ids[j], users, cnx))
        labelers = np.array(labelers)
        labelers = np.exp(-labelers)/(sum(np.exp(-labelers)))
        return [image_ids[j] for j in np.random.choice(inconsistent_ids, batch_size, replace=False, p=labelers)]
    if len(inconsistent_ids)==batch_size:
        return [image_ids[j] for j in inconsistent_ids]
    if len(inconsistent_ids)<batch_size:
        if len(inconsistent_ids)+len(consistent_ids)>=5:
            for j in consistent_ids:
                labelers.append(findannotators(X_test[j], image_ids[j], users, cnx))
            labelers = np.array(labelers)
            labelers = np.exp(-labelers)/(sum(np.exp(-labelers)))
            return [image_ids[j] for j in inconsistent_ids+np.random.choice(consistent_ids, batch_size-len(inconsistent_ids), replace=False, p=labelers)]
        else:
            all_test_idx = range(X_test.shape[0])
            rem_test_idx = list(set(all_test_idx)-set(inconsistent_ids)-set(consistent_ids))
            for j in rem_test_idx:
                labelers.append(findannotators(X_test[j]), image_ids[j],users, cnx)
            labelers = np.array(labelers)
            labelers = np.exp(-labelers)/(sum(np.exp(-labelers)))
            return [image_ids[j] for j in inconsistent_ids+consistent_ids+np.random.choice(rem_test_idx, batch_size-len(inconsistent_ids)-len(consistent_ids), replace=False, p=labelers)]
            
        
        
        
        
    
if __name__=="__main__":
    clf = DecisionTreeClassifier(random_state=0)
    iris = load_iris()
    total_idx = range(len(iris.data))
    train_num=100
    train_idx = random.sample(total_idx, train_num)
    test_idx = list(set(total_idx)-set(train_idx))
    X_train = iris.data[train_idx, :]
    X_test = iris.data[test_idx, :]
    y_train = iris.target[train_idx]
    y_test = iris.target[test_idx]
    feature_names = iris.feature_names
    clf.fit(X_train, y_train)
    node_values = clf.tree_.value
    
    node_id = clf.apply([X_test[0]])[0]
    
    print("Instances", node_values[node_id])
    print(query_search(clf, X_test[0], node_values, 1))
    
