package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class Battery extends RealmObject {
    private double level;
    private double capacity;

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "level=" + level +
                ", capacity=" + capacity +
                '}';
    }
}
