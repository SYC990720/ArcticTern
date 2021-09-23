package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class Geomagnetism extends RealmObject {
    private double x;
    private double y;
    private double z;
    private double alpha;
    private double beta;
    private double gamma;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public String toString() {
        return "Geomagnetism{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", alpha=" + alpha +
                ", beta=" + beta +
                ", gamma=" + gamma +
                '}';
    }
}
