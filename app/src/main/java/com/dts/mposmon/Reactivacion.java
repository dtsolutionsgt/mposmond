package com.dts.mposmon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.dts.base.clsClasses;
import com.dts.classes.ExDialog;
import com.dts.classes.clsD_ordenObj;
import com.dts.ladapter.LA_D_orden_list;
import com.dts.services.srvCommit;

public class Reactivacion extends PBase {

    private ListView listView;
    private TextView lblTit;

    private LA_D_orden_list adapter;
    private clsD_ordenObj D_ordenObj;

    private int itemid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactivacion);

        super.InitBase();

        listView = findViewById(R.id.listView);
        lblTit = findViewById(R.id.textView179);

        D_ordenObj=new clsD_ordenObj(this,Con,db);

        setHandlers();

        if (gl.modo==0) lblTit.setText("ORDENES ENTREGADOS");else lblTit.setText("ORDENES ANULADOS");

        listItems();

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsD_orden item = (clsClasses.clsD_orden) lvObj;

                adapter.setSelectedIndex(position);

                itemid=item.codigo_orden;

                msgAskActiv("ACTIVAR ORDEN");
            }
        });

    }

    //endregion

    //region Main

    private void listItems() {
        long fi=du.addDays(du.getActDate(),-1);

        try {
            if (gl.modo==0) {
                sql="WHERE (Estado=3) AND (FECHA_INICIO>="+fi+") ORDER BY FECHA_INICIO DESC";
            } else {
                sql="WHERE (Estado=99) AND (FECHA_INICIO>="+fi+") ORDER BY FECHA_INICIO DESC";
            }
            D_ordenObj.fill(sql);

            adapter = new LA_D_orden_list(this, this, D_ordenObj.items);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void activarOrden() {
        String ss="",ssq="";

        try {
            ss="UPDATE D_ORDEN SET ESTADO=1,FECHA_FIN=0,TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+itemid;
            ssq="UPDATE D_ORDEN SET ESTADO=1,FECHA_FIN='20000101 00:00:00',TIEMPO_TOTAL=0 WHERE CODIGO_ORDEN="+itemid;

            db.execSQL(ss);

            Intent intent = new Intent(this, srvCommit.class);
            intent.putExtra("URL",gl.wsurl);
            intent.putExtra("command",ssq);
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

    private void msgAskActiv(String msg) {

        ExDialog dialog = new ExDialog(this);
        dialog.setTitle("ACTIVACION");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activarOrden();
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
            D_ordenObj.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

    //endregion

}