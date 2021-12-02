package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsD_ordenObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM D_orden";
    private String sql;
    public ArrayList<clsClasses.clsD_orden> items= new ArrayList<clsClasses.clsD_orden>();

    public clsD_ordenObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        cont=context;
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
        count = 0;
    }

    public void reconnect(BaseDatos dbconnection, SQLiteDatabase dbase) {
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
    }

    public void add(clsClasses.clsD_orden item) {
        addItem(item);
    }

    public void update(clsClasses.clsD_orden item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsD_orden item) {
        deleteItem(item);
    }

    public void delete(int id) {
        deleteItem(id);
    }

    public void fill() {
        fillItems(sel);
    }

    public void fill(String specstr) {
        fillItems(sel+ " "+specstr);
    }

    public void fillSelect(String sq) {
        fillItems(sq);
    }

    public clsClasses.clsD_orden first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsD_orden item) {

        ins.init("D_orden");

        ins.add("CODIGO_ORDEN",item.codigo_orden);
        ins.add("COREL",item.corel);
        ins.add("EMPRESA",item.empresa);
        ins.add("CODIGO_RUTA",item.codigo_ruta);
        ins.add("TIPO",item.tipo);
        ins.add("NUM_ORDEN",item.num_orden);
        ins.add("ESTADO",item.estado);
        ins.add("FECHA_INICIO",item.fecha_inicio);
        ins.add("FECHA_FIN",item.fecha_fin);
        ins.add("TIEMPO_LIMITE",item.tiempo_limite);
        ins.add("TIEMPO_TOTAL",item.tiempo_total);
        ins.add("NOTA",item.nota);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsD_orden item) {

        upd.init("D_orden");

        upd.add("COREL",item.corel);
        upd.add("EMPRESA",item.empresa);
        upd.add("CODIGO_RUTA",item.codigo_ruta);
        upd.add("TIPO",item.tipo);
        upd.add("NUM_ORDEN",item.num_orden);
        upd.add("ESTADO",item.estado);
        upd.add("FECHA_INICIO",item.fecha_inicio);
        upd.add("FECHA_FIN",item.fecha_fin);
        upd.add("TIEMPO_LIMITE",item.tiempo_limite);
        upd.add("TIEMPO_TOTAL",item.tiempo_total);
        upd.add("NOTA",item.nota);

        upd.Where("(CODIGO_ORDEN="+item.codigo_orden+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsD_orden item) {
        sql="DELETE FROM D_orden WHERE (CODIGO_ORDEN="+item.codigo_orden+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM D_orden WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {

        Cursor dt;
        clsClasses.clsD_orden item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsD_orden();

            item.codigo_orden=dt.getInt(0);
            item.corel=dt.getString(1);
            item.empresa=dt.getInt(2);
            item.codigo_ruta=dt.getInt(3);
            item.tipo=dt.getInt(4);
            item.num_orden=dt.getString(5);
            item.estado=dt.getInt(6);
            item.fecha_inicio=dt.getLong(7);
            item.fecha_fin=dt.getLong(8);
            item.tiempo_limite=dt.getInt(9);
            item.tiempo_total=dt.getInt(10);
            item.nota=dt.getString(11);

            items.add(item);

            dt.moveToNext();
        }

        if (dt!=null) dt.close();

    }

    public int newID(String idsql) {
        Cursor dt=null;
        int nid;

        try {
            dt=Con.OpenDT(idsql);
            dt.moveToFirst();
            nid=dt.getInt(0)+1;
        } catch (Exception e) {
            nid=1;
        }

        if (dt!=null) dt.close();

        return nid;
    }

    public String addItemSql(clsClasses.clsD_orden item) {

        ins.init("D_orden");

        ins.add("CODIGO_ORDEN",item.codigo_orden);
        ins.add("COREL",item.corel);
        ins.add("EMPRESA",item.empresa);
        ins.add("CODIGO_RUTA",item.codigo_ruta);
        ins.add("TIPO",item.tipo);
        ins.add("NUM_ORDEN",item.num_orden);
        ins.add("ESTADO",item.estado);
        ins.add("FECHA_INICIO",item.fecha_inicio);
        ins.add("FECHA_FIN",item.fecha_fin);
        ins.add("TIEMPO_LIMITE",item.tiempo_limite);
        ins.add("TIEMPO_TOTAL",item.tiempo_total);
        ins.add("NOTA",item.nota);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsD_orden item) {

        upd.init("D_orden");

        upd.add("COREL",item.corel);
        upd.add("EMPRESA",item.empresa);
        upd.add("CODIGO_RUTA",item.codigo_ruta);
        upd.add("TIPO",item.tipo);
        upd.add("NUM_ORDEN",item.num_orden);
        upd.add("ESTADO",item.estado);
        upd.add("FECHA_INICIO",item.fecha_inicio);
        upd.add("FECHA_FIN",item.fecha_fin);
        upd.add("TIEMPO_LIMITE",item.tiempo_limite);
        upd.add("TIEMPO_TOTAL",item.tiempo_total);
        upd.add("NOTA",item.nota);

        upd.Where("(CODIGO_ORDEN="+item.codigo_orden+")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

