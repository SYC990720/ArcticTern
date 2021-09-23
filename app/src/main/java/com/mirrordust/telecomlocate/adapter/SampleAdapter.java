package com.mirrordust.telecomlocate.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.activity.SampleDetailActivity;
import com.mirrordust.telecomlocate.entity.Sample;
import com.mirrordust.telecomlocate.util.C;
import com.mirrordust.telecomlocate.util.Utils;

import io.realm.RealmResults;

/**
 * Created by LiaoShanhe on 2017/07/11/011.
 */

public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {

    private RealmResults<Sample> mSampleList;

    public SampleAdapter(RealmResults<Sample> samples) {
        mSampleList = samples;
    }

    public void addSample() {
//        notifyDataSetChanged();
        notifyItemInserted(0);
    }

    @Override
    public SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_sample_recycler_view, parent, false);
        return new SampleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SampleViewHolder holder, int position) {
        int size = mSampleList.size();
        int sampleIdx = size - position - 1;
        int showIdx = sampleIdx + 1;
        final Sample s = mSampleList.get(sampleIdx);
        holder.No.setText(Utils.index2String(showIdx));
        holder.BSNum.setText(Utils.baseStationNum2String(s.getBSList().size()));
        holder.sampleTime.setText(Utils.timestamp2LocalTime(s.getTime()));
        holder.sampleLocation.setText(Utils.latlng2String(s.getLatLng()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(holder.itemView.getContext(), SampleDetailActivity.class);
                intent.putExtra(C.ARG_SAMPLE_ID, s.getmID());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSampleList.size();
    }

    class SampleViewHolder extends RecyclerView.ViewHolder {

        TextView No;
        TextView BSNum;
        TextView sampleTime;
        TextView sampleLocation;

        SampleViewHolder(View itemView) {
            super(itemView);
            No = (TextView) itemView.findViewById(R.id.tv_No);
            BSNum = (TextView) itemView.findViewById(R.id.tv_bs_num);
            sampleTime = (TextView) itemView.findViewById(R.id.tv_time);
            sampleLocation = (TextView) itemView.findViewById(R.id.tv_loc);
        }
    }
}
