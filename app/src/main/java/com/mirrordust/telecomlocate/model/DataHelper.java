package com.mirrordust.telecomlocate.model;

import android.provider.Settings;
import android.util.Log;

import com.mirrordust.telecomlocate.entity.DataSet;
import com.mirrordust.telecomlocate.entity.Sample;
import com.mirrordust.telecomlocate.interf.OnAddOrUpdateSampleListener;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * CRUD for Sample.
 */

public class DataHelper {

    public static final String TAG = "DataHelper";

    // create & update
    public static void addOrUpdateSampleAsync(
            Realm realm, final Sample sample,
            final OnAddOrUpdateSampleListener onAddOrUpdateSampleListener) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(sample);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                onAddOrUpdateSampleListener.onAddOrUpdateSample();
                Log.e(TAG, "addSampleAsync success");
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // TODO: 2017/07/26/026 failed to save a new sample
                Log.e(TAG, "addSampleAsync error");
            }
        });
    }

    public static void updateDataSetExport(Realm realm, final long index, final boolean exported) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DataSet dataSet = getDataSet(realm, index);
                dataSet.setExported(exported);
            }
        });

    }

    public static void updateDataSetUpload(Realm realm, final long index, final boolean uploaded) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DataSet dataSet = getDataSet(realm, index);
                dataSet.setUploaded(uploaded);
            }
        });
    }

    // retrieve
    public static Sample getSample(Realm realm, String mid) {
        return realm.where(Sample.class).equalTo("mID", mid).findFirst();
    }

    public static DataSet getDataSet(Realm realm, String name) {
        return realm.where(DataSet.class).equalTo("name", name).findFirst();
    }

    public static DataSet getDataSet(Realm realm, long index) {
        return realm.where(DataSet.class).equalTo("index", index).findFirst();
    }

    public static long getDataSetIndexByName(Realm realm, String name) {
        DataSet dataSet = getDataSet(realm, name);
        return dataSet.getIndex();
    }

    public static String getDataSetNameByIndex(Realm realm, long index) {
        DataSet dataSet = getDataSet(realm, index);
        return dataSet.getName();
    }

    public static RealmResults<Sample> getSamplesByIndex(Realm realm, long index) {
        return realm.where(Sample.class).equalTo("index", index).findAll();
    }

    public static RealmResults<Sample> getSamplesByName(Realm realm, String name) {
        long index = getDataSetIndexByName(realm, name);
        return getSamplesByIndex(realm, index);
    }

    public static RealmResults<Sample> getNewSamples(Realm realm) {
        return getSamplesByIndex(realm, 0);
    }

    public static RealmResults<Sample> getAllSamples(Realm realm) {
        return realm.where(Sample.class).findAll();
    }

    public static RealmResults<DataSet> getAllDataSet(Realm realm) {
        return realm.where(DataSet.class).findAll();
    }

    public static long getMaxDataSetIndex(Realm realm) {
        return realm.where(DataSet.class).max("index").longValue();
    }

    public static void saveNewSamples(Realm realm, final long newIndex, final String newName) {
        final RealmResults<Sample> newSamples = getNewSamples(realm);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // first create a new data set
                DataSet dataSet = realm.createObject(DataSet.class);
                dataSet.setIndex(newIndex);
                dataSet.setName(newName);
                dataSet.setExported(false);
                dataSet.setUploaded(false);
                dataSet.setExportedPath("");
                dataSet.setDesc(
                        newSamples.size() == 0
                                ?
                                String.format("%s,%s,%s",
                                        newSamples.size(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis())
                                :
                                String.format("%s,%s,%s",
                                        newSamples.size(),
                                        newSamples.first().getTime(),
                                        newSamples.last().getTime()));
                // then update new samples' index from 0 to a new value
                for (Sample sample : newSamples) {
                    sample.setIndex(newIndex);
                    realm.copyToRealmOrUpdate(sample);
                }
            }
        });
    }

    // delete
    public static void deleteSample(Realm realm, String mid) {
        // delete a single sample
        final Sample sample = getSample(realm, mid);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                sample.deleteFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // TODO: 2017/07/26/026 delete a sample success
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // TODO: 2017/07/26/026 delete a sample error
            }
        });
    }

    public static void deleteSamples(Realm realm, final long index) {
        // delete a dataset's samples
        // when delete a set of samples, delete corresponding dataset
        final RealmResults<Sample> samples = getSamplesByIndex(realm, index);
        final DataSet dataSet = getDataSet(realm, index);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                samples.deleteAllFromRealm();
                if (index != 0) {
                    dataSet.deleteFromRealm();
                }
            }
        });
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                samples.deleteAllFromRealm();
//                dataSet.deleteFromRealm();
//            }
//        }, new Realm.Transaction.OnSuccess() {
//            @Override
//            public void onSuccess() {
//                Log.e(TAG, "删除成功");
//                // TODO: 2017/07/26/026 delete a data set success
//            }
//        }, new Realm.Transaction.OnError() {
//            @Override
//            public void onError(Throwable error) {
//                // TODO: 2017/07/26/026 delete a data set error
//            }
//        });
    }

    public static void deleteSamples(Realm realm, String name) {
        // delete a dataset's samples
        long index = getDataSetIndexByName(realm, name);
        deleteSamples(realm, index);
    }

    public static void deletaAllSamples(Realm realm) {
        // delete all samples
        // and delete all dataset except whose index = 0
        final RealmResults<Sample> allSamples = getAllSamples(realm);
        final RealmResults<DataSet> dataSets = realm
                .where(DataSet.class)
                .notEqualTo("index", 0)
                .findAll();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                allSamples.deleteAllFromRealm();
                dataSets.deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // TODO: 2017/07/26/026 delete all samples success
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                // TODO: 2017/07/26/026 delete all samples error
            }
        });
    }


}
