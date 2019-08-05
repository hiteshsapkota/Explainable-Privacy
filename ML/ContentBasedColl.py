#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Apr 19 09:58:01 2019

@author: hxs1943
"""
import numpy as np
from sklearn.metrics import mean_squared_error
import matplotlib.pyplot as plt
import pandas as pd
import copy

"""Remove 30% of the data for testing"""
def train_test_split(sharing_matrix):
    test = np.zeros(sharing_matrix.shape)
    train = sharing_matrix.copy()
    for user in range(sharing_matrix.shape[0]):
        test_ratings = np.random.choice(sharing_matrix[user, :].nonzero()[0], 
                                        size=int(np.floor(len(sharing_matrix[user, :].nonzero()[0])*0.3)), 
                                        replace=False)
        train[user, test_ratings] = 0.
        test[user, test_ratings] = sharing_matrix[user, test_ratings]
        
    # Test and training are truly disjoint
    assert(np.all((train * test) == 0)) 
    return train, test

class ContentMF():
    def __init__(self, sharing_matrix, image_matrix, feedback_matrix, n_attributes = 68, learning='sgd', reg =0, feedback_scale=5):
        """
        Train the matrix facotrization model to get the user latent matrix which is user*images
        Params
        =======
        sharing_matrix: (nd array)
            User* Images with corresponding sharing decision
            image_matrix: (nd array)
                Images*Attributes with corresponding binary value depnding on presence or absense of attribute
            feedback_matrix: (nd array)
                User* Attributes with feedback provided by the user in the range 1-5
            n_attributes: (int)
                Number of attributes
            learning: (float)
                   Learning rate
            reg: (float)
                Regularization term for user latent matrix
            Feedback scale: (int)
                          Value given from the user with 5 as least sensitive and 0 as most sensitive
            
            
        
        """
        self.sharing_matrix = sharing_matrix
        self.image_matrix = image_matrix
        self.feedback_matrix = feedback_matrix
        self.n_attributes = n_attributes
        self.learning = learning
        self.reg = reg
        self.feedback_scale = feedback_scale
        self.n_users, self.n_images = sharing_matrix.shape
        self.sample_row, self.sample_col = self.sharing_matrix.nonzero()
        self.n_samples = len(self.sample_row)
        self.mask = copy.deepcopy(feedback_matrix)
        self.mask[self.mask>0] = 1
        """Initialization of user matrix"""
        self.user_matrix = np.random.normal(scale = 1./self.n_attributes, size = (self.n_users, self.n_attributes))
        def train(self, n_iter=10, learning_rate=0.1):
            """Train the model foe n_iter from scratch"""
            self.learning_rate = learning_rate
            self.user_bias = np.zeros(self.n_users)
            self.global_bias = np.mean(self.sharing_matrix[np.where(self.ratings!=0)])
            """Start training"""
            self.partial_train(n_iter)
        def partial_train(self, n_iter):
            iteration = 1
            while iteration<=n_iter:
                feedback_norm = (self.feedback_matrix-1)*(np.amax(self.user_matrix, axis=1)-np.amin(self.user_matrix, axis=1))[:, np.newaxis]/(self.feedback_matrix-1)+np.amin(self.user_matrix, axis=1)[:, np.newaxis]
                self.user_matrix = self.user_matrix+(feedback_norm*self.mask-self.user_matrix)*self.mask
                if iteration%10==0:
                    print('\t Current iteration: {}'.format(iteration))
                self.training_indices = np.arange(self.n_samples)
                np.random.shuffle(self.training_indices)
                self.sgd() 
                iteration+=1
            feedback_norm = (self.feedback_matrix-1)*(np.amax(self.user_matrix, axis=1)-np.amin(self.user_matrix, axis=1))[:, np.newaxis]/(self.feedback_matrix-1)+np.amin(self.user_matrix, axis=1)[:, np.newaxis]
            self.user_matrix = self.user_matrix+(feedback_norm*self.mask-self.user_matrix)*self.mask
                
                
        def sgd(self):
            for idx in self.training_indices:
                u = self.sample_row[idx]
                i = self.sample_col[idx]
                prediction = self.predict(u, i)
                e = (self.sharing_matrix[u, i]-prediction)
                # Update bias
                self.user_bias[u]+=self.learning_rate*(e-self.reg*self.user_bias[u])
                # Update user latent matrix
                self.user_matrix[u, :]+=self.learning_rate*(e*self.image_matrix[i, :]-self.reg*self.user_matrix[u, :])    
                
                
        def predict(self, u, i):
            """Single user and item prediction"""
            prediction = self.global_bias + self.user_bias[u]
            prediction+=self.user_matrix[u, :].dot(self.image_matrix[i, :].T)
            return prediction
        
        def predict_all(self):
            """Predict ratings for every user and item."""
            predictions = np.zeros(self.n_users, self.n_images)
            for u in range(self.n_users):
                for i in range(self.n_images):
                    predictions[u, i] = self.predict(u, i)
            return predictions
        
        def calculate_learning_curve(self, iter_array, test, learning_rate=0.1):
            """
            Keep track of MSE as a function of training iterations.
            
            Params
            ====
            iter_array: (list)
            List of number of iterations to train for each step of the learning curve. e.g., [1, 5, 10, 20]
            test: (2D ndarray)
             Testing dataset (assumed to be user*image)
             
             The function creates two new class attributes
             train_mse (list)
             test_mse (list)
            """
            iter_array.sort()
            self.train_mse = []
            self.test_mse = []
            iter_diff = 0
            for (i, n_iter) in enumerate(iter_array):
                if i==0:
                    self.train(n_iter-iter_diff, learning_rate)
                else:
                    self.partial_train(n_iter-iter_diff)
                predictions = self.predict_all()
                self.train_mse+=[get_mse(predictions, self.sharing_matrix)]
                self.test_mse+=[get_mse(predictions, test)]
                iter_diff = n_iter

def get_mse(pred, actual):
            # Ignore nonzero terms
            pred = pred[actual.nonzero()].flatten()
            actual = actual[actual.nonzero()].flatten()   
            return mean_squared_error(pred, actual)     

def plot_learning_curve(iter_array, model):
           plt.plot(iter_array, model.train_mse, \
             label='Training', linewidth=5)
           plt.plot(iter_array, model.test_mse, \
             label='Test', linewidth=5)
           plt.xticks(fontsize=16);
           plt.yticks(fontsize=16);
           plt.xlabel('iterations', fontsize=30);
           plt.ylabel('MSE', fontsize=30);
           plt.legend(loc='best', fontsize=20);
           plt.show()   
           
def getUserMatrix(sharing_matrix, image_matrix, feedback_matrix):
    train, test = train_test_split(sharing_matrix)
    num_features = sharing_matrix.shape[1]   
    regularizations = [0.01, 0.1, 1, 10, 100]
    iter_array = [1, 2, 5, 10, 25, 50, 100]            
    best_params = {}
    best_params['reg'] = regularizations[0]
    iter_array = [1, 2, 5, 10, 25, 50, 100, 200]
    learning_rates = [1e-5, 1e-4, 1e-3, 1e-2]
    best_params['learning_rate'] = None
    best_params['n_iter'] = 0
    best_params['train_mse'] = np.inf
    best_params['test_mse'] = np.inf
    best_params['model'] = None
    for rate in learning_rates:
        print('Rate: {}'.format(rate))
        MF_SGD = ContentMF(train, image_matrix, feedback_matrix, n_factors=num_features, learning='sgd', reg = regularizations[1])
        MF_SGD.calculate_learning_curve(iter_array, test, learning_rate=rate)
        min_idx = np.argmin(MF_SGD.test_mse)
        if MF_SGD.test_mse[min_idx] < best_params['test_mse']:
            best_params['n_iter'] = iter_array[min_idx]
            best_params['learning_rate'] = rate
            best_params['train_mse'] = MF_SGD.train_mse[min_idx]
            best_params['test_mse'] = MF_SGD.test_mse[min_idx]
            best_params['model'] = MF_SGD
            print('New optimal hyperparameters')
            print(pd.Series(best_params))
    plot_learning_curve(iter_array, best_params['model'])
    print("Best model statistics:", best_params)
    
    return best_params['model'].user_vecs
    
     
