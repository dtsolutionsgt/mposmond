package com.dts.mposmon;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dts.base.clsClasses;
import com.dts.classes.ExDialog;
import com.dts.ladapter.LA_D_orden_list;
import com.dts.ladapter.LA_Suc;
import com.dts.webservice.wsOpenDT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Sucursal extends PBase {

    private ListView listView;
    private EditText txtEmpresa, txtClave;
    private TextView lblTit;

    private wsOpenDT ws;

    private Runnable rnAfterCheck,rnList;

    private LA_Suc adapter;

    private ArrayList<clsClasses.clsSuc> items= new ArrayList<clsClasses.clsSuc>();

    private String emp,suc,nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sucursal);

        super.InitBase();

        listView = findViewById(R.id.listView);
        lblTit = findViewById(R.id.textView179);
        txtEmpresa = findViewById(R.id.editTextNumber);txtEmpresa.requestFocus();
        txtClave = findViewById(R.id.editTextNumber2);

        gl.restart=false;

        getParams();

        ws=new wsOpenDT(gl.wsurl);

        rnAfterCheck = new Runnable() {
            public void run() {
                checkCompany();
            }
        };

        rnList = new Runnable() {
            public void run() {
                listItems();
            }
        };

        setHandlers();
    }

    //region Events

    public void doConnect(View view) {
        if (!validaDatos()) return;

        toast("Conectando . . .");
        emp=txtEmpresa.getText().toString();
        sql="SELECT NOMBRE,CLAVE FROM P_EMPRESA WHERE EMPRESA="+emp;
        ws.execute(sql,rnAfterCheck);
    }

    public void doExit(View view) {
        finish();
    }

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsSuc item = (clsClasses.clsSuc) lvObj;

                adapter.setSelectedIndex(position);

                suc=""+item.codigo;
                nombre=item.nombre;

                msgAsk("APLICAR "+nombre);
            }
        });

    }

    //endregion

    //region Main

    private void listItems() {
        int rc;
        clsClasses.clsSuc item;

        items.clear();

        try {
            rc=ws.openDTCursor.getCount();
            if (rc==0) return;

            ws.openDTCursor.moveToFirst();
            while (!ws.openDTCursor.isAfterLast()) {
                item=clsCls.new clsSuc();
                item.codigo=ws.openDTCursor.getInt(0);
                item.nombre=ws.openDTCursor.getString(1);
                items.add(item);

                ws.openDTCursor.moveToNext();
            }

            adapter = new LA_Suc(this, this,items);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void checkCompany() {
        String clave;
        int rc;

        try {
            rc=ws.openDTCursor.getCount();
            if (rc==0) return;

            ws.openDTCursor.moveToFirst();
            clave=ws.openDTCursor.getString(1);

            if (!clave.equalsIgnoreCase(txtClave.getText().toString())) {
                 msgbox("El código de la empresa o la clave son incorrectos");return;
            }

            toast("Obteniendo lista de sucursales . . .");
            emp=txtEmpresa.getText().toString();
            sql="SELECT CODIGO_SUCURSAL,DESCRIPCION FROM P_SUCURSAL WHERE EMPRESA="+emp+" ORDER BY DESCRIPCION";
            ws.execute(sql,rnList);

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void saveSettings() {
        FileWriter wfile=null;
        BufferedWriter writer=null;
        String fname;

        try {

            gl.emp=Integer.parseInt(emp);
            gl.tienda=Integer.parseInt(suc);
            gl.sucursal=nombre;

            fname = Environment.getExternalStorageDirectory().getPath() + "/mposmonitor.txt";
            wfile = new FileWriter(fname, false);
            writer = new BufferedWriter(wfile);
            writer.write(gl.wsurl);writer.write("\r\n");
            writer.write(""+gl.emp);writer.write("\r\n");
            writer.write(""+gl.tienda);writer.write("\r\n");
            writer.write(gl.sucursal);writer.write("\r\n");

            writer.close();writer = null;wfile = null;

            gl.restart=true;
            finish();
        } catch (Exception e) {
            String ss=e.getMessage();
        }
    }

    //endregion

    //region Aux

    public void getParams() {
        String line;
        gl.wsurl = "http://52.41.114.122/MPosWS_QA/Mposws.asmx";

        try {
            File file1 = new File(Environment.getExternalStorageDirectory(), "/mposmonitor.txt");

            FileInputStream fIn = new FileInputStream(file1);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

            line = myReader.readLine();gl.wsurl=line;
            line = myReader.readLine();gl.emp=Integer.parseInt(line);
            line = myReader.readLine();gl.tienda=Integer.parseInt(line);

            try {
                line = myReader.readLine();gl.sucursal=line;
            } catch (Exception e) {
                gl.sucursal="";
            }

            myReader.close();

        } catch (Exception e) {
            gl.wsurl = "http://52.41.114.122/MPosWS_QA/Mposws.asmx";
            toastlong("Archivo de configuracion no existe o incompleto\n"+gl.wsurl);
        }

    }

    private boolean validaDatos(){
         try {
            if (txtEmpresa.getText().toString().isEmpty()){
                msgbox("Debe ingresar la empresa para recibir los datos");
                txtEmpresa.requestFocus();
                return  false;
            }

            if (txtClave.getText().toString().isEmpty()){
                msgbox("Debe ingresar la clave para recibir los datos");
                txtClave.requestFocus();
                return  false;
            }

            return  true;
        } catch (Exception ex){
            msgbox("Ocurrió un error validando los datos " + ex.getMessage());return  false;
        }

    }

    //endregion

    //region Dialogs

    private void msgAsk(String msg) {
        ExDialog dialog = new ExDialog(this);
        dialog.setTitle("Sucursal");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saveSettings();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

    //endregion

    //region Activity Events


    //endregion


}