package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

public class Barometric extends RealmObject {
    private double pressure;

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return "Barometric{" +
                "pressure=" + pressure +
                '}';
    }
}
