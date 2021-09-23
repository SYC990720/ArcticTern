package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/24/024.
 */

public class DataSet extends RealmObject {
    private String name;
    private long index;
    private boolean exported;
    private boolean uploaded;
    private String desc;
    private String exportedPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getExportedPath() {
        return exportedPath;
    }

    public void setExportedPath(String exportedPath) {
        this.exportedPath = exportedPath;
    }

    @Override
    public String toString() {
        return "DataSet{" +
                "name='" + name + '\'' +
                ", index=" + index +
                ", exported=" + exported +
                ", uploaded=" + uploaded +
                ", desc='" + desc + '\'' +
                ", exportedPath='" + exportedPath + '\'' +
                '}';
    }
}
