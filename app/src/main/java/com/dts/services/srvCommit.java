package com.dts.services;

import android.content.Intent;
import android.os.Environment;

import com.dts.webservice.wsCommit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class srvCommit extends srvBase {

    private wsCommit wscom;
    private Runnable rnErrorCheck;
    private String command,orderid;
    private String orddir= Environment.getExternalStorageDirectory().getPath() + "/mposmdisp";

    @Override
    public void execute() {

        rnErrorCheck = new Runnable() {
            public void run() {
                if (wscom.errflag) savePendingCommand();
            }
        };

        wscom =new wsCommit(URL);
        wscom.execute(command,rnErrorCheck);
    }

    @Override
    public void loadParams(Intent intent) {
        command = intent.getStringExtra("command");
        orderid = intent.getStringExtra("orderid");
    }

    private void savePendingCommand() {
        FileWriter wfile=null;
        BufferedWriter writer=null;
        String fname;

        if (orderid.isEmpty()) return;

        try {
            fname = orddir + "/_" + orderid + ".txt";
            wfile = new FileWriter(fname, false);
            writer = new BufferedWriter(wfile);
            writer.write(command);
            writer.write("\r\n");
            writer.close();writer = null;wfile = null;
        } catch (Exception e) {
            String ss=e.getMessage();
        }
    }

}
