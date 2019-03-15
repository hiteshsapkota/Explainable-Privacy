#!/usr/bin/python
"""Libraries
"""
import json
import sys
import csv
import os.path as osp
import random
import pandas as pd
import numpy as np
import mysql
import mysql.connector
from mysql.connector import errorcode

"""This gives current directory"""

with open("base_dir.json") as outfile:
    base_path_file = json.load(outfile)

base_path = base_path_file['base_dir']





"""Testing and Cluster Image Files"""

test_image_anno_paths = []


with open(base_path+"Data/Collected/test2017.txt") as anno_file:
    
    for line in anno_file:
        anno_path = osp.join('', line.strip())
        test_image_anno_paths.append(anno_path)

    
with open(base_path+"Data/Generated/cluster_image.json") as outfile:
    cluster_image = json.load(outfile)
 
    
cluster_number = [k for k, v in cluster_image.items()]


def load_attributes(attr_list_path=None):
    
    """
    Returns mappings: {attribute_id -> attribute_name} and {attribute_id -> idx}
    where attribute_id = 'aXX_YY' (string),
    attribute_name = description (string),
    idx \in [0, 67] (int)
    :return:
    """
    
    if attr_list_path is None:
        attributes_path = osp.join(base_path+'Data/Collected/attributes.tsv')
    
    else:
        attributes_path = attr_list_path
   
    attr_id_to_name = dict()
    attr_id_to_idx = dict()

    with open(attributes_path, 'r') as fin:
        ts = csv.DictReader(fin, delimiter='\t')
        rows = filter(lambda r: r['idx'] is not '', [row for row in ts])

        for row in rows:
            attr_id_to_name[row['attribute_id']] = row['description']
            attr_id_to_idx[row['attribute_id']] = int(row['idx'])

    return attr_id_to_name, attr_id_to_idx


def load_attribute_name(file_name, file_path=base_path+"Data/Collected/user_studies"):
    
    data = {}
    file = open(file_path+"/"+file_name)
    
    for line in file:
        line_data = line.split("\t")
        
        if line_data[0]=="attribute_id":
            continue
        
        data[line_data[0]]=line_data[1]
        
    data["a0_safe"]='Safe'
    
    return data


[attr_id_to_name, _]=load_attributes()
feature_names = [k for k, v in attr_id_to_name.items()]
attribute_map_file = load_attribute_name("user_profiles.tsv")


def getImageAttributes(image_id):
    
    """image id will be in the form of annotations/image_type/id.json"""
    
    with open(base_path+"Data/Collected/"+image_id) as jf:
         anno=json.load(jf)
         labels = anno['labels']
    
    return labels


def getfeatureStatus(image_id):
    
    present_attributes = getImageAttributes(image_id)
    featureStatus=[]
    
    for feature in feature_names:
        
        if feature in present_attributes:
            featureStatus.append(1)
        
        else:
            featureStatus.append(0)
    
    return featureStatus
            

def get_Connection():
    
   with open(base_path+"Data/Generated/config.json") as outfile:
        config=json.load(outfile)
   
   try:
       cnx = mysql.connector.connect(**config)
       return cnx
     
   except mysql.connector.Error as err:
       
       if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
           print("Something is wrong with your user name or password")
           
       elif err.errno == errorcode.ER_BAD_DB_ERROR:
           print("Database does not exist")
           
       else:
           print(err)
           

def labels_to_vec(labels, attr_id_to_idx):
    
    n_labels = len(attr_id_to_idx)
    label_vec = np.zeros(n_labels)
    
    for attr_id in labels:
        label_vec[attr_id_to_idx[attr_id]] = 1
        
    return label_vec


def load_tsv(file_name, file_path=base_path+"Data/Collected/user_studies"):
    
    data={}
    file=open(file_path+"/"+file_name)
    
    for line in file:
        line_data=line.split("\t")
        
        if line_data[0]=='attribute_id':
            continue
        
        data[line_data[0]]=line_data[2:]
        
    return data


def generateImageID(user_name, study_mode ='training',num_images = 1):
    
    """Generate Image ID and store it into the database"""
    
    cnx = get_Connection()
    cursor = cnx.cursor()
    
    if study_mode=='training':
        
        clusters = random.sample(cluster_number , int(num_images))
        Image_IDS=[]
        Image_Paths = []
        
        for cluster in clusters:
            images = cluster_image[cluster]
            image_id = random.sample(images, 1)
            Image_IDS.append(image_id[0])
            
        for image_id in Image_IDS:
            with open(base_path+"Data/Collected/"+image_id) as jsonfile:
                  file = json.load(jsonfile)
                  Image_Paths.append(file['image_path'])
                  
        for image_id, image_path in zip(Image_IDS, Image_Paths):
            image_path='/'+image_path
            values = (user_name, image_id, image_path, 0)
            sql = "INSERT INTO training (user_name, image_id, image_path, display_status) VALUES (%s, %s, %s, %s);"
            cursor.execute(sql, values)
            
        cnx.commit()
        
        return [Image_IDS, Image_Paths]
    
    elif study_mode =='evaluation':
        Image_IDS = random.sample(test_image_anno_paths, int(num_images))
        Image_Paths= []
        
        for image_id in Image_IDS:
            
            with open(base_path+"Data/Collected/"+image_id) as jsonfile:
                file = json.load(jsonfile)
                Image_Paths.append(file['image_path'])
                
        for image_id, image_path in zip(Image_IDS, Image_Paths):
            values = (user_name, image_id, image_path, 0)
            sql = "INSERT INTO evaluation (user_name, image_id, image_path, display_status) VALUES (%s, %s, %s, %s);"
            
            cursor.execute(sql, values)
            
        cnx.commit()
        
    cnx.close()


def getUserInstances(user_name, table='training'):
    
    cnx = get_Connection()
    cursor = cnx.cursor()
    
    if table=='training':
        sql = "select * from training where user_name=%s and display_status=1 and sharing_decision is not null;"
        
    elif table == 'feedback':
        sql = "select * from evaluation where user_name=%s and display_status=1 and sharing_decision is not null;"
        
    cursor.execute(sql, (user_name, ))
    record = cursor.fetchall()
    
    fields = feature_names+['Decision']+['ImageId']
    df = pd.DataFrame(columns=fields)
    
    for i, row in enumerate(record):
        image_id = row[2]
        features = getfeatureStatus(image_id)
        
        if table=="feedback":
             decision = row[5]
             
        else:
            decision = row[4]
        df.loc[i]=features+[decision]+[image_id]
        
    return df
        
        
def attributetoName(attributes, attribute_map_file):
    
    attribute_name = []
    
    for attribute in attributes:
        attribute_name.append(attribute_map_file[attribute])
        
    return attribute_name

                
def nametoAttribute(names, attribute_map_file):
    
   return [k for k,v in attribute_map_file.items() if v in names]   
 
    
def storeFeedback(user_name):
    
    names=[]
    sensitivity_scores=[]
    data=pd.read_csv(base_path+"instance_attribute_feedback.csv")
    
    for index, row in data.iterrows():
        attribute = row['Attribute']
        attribute=attribute.replace('\r', '')
        attribute=attribute.replace('\n', '')
        names.append(attribute)
        sensitivity_scores.append(int(row['Value']))
  
    attributes = nametoAttribute(names, attribute_map_file)
  
    with open(base_path+"Data/Generated/feedback.json", "r+") as iofile:
        feedback_file = json.load(iofile)
        user_features = feedback_file[user_name]
        
        for i, attribute in enumerate(attributes):
            
            user_features[feature_names.index(attribute)]=(6-sensitivity_scores[i])
            
        feedback_file[user_name]=user_features
        iofile.seek(0)
        json.dump(feedback_file, iofile)
        iofile.truncate()
        
        
def getTrainedUsers(users, th=4):
    cnx = get_Connection()
    cursor = cnx.cursor()
    trained_users=[]
    for user in users:
        sql = "select count(*) from training where display_status=1 and user_name=%s"
        cursor.execute(sql, (user, ))
        number_records= cursor.fetchone()[0]
        if number_records>th:
            trained_users.append(user)
        
    return trained_users
        
        
       
        
        
    
        
if __name__=="__main__":
    
    if sys.argv[1]=="generateImageID":
        
        generateImageID(sys.argv[2], sys.argv[3], int(sys.argv[4]))
        
    if sys.argv[1]=="getImageAttributes":
        
        attributes = getImageAttributes(sys.argv[2])
        attribute_map_file = load_attribute_name("user_profiles.tsv")
        attribute_name = attributetoName(attributes, attribute_map_file)
        for attribute in attribute_name:
            print(attribute)
        
        

    if sys.argv[1]=="storeFeedback":
        
        storeFeedback(sys.argv[2])
        
        
        
        
        
    
        
        
    
