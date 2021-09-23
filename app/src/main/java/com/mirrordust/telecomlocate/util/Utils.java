package com.mirrordust.telecomlocate.util;

import android.util.Log;
import com.mirrordust.telecomlocate.entity.BaseStation;
import com.mirrordust.telecomlocate.entity.LatLng;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by LiaoShanhe on 2017/07/25/025.
 */

public class Utils {

    public static String index2String(int index) {
        return String.format("No.%s", index);
    }

    public static String baseStationNum2String(int number) {
        return String.format("[%s BS]", number);
    }

    public static String timestamp2LocalTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(milliseconds));
    }

    public static String timestamp2ShortLocalTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm", Locale.getDefault());
        return sdf.format(new Date(milliseconds));
    }

    public static String latlng2String(LatLng latLng) {
        return String.format("Lon: %s\nLat: %s", latLng.getLongitude(), latLng.getLatitude());
    }

    public static String dataSetDesc2String(String desc) {
        String[] ss = desc.split(",");
        String size = ss[0], startTime = ss[1], endTime = ss[2];
        long st = Long.parseLong(startTime), et = Long.parseLong(endTime);
        return String.format("[%s], %s ~ %s",
                size, timestamp2LocalTime(st), timestamp2LocalTime(et));
    }

    public static String dataSetDesc2FileSuffix(String desc) {
        String[] ss = desc.split(",");
        String size = ss[0], startTime = ss[1], endTime = ss[2];
        long st = Long.parseLong(startTime), et = Long.parseLong(endTime);
        return String.format("%s_%s_%s",
                size, timestamp2ShortLocalTime(st), timestamp2ShortLocalTime(et));
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        } else {
            return reference;
        }
    }

    public static String[] getFieldsValues(Object obj) {
        String sep = ",";

        if (obj == null)
            return null;

        Field[] fields = obj.getClass().getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        List<Object> fieldValues = new ArrayList<>();
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            fieldNames.add(field.getName());
            try {
                fieldValues.add(field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i != 0) {
                name.append(sep);
                value.append(sep);
            }
            name.append(fieldNames.get(i));
            value.append(fieldValues.get(i).toString());
        }
        return new String[]{name.toString(), value.toString()};
    }

}
