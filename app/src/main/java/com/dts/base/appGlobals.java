package com.dts.base;

import android.app.Application;
import android.content.Context;


public class appGlobals extends Application {

    public Context context;
    public String wsurl,sucursal,mac;
    public int emp,tienda,id,modo;
    public boolean restart=false;

}
