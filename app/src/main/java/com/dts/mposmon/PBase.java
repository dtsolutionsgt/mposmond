package com.dts.mposmon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.dts.base.BaseDatos;
import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.appGlobals;
import com.dts.base.clsClasses;

public class PBase extends FragmentActivity {

    public int active;
    public SQLiteDatabase db;
    public BaseDatos Con;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    public String sql;

    public appGlobals gl;
    public MiscUtils mu;
    public DateUtils du;
    public clsClasses clsCls = new clsClasses();

    public int callback =0,mode;
    public int selid,selidx;
    public long fecha,stamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_base);

    }

    public void InitBase() {

        Con = new BaseDatos(this);
        opendb();
        ins=Con.Ins;upd=Con.Upd;

        gl=((appGlobals) this.getApplication());
        gl.context=this;

        mu=new MiscUtils(this,gl);
        du=new DateUtils();

        fecha=du.getActDateTime();stamp=du.getActDate();

        selid=-1;selidx=-1;
        callback =0;

    }

    //region Database

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
            active=1;
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
            active= 0;
        }
    }

    public void exec() {
        db.execSQL(sql);
    }

    public Cursor open() {
        Cursor dt;

        dt=Con.OpenDT(sql);
        try {
            dt.moveToFirst();
        } catch (Exception e) {
        }

        return dt;
    }

    //endregion

    //region Web service callback

    public void wsCallBack(int callmode, Boolean throwing, String errmsg) throws Exception {
        if (throwing) throw new Exception(errmsg);
    }


    //endregion

    //region Messages

    public void toast(String msg) {

        if (mu.emptystr(msg)) return;

        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void toastlong(String msg) {

        if (mu.emptystr(msg)) return;

        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void toast(double val) {
        this.toast(""+val);
    }

    public void msgbox(String msg){
        mu.msgbox(msg);
    }

    public void msgbox(int val){
        mu.msgbox(""+val);
    }

    public void msgbox(double val){
        mu.msgbox(""+val);
    }

    //endregion

    //region Activity Events

    @Override
    public void onResume() {
        opendb();
        super.onResume();
    }

    @Override
    public void onPause() {
        try {
            Con.close();   }
        catch (Exception e) { }
        active= 0;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //endregion

}
