package com.dts.webservice;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

public class wsOpenDT  extends wsBase {

    public Cursor openDTCursor;

    private ArrayList<String> results=new ArrayList<String>();

    private String command;
    private int odt_rows,odt_cols;

    public wsOpenDT(String Url) {
        super(Url);
    }

    public void execute(String commandlist, Runnable afterfinish) {
        command=commandlist;
        super.execute(afterfinish);
    }

    @Override
    protected void wsExecute() {
        openDT();
    }

    public void openDT() {
        String str;
        int rc;

        String METHOD_NAME = "getDT";

        results.clear();

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");param.setValue(command);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapObject resSoap =(SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc=resSoap.getPropertyCount()-1;

            for (int i = 0; i < rc; i++) {
                str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                if (i==0) {
                    if (!str.equalsIgnoreCase("#")) throw new Exception(str);
                } else {
                    results.add(str);

                    if (i==1) odt_rows = Integer.parseInt(str);
                    if (i==2) odt_cols=Integer.parseInt(str);
                }
            }

            createCursor();

        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            createVoidCursor();
        }

    }

    private void createCursor() {
        String[] mRow = new String[odt_cols];
        MatrixCursor cursor = new MatrixCursor(mRow);
        int pos;
        String ss;

        try {
            createVoidCursor();
            if (odt_rows==0) return;

            pos=2;
            for (int i = 0; i <odt_rows; i++) {
                for (int j = 0; j <odt_cols; j++) {
                    try {
                        ss=results.get(pos);
                        if (ss.equalsIgnoreCase("anyType{}")) ss = "";
                        mRow[j]=ss;
                    } catch (Exception e) {
                        mRow[j]="";
                    }
                    pos++;
                }
                cursor.addRow(mRow);
            }

            int rc=cursor.getCount();

            openDTCursor=cursor;
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
            createVoidCursor();
        }
    }

    private void createVoidCursor() {
        String[] mRow = new String[odt_cols];
        MatrixCursor cursor = new MatrixCursor(mRow);

        try {
            openDTCursor=cursor;
        } catch (Exception e) {
            errflag=true;
            error=e.getMessage();
        }
    }

}
