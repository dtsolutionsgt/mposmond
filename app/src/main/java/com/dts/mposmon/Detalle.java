package com.dts.mposmon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.dts.base.clsClasses;
import com.dts.classes.clsD_ordenObj;
import com.dts.classes.clsD_ordendObj;
import com.dts.ladapter.LA_D_ordend;
import com.dts.services.srvCommit;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Detalle extends PBase {

    private ListView listView;
    private TextView lblNum;

    private LA_D_ordend adapter;
    private clsD_ordendObj D_ordendObj;
    private clsClasses.clsD_orden item;

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        super.InitBase();

        listView = (ListView) findViewById(R.id.listView);
        lblNum=findViewById(R.id.textView179);

        id=gl.id;

        D_ordendObj=new clsD_ordendObj(this,Con,db);

        loadItem();

        setHandlers();

        listItems();

    }

    //region Events

    public void doPrep(View view) {
        aplicaEstado(1);
    }

    public void doListo(View view) {
        aplicaEstado(2);
    }

    public void doEntreg(View view) {
        aplicaEstado(3);
    }

    public void doAnul(View view) {
        msgAskAnul("Anular orden");
    }

    public void doExit(View view) {
        finish();
    }

    private void setHandlers() {

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsD_ordend item = (clsClasses.clsD_ordend)lvObj;

                adapter.setSelectedIndex(position);
            };
        });
    }

    //endregion

    //region Main

    private void listItems() {

        try {
            D_ordendObj.fill("WHERE (CODIGO_ORDEN="+id+")");

            adapter=new LA_D_ordend(this,this,D_ordendObj.items);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void loadItem() {
        try {
            clsD_ordenObj D_ordenObj=new clsD_ordenObj(this,Con,db);
            D_ordenObj.fill("WHERE (CODIGO_ORDEN="+id+")");
            item=D_ordenObj.first();

            lblNum.setText("ORDEN "+item.num_orden.toUpperCase());


        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void aplicaEstado(int est) {
        String ss="",ssq="",fs=du.univfechahora(du.getActDateTime());
        long fi,ff=du.getActDateTime();
        int tt;

        try {
            switch (est) {
                case 1: // Preparacion
                    ss="UPDATE D_ORDEN SET ESTADO=1,FECHA_FIN=0,TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    ssq="UPDATE D_ORDEN SET ESTADO=1,FECHA_FIN='20000101 00:00:00',TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    break;

                case 2: // Listo
                    ss="UPDATE D_ORDEN SET ESTADO=2,FECHA_FIN=0,TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    ssq="UPDATE D_ORDEN SET ESTADO=2,FECHA_FIN='20000101 00:00:00',TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    break;

                case 3: // Entregado
                    fi=item.fecha_inicio;tt=du.getmindif(ff,fi);
                    ss="UPDATE D_ORDEN SET ESTADO=3,FECHA_FIN="+ff+",TIEMPO_TOTAL="+tt+" WHERE CODIGO_ORDEN="+id;
                    ssq="UPDATE D_ORDEN SET ESTADO=3,FECHA_FIN='"+fs+"',TIEMPO_TOTAL="+tt+" WHERE CODIGO_ORDEN="+id;
                    break;

                case 4: // Anulacion
                    ss="UPDATE D_ORDEN SET ESTADO=99,FECHA_FIN=0,TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    ssq="UPDATE D_ORDEN SET ESTADO=99,FECHA_FIN='20000101 00:00:00',TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+id;
                    break;
            }

            db.execSQL(ss);

            Intent intent = new Intent(this, srvCommit.class);
            intent.putExtra("URL",gl.wsurl);
            intent.putExtra("command",ssq);
            intent.putExtra("orderid",""+id);
            startService(intent);

            finish();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Aux


    //endregion

    //region Dialogs

    private void msgAskAnul(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Anulacion");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                aplicaEstado(4);
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }



    //endregion

    //region Activity Events

    @Override
    public void onResume() {
        super.onResume();
        try {
            D_ordendObj.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

    //endregion


}