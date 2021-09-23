import joblib
import pandas
import pickle
import os
from os.path import dirname, join
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from com.chaquo.python import Python
import json

files_dir = str(Python.getPlatform().getApplication().getFilesDir())
# ss_1=-1, ss_2=-1, ss_3=-1, ss_4=-1, ss_5=-1, ss_6=-1, ss_7=-1,ss_8=-1,
# lac_1=-1, lac_2=-1, lac_3=-1, lac_4=-1, lac_5=-1, lac_6=-1, lac_7=-1,lac_8=-1,
# cid_1=-1, cid_2=-1, cid_3=-1, cid_4=-1, cid_5=-1, cid_6=-1, cid_7=-1,cid_8=-1,
# mnc_1=-1,  mnc_2=-1, mnc_3=-1, mnc_4=-1, mnc_5=-1, mnc_6=-1, mnc_7=-1, mnc_8=-1,
# lon=-1, lon2=-1, lon3=-1, lon4=-1, lon5=-1, lon6=-1, lon7=-1,lon8=-1,
# lat=-1, lat2=-1, lat3=-1, lat4=-1, lat5=-1, lat6=-1, lat7=-1,lat8=-1
def prediction_rf(parameterJson):
    if not os.path.exists(files_dir+"\\rf32.pkl"):
        fileX = join(dirname(__file__), "trainX.csv")
        X=pd.read_csv(fileX)
        fileY = join(dirname(__file__), "trainY.csv")
        Y=pd.read_csv(fileY)
        regr = RandomForestRegressor().fit(X, Y)
        joblib.dump(value=regr,filename=files_dir+"\\rf32.pkl",compress=3)

    filename = join(dirname(__file__), files_dir+"\\rf32.pkl")
    model = joblib.load(filename)
    dict=json.loads(str(parameterJson))[0]

    #列顺序，与训练集一致
    sf_col = ['ss_1', 'ss_2', 'ss_3', 'ss_4', 'ss_5', 'ss_6', 'ss_7','ss_8',
              'lac_1', 'lac_2', 'lac_3', 'lac_4', 'lac_5', 'lac_6', 'lac_7','lac_8',
              'cid_1', 'cid_2', 'cid_3', 'cid_4', 'cid_5', 'cid_6', 'cid_7','cid_8',
              'mnc_1',  'mnc_2', 'mnc_3', 'mnc_4', 'mnc_5', 'mnc_6', 'mnc_7', 'mnc_8',
              'lon', 'lon2', 'lon3', 'lon4', 'lon5', 'lon6', 'lon7','lon8',
              'lat', 'lat2', 'lat3', 'lat4', 'lat5', 'lat6', 'lat7','lat8']
    for col in sf_col:
        if col not in dict.keys():
            dict[col]=-1
    MRdata=pd.DataFrame(dict,index=[0])
    MRdata=MRdata[sf_col]
    MRdata.fillna(-1)
    result = model.predict(MRdata)[0]
    return result

