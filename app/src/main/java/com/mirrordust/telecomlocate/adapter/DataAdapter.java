package com.mirrordust.telecomlocate.adapter;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.entity.DataSet;
import com.mirrordust.telecomlocate.interf.DataContract;
import com.mirrordust.telecomlocate.util.Utils;

import io.realm.RealmResults;

/**
 * Created by LiaoShanhe on 2017/07/31/031.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    public static final String TAG = "DataAdapter";

    private DataContract.View mDataView;
    private RealmResults<DataSet> mDataSetList;

    public DataAdapter(DataContract.View dataView, RealmResults<DataSet> dataSetList) {
        mDataView = dataView;
        mDataSetList = dataSetList;
    }

    public void onDataSetRemove() {
        notifyDataSetChanged();
    }

    public void onDataSetUpdate() {
        notifyDataSetChanged();
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.data_recycler_view, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataViewHolder holder, int position) {
        final DataSet dataSet = mDataSetList.get(position);
        holder.dataSetName.setText(dataSet.getName());
        holder.dataSetDesc.setText(Utils.dataSetDesc2String(dataSet.getDesc()));
        if (dataSet.getIndex() == 0) {
            holder.exported.setVisibility(View.GONE);
            holder.uploaded.setVisibility(View.GONE);
            holder.options.setVisibility(View.GONE);
        } else {
            holder.exported.setVisibility(dataSet.isExported() ? View.VISIBLE : View.GONE);
            holder.uploaded.setVisibility(dataSet.isUploaded() ? View.VISIBLE : View.GONE);
            holder.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), v);
                    popupMenu.inflate(R.menu.menu_data_set);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_export:
                                    mDataView.checkExported(dataSet.isExported(),
                                            dataSet.getIndex(), dataSet.getName(),
                                            dataSet.getDesc());
                                    return true;
                                case R.id.action_upload:
                                    mDataView.checkUploaded(dataSet.isExported(),
                                            dataSet.isUploaded(), dataSet.getIndex(),
                                            dataSet.getName(), dataSet.getDesc());
                                    return true;
                                case R.id.action_delete_data_set:
                                    mDataView.showConfirmDeleteDialog(
                                            dataSet.getIndex(),dataSet.getName());
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
            holder.dataSetInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2017/07/31/031 dataset详细
                    Log.e(TAG, "dataset详细");
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mDataSetList.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout dataSetInfo;
        TextView dataSetName;
        ImageView exported;
        ImageView uploaded;
        TextView dataSetDesc;
        TextView options;

        DataViewHolder(View itemView) {
            super(itemView);
            dataSetInfo = (RelativeLayout) itemView.findViewById(R.id.data_set_info);
            dataSetName = (TextView) itemView.findViewById(R.id.tv_data_set_name);
            exported = (ImageView) itemView.findViewById(R.id.iv_exported);
            uploaded = (ImageView) itemView.findViewById(R.id.iv_uploaded);
            dataSetDesc = (TextView) itemView.findViewById(R.id.tv_data_set_desc);
            options = (TextView) itemView.findViewById(R.id.tv_options);
        }
    }
}
