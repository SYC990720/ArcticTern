package com.mirrordust.telecomlocate.fragment;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.activity.PredictionActivity;
import com.mirrordust.telecomlocate.activity.SampleDetailActivity;
import com.mirrordust.telecomlocate.entity.BaseStation;
import com.mirrordust.telecomlocate.entity.Sample;
import com.mirrordust.telecomlocate.livedata.MRdata;
import com.mirrordust.telecomlocate.model.DataHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class PredFragment extends Fragment {
    //预测的经纬坐标
    private List<Double> x;
    private List<Double> y;
    //实际的经纬坐标
    private double actualPosX;
    private double actualPosY;
    //lv中data
    private List<PreFragmentResult> data;
    private ListView lv;
    private PredictionAdapter arrayAdapter;

    //记录时间
    private long startTime;

    public PredFragment() {
        x = new ArrayList<>();
        y = new ArrayList<>();
        data = new ArrayList<>();
    }

    public static PredFragment newInstance() {
        return new PredFragment();
    }


    // 初始化Python环境
    public void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(getContext()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pred, container, false);
        //异步调用python预测算法
        PredictionAsyncTask predictionAsyncTask = new PredictionAsyncTask();
        predictionAsyncTask.execute();
        lv = (ListView) view.findViewById(R.id.lv);
        //为ListView设置Adapter来绑定数据
        updateData();
        arrayAdapter = new PredictionAdapter(data);
        lv.setAdapter(arrayAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private List<PreFragmentResult> updateData() {
        if (data.isEmpty()) {
            //获取起始时间
            startTime = new Date().getTime();
            for (int i = 0; i < 1; i++) {
                PreFragmentResult temp = new PreFragmentResult((String) this.getResources().getText(R.string.preAlgorithm1) + " Prediction",
                        "predicting...", actualPosX + ";" + actualPosY, "predicting...", "predicting...");
                data.add(temp);
//                data.add("random forest prediction:");
//                data.add("predicted position:predicting...");
//                data.add("actual position:"+actualPosX+";"+actualPosY);
            }
        } else {
            data.clear();
            // 获取间隔时间
            long gapTime = new Date().getTime() - startTime;
            for (int i = 0; i < 1; i++) {
                //计算误差
                float[] results = new float[1];
                Location.distanceBetween(x.get(i), y.get(i), actualPosX, actualPosY, results);
                //打包
                PreFragmentResult temp = new PreFragmentResult((String) this.getResources().getText(R.string.preAlgorithm1) + " Prediction",
                        x.get(i) + ";" + y.get(i), actualPosX + ";" + actualPosY, Float.toString(results[0]), Long.toString(gapTime));
                data.add(temp);
            }
        }
        return data;
    }

    public class PredictionAsyncTask extends AsyncTask<Void, Void, List<Double>> {
        JSONObject json;
        Python py;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SampleDetailActivity parentActivity = (SampleDetailActivity) getActivity();
            String mID = parentActivity.getSampleId();
            Sample sample = DataHelper.getSample(parentActivity.getRealm(), mID);
            BaseStation mbs = sample.getMBS();
            List<BaseStation> bslist = sample.getBSList();
            actualPosX = sample.getLatLng().getLongitude();
            actualPosY = sample.getLatLng().getLatitude();

            HashMap<String, String> parameterMap = new HashMap<>();
            initPython();
            py = Python.getInstance();
            parameterMap.put("ss_1", String.valueOf(mbs.getDbm()));
            json = new JSONObject();
            try {
                json.put("ss_1", String.valueOf(mbs.getDbm()));
                json.put("lac_1", String.valueOf(mbs.getLac()));
                json.put("cid_1", String.valueOf(mbs.getCid()));
                json.put("mnc_1", String.valueOf(mbs.getLon()));
                json.put("lon", String.valueOf(mbs.getLon()));
                json.put("lat", String.valueOf(mbs.getLat()));
                if (!bslist.isEmpty()) {
                    for (int i = 2; i < Math.min(bslist.size(), 8) + 2; i++) {
                        json.put("ss_" + i, String.valueOf(bslist.get(i - 2).getDbm()));
                        json.put("lac_" + i, String.valueOf(bslist.get(i - 2).getLac()));
                        json.put("cid_" + i, String.valueOf(bslist.get(i - 2).getCid()));
                        json.put("mnc_" + i, String.valueOf(bslist.get(i - 2).getMnc()));
                        json.put("lon_" + i, String.valueOf(bslist.get(i - 2).getLon()));
                        json.put("lat_" + i, String.valueOf(bslist.get(i - 2).getLat()));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected List<Double> doInBackground(Void... voids) {


            JSONArray jsonarray = new JSONArray();
            jsonarray.put(json);
            String parameterJson = jsonarray.toString();
            PyObject obj1 = py.getModule("prediction").callAttr("prediction_rf", new Kwarg("parameterJson", parameterJson));
            List<PyObject> result = obj1.asList();
            Double x = result.get(0).toDouble();
            Double y = result.get(1).toDouble();
            ArrayList<Double> xy = new ArrayList<>();
            xy.add(x);
            xy.add(y);
            return xy;
        }

        @Override
        protected void onPostExecute(List<Double> doubles) {
            super.onPostExecute(doubles);
            x.add(doubles.get(0));
            y.add(doubles.get(1));
            //更新数据及UI
            updateData();
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public class PredictionAdapter extends BaseAdapter {
        public List<PreFragmentResult> results;

        public PredictionAdapter() {
        }

        public PredictionAdapter(List<PreFragmentResult> temp) {
            results = temp;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return results.size();
        }

        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.fragment_pred_custom, parent, false);
            TextView title, prediction, actually, error, time;
//            ImageView i1;
            title = (TextView) row.findViewById(R.id.title);
            prediction = (TextView) row.findViewById(R.id.predictionValue);
            actually = (TextView) row.findViewById(R.id.actualValue);
            error = (TextView) row.findViewById(R.id.errorValue);
            time = (TextView) row.findViewById(R.id.timeValue);
            title.setText(this.results.get(position).title);
            prediction.setText(this.results.get(position).prediction);
            actually.setText(this.results.get(position).actually);
            error.setText(this.results.get(position).error);
            time.setText(this.results.get(position).time);
            return (row);
        }

    }

}
