package com.mirrordust.telecomlocate.pojo;

/**
 * Created by LiaoShanhe on 2017/07/12/012.
 */

public class DetailItem {

    private String title;
    private String value;

    public DetailItem() {
    }

    public DetailItem(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
