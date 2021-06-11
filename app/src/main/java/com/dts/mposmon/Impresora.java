package com.dts.mposmon;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Impresora extends PBase {

    private EditText txtMac;
    private CheckBox cbImp;

    private String mac;
    private int imp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impresora);

        super.InitBase();

        cbImp = findViewById(R.id.checkBox);cbImp.setText("USAR IMPRESORA   ");
        txtMac = findViewById(R.id.editTextTextPersonName);txtMac.requestFocus();

        loadItem();
    }

    //region Events

    public void doSave(View view) {
        save();
    }

    public void doExit(View view) {
        finish();
    }

    //endregion

    //region Main

    private void loadItem() {
        String line;

        try {
            File file1 = new File(Environment.getExternalStorageDirectory(), "/mposmonimp.txt");

            FileInputStream fIn = new FileInputStream(file1);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

            mac="";imp=0;
            line = myReader.readLine();if (line.equalsIgnoreCase("1")) imp=1;
            line = myReader.readLine();mac=line;

            myReader.close();
        } catch (Exception e) {
            mac="";imp=0;
        }

        cbImp.setChecked(imp==1);
        txtMac.setText(mac);
    }

    private void save() {
        FileWriter wfile=null;
        BufferedWriter writer=null;
        String fname;

        try {

            if (cbImp.isChecked()) imp=1;else imp=0;
            mac=""+txtMac.getText().toString();

            if (imp==1 && mac.isEmpty()) {
                msgbox("MAC INCORRECTO");return;
            }

            fname = Environment.getExternalStorageDirectory().getPath() + "/mposmonimp.txt";
            wfile = new FileWriter(fname, false);
            writer = new BufferedWriter(wfile);
            writer.write(""+imp);writer.write("\r\n");
            writer.write(mac);writer.write("\r\n");
            writer.close();writer = null;wfile = null;

            toastlong("CONFIGURACIÓN DE IMPRESORA GUARDADA");

            gl.restart=true;
            finish();
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

    //endregion

    //region Aux


    //endregion

    //region Dialogs


    //endregion

    //region Activity Events


    //endregion


}