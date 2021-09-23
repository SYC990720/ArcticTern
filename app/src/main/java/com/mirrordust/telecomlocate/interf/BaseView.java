package com.mirrordust.telecomlocate.interf;

import android.content.Context;

/**
 * Created by LiaoShanhe on 2017/07/27/027.
 */

public interface BaseView<T> {

    void setPresenter(T presenter);

    Context getContext();

}
