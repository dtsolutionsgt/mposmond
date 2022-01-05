package com.dts.mposmon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.RelativeLayout;
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

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Detalle extends PBase {

    private ListView listView;
    private TextView lblNum;
    private RelativeLayout relBot,relWait;

    private LA_D_ordend adapter;
    private clsD_ordendObj D_ordendObj;
    private clsClasses.clsD_orden item;

    private File file=new File(Environment.getExternalStorageDirectory()+"/print.txt");

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        super.InitBase();

        listView = findViewById(R.id.listView);
        lblNum = findViewById(R.id.textView179);
        relBot = findViewById(R.id.relbot);relBot.setVisibility(View.VISIBLE);
        relWait = findViewById(R.id.relwait);relWait.setVisibility(View.INVISIBLE);

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

    private boolean tieneImpresion() {

        String line;
        int imp=0;

        try {

            File file1 = new File(Environment.getExternalStorageDirectory(), "/mposmonimp.txt");

            FileInputStream fIn = new FileInputStream(file1);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

            line = myReader.readLine();if (line.equalsIgnoreCase("1")) imp=1;
            line = myReader.readLine();gl.mac=line;

            myReader.close();

        } catch (Exception e) {
            toastlong("Impresion "+e.getMessage()); gl.mac="";imp=0;
        }

        return imp==1;
    }

    public void doPrint(View view) {
        if (!tieneImpresion()) return;
        imprimir();
        try {
            waitprint();
        } catch (Exception e) {}
    }

    public void doDel(View view) {
        file.delete();
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

    private void imprimir() {
        try {

            String filePath = Environment.getExternalStorageDirectory()+"/print.txt";
            File file = new File(filePath);

            if(file.exists()){

                if (gl.mac!=null){
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.dts.epsonprint");
                    intent.putExtra("mac","BT:"+gl.mac);
                    intent.putExtra("fname", Environment.getExternalStorageDirectory()+"/print.txt");
                    intent.putExtra("askprint",1);
                    intent.putExtra("copies",1);
                    this.startActivity(intent);
                }else{
                    toastlong("No se obtuvo el mac adress de la impresora");
                }

            }else{
                toastlong("El archivo de impresión no se generó");
            }

        } catch (Exception e) {
            toastlong("El controlador de Epson TM BT no está instalado");
        }
    }

    private void waitprint() {

        relBot.setVisibility(View.INVISIBLE);
        relWait.setVisibility(View.VISIBLE);

        try {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!file.exists()) {
                        timer.cancel();
                        aplicaEstado(3);
                        return;
                    }
                }
            },0, 1000);
        } catch (Exception e) {}

    }

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