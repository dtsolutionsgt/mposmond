package com.dts.mposmon;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dts.base.clsClasses;
import com.dts.classes.ExDialog;
import com.dts.classes.clsD_ordenObj;
import com.dts.ladapter.LA_D_orden;
import com.dts.services.srvCommit;
import com.dts.services.startJobService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;

public class MainActivity extends PBase {

    private GridView gridView;
    private ImageView imgCon;
    private TextView lblErr,lblHora,lble,lblp,lblr;

    private clsD_ordenObj D_ordenObj;

    private LA_D_orden adapter;

    public ArrayList<String> corels= new ArrayList<String>();

    private String params,errstr,path;
    private boolean idle=true;

    private TimerTask ptask;
    private int period=10000,delay=50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grantPermissions();
    }

    private void startApplication() {

        try {
            super.InitBase();

            gridView = findViewById(R.id.gridView);
            imgCon=findViewById(R.id.imageView3);imgCon.setVisibility(View.INVISIBLE);
            lblErr=findViewById(R.id.textView3);lblErr.setVisibility(View.INVISIBLE);
            lblHora=findViewById(R.id.textView);
            lble=findViewById(R.id.textView179);
            lblp=findViewById(R.id.textView181);
            lblr=findViewById(R.id.textView182);

            if (!initSession()) return;

            startJobService.startService(this,params);

            setHandlers();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    //region Events

    public void doMenu(View view) {
        showItemMenu();
    }

    private void setHandlers() {

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object lvObj = gridView.getItemAtPosition(position);
                clsClasses.clsD_orden item = (clsClasses.clsD_orden)lvObj;

                adapter.setSelectedIndex(position);

                gl.id=item.codigo_orden;
                startActivity(new Intent(MainActivity.this,Detalle.class));
            };
        });
    }

    //endregion

    //region Main

    private void processTimer() {
        try {
            lblHora.setText(du.shora(du.getActDateTime()));
            if (idle) recibeOrdenes();
        } catch (Exception e) {
            toast(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void listItems() {
        clsClasses.clsD_orden item;
        long fi,ff=du.getActDateTime();
        int tt,cent,cprep,cretr;
        double pos;

        cent=0;cprep=0;cretr=0;

        try {

            D_ordenObj.fill("WHERE (FECHA_INICIO>"+du.getActDate()+") AND (ESTADO=3)");
            cent=D_ordenObj.count;

            D_ordenObj.fill("WHERE ESTADO<3 ORDER BY ESTADO DESC,FECHA_INICIO");
            cprep=D_ordenObj.count;

            for (int i = 0; i <D_ordenObj.count; i++) {
                item=D_ordenObj.items.get(i);
                item.tiempo_total=du.getmindif(ff,item.fecha_inicio);
                if (item.tiempo_total>item.tiempo_limite) cretr++;
                if (item.tiempo_limite<=0) item.tiempo_limite=3;
                pos=item.tiempo_total;pos=pos/item.tiempo_limite;
                item.color=getItemColor(pos,item.estado);
            }

            adapter=new LA_D_orden(this,this,D_ordenObj.items);
            gridView.setAdapter(adapter);
        } catch (Exception e) {
            toast(e.getMessage());
        }

        lble.setText(""+cent);lblp.setText(""+cprep);lblr.setText(""+cretr);

    }

    private boolean initSession() {
        try {

            path = Environment.getExternalStorageDirectory().getPath() + "/mposmdisp";

            try {
                String orddir=Environment.getExternalStorageDirectory().getPath() + "/mposmdisp";
                File directory = new File(orddir);directory.mkdirs();
            } catch (Exception e) {}

            try {
                String errdir=Environment.getExternalStorageDirectory().getPath() + "/mposmdisp/error";
                File directory = new File(errdir);directory.mkdirs();
            } catch (Exception e) {}

            initTimer();

            getParams();
            if (gl.wsurl.isEmpty()) return false;

            params =gl.wsurl+"#"+gl.emp+"#"+gl.tienda;

            D_ordenObj=new clsD_ordenObj(this,Con,db);

            return true;
        } catch (Exception e) {
            toast(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());return false;
        }
    }

    private void initTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(ptask=new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public synchronized void run() {
                        processTimer();
                    }
                });
            }
        }, delay, period);
    }

    //endregion

    //region Ordenes

    private void recibeOrdenes() {
        int pp;
        String fname;

        corels.clear();

        try {
            File directory = new File(path);
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                fname=files[i].getName();
                pp=fname.indexOf(".txt");
                if (pp>0) agregaOrden(path+"/"+fname,path+"/error/"+fname,fname);
            }
        } catch (Exception e) {
            toast("recibeOrdenes1 : "+e.getMessage());
        }

        try {

            for (int i = 0; i <corels.size(); i++) {
                try {
                    sql="UPDATE D_ORDEN SET ESTADO=0 WHERE CODIGO_ORDEN="+corels.get(i);
                    db.execSQL(sql);
                } catch (Exception e) {}
            }

            D_ordenObj.fill("WHERE (ESTADO=0)");
            if (D_ordenObj.count>0) enviaConfirmacion();
            D_ordenObj.items.clear();
        } catch (Exception e) {
            toast("recibeOrdenes2 : "+e.getMessage());
        }

        listItems();
    }

    public boolean agregaOrden(String fname,String ename,String cor) {
        File file=null;
        BufferedReader br=null;
        ArrayList<String> items=new ArrayList<String>();
        String sql,fi;
        int lim;
        boolean flag=true;

        cor=cor.replace(".txt","");corels.add(cor);

        try {
            sql="SELECT CODIGO_ORDEN FROM D_ORDEN WHERE CODIGO_ORDEN="+cor;
            Cursor dt=Con.OpenDT(sql);
            if (dt.getCount()>0) flag=false;
        } catch (Exception e) {
            String ss=e.getMessage();
        }

        try {
            file=new File(fname);
            br = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            errstr=e.getMessage(); moveFile(fname,ename,errstr);
            toast("Ocurrio error en recepción de orden :\n"+errstr);return false;
        }

        if (flag) {
            try {
                db.beginTransaction();

                while ((sql=br.readLine())!= null) {
                    items.add(sql);
                    db.execSQL(sql);
                }

                sql="UPDATE D_ORDEN SET FECHA_FIN=0 WHERE CODIGO_ORDEN="+cor;
                db.execSQL(sql);

                db.setTransactionSuccessful();
                db.endTransaction();

                corels.add(cor);
            } catch (Exception e) {
                db.endTransaction();errstr=e.getMessage();moveFile(fname,ename,errstr);
                toast("Ocurrio error en recepción de orden :\n"+errstr);return false;
            }
        }

        try {
            br.close();
        } catch (Exception e) {
            errstr=e.getMessage();
        }

        file.delete();

        return true;
    }

    public void enviaConfirmacion() {
        String ss="";
        int codo;
        long fini;

        try {
            for (int i = 0; i <D_ordenObj.count; i++) {
                codo=D_ordenObj.items.get(i).codigo_orden;
                fini=D_ordenObj.items.get(i).fecha_inicio;

                ss+="UPDATE D_orden SET ESTADO=1 WHERE CODIGO_ORDEN="+codo+";";

                try {
                    sql="UPDATE D_ORDEN SET ESTADO=1 WHERE CODIGO_ORDEN="+codo;
                    db.execSQL(sql);
                } catch (Exception e) {}

            }

            Intent intent = new Intent(MainActivity.this, srvCommit.class);
            intent.putExtra("URL",gl.wsurl);
            intent.putExtra("command",ss);
            startService(intent);

        } catch (Exception e) {
            toast(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
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

            myReader.close();

        } catch (Exception e) {
            toastlong("Archivo de configuracion no existe o incompleto");gl.wsurl="";
        }
    }

    private void moveFile(String fname,String ename) {
        try {
            FileUtils.forceDelete(new File(ename));
            FileUtils.moveFile(new File(fname),new File(ename));
         } catch (Exception e) {}
    }

    private void moveFile(String fname,String ename,String errstr) {
        try {
            File ff=new File(fname);
            File fe=new File(ename);

            FileUtils.forceDelete(fe);
            FileUtils.moveFile(ff,fe);
            FileUtils.writeStringToFile(fe,errstr,"UTF8",true);
        } catch (Exception e) {}
    }

    private int getItemColor(double pos,int estado) {
        int rv,gv,bv;

        if (estado==2) return Color.rgb(0,216,36);

        if (pos<0) return Color.rgb(0,240,0);
        if (pos>1) return Color.rgb(255,0,0);

        rv=255;
        if (pos<=0.5) {
            gv=255;bv=(int) ((1-pos)*255);
        } else {
            gv=(int) ((1-pos)*255);bv=0;
        }

        gv=255-(int) (pos*128);
        bv=(int) ((1-pos)*255);

        return Color.rgb(rv,gv,bv);
    }

    //endregion

    //region Dialogs

    private void showItemMenu() {
        final String[] selitems = {"ORDENES ENTREGADOS","ORDENES ANULADOS","CERRAR MONITOR"};
        final AlertDialog Dialog;

        try {

            TextView titleView = new TextView(this);
            titleView.setText("MENU PRINCIPAL");
            titleView.setPadding(20, 10, 10, 20);
            titleView.setTextSize(24F);
            titleView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            titleView.setBackgroundColor(Color.parseColor("#1A8AC6"));
            titleView.setTextColor(Color.WHITE);

            AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
            menudlg.setCustomTitle(titleView);

            menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            gl.modo = 0;
                            startActivity(new Intent(MainActivity.this, Reactivacion.class));
                            break;
                        case 1:
                            gl.modo = 1;
                            startActivity(new Intent(MainActivity.this, Reactivacion.class));
                            break;
                        case 2:
                            finish();
                            break;
                    }
                }
            });

            menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });


            Dialog = menudlg.create();
            Dialog.setOnShowListener(
                    new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface alert) {
                            ListView listView = ((AlertDialog) alert).getListView();
                            ListAdapter originalAdapter = listView.getAdapter();

                            listView.setAdapter(new ListAdapter() {
                                @Override
                                public int getCount() {
                                    return originalAdapter.getCount();
                                }

                                @Override
                                public Object getItem(int id) {
                                    return originalAdapter.getItem(id);
                                }

                                @Override
                                public long getItemId(int id) {
                                    return originalAdapter.getItemId(id);
                                }

                                @Override
                                public int getItemViewType(int id) {
                                    return originalAdapter.getItemViewType(id);
                                }

                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = originalAdapter.getView(position, convertView, parent);
                                    TextView textView = (TextView) view;
                                    textView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                                    textView.setTextColor(Color.BLACK);
                                    textView.setPadding(30, 10, 10, 10);
                                    textView.setTextSize(36);
                                    return view;
                                }

                                @Override
                                public int getViewTypeCount() {
                                    return originalAdapter.getViewTypeCount();
                                }

                                @Override
                                public boolean hasStableIds() {
                                    return originalAdapter.hasStableIds();
                                }

                                @Override
                                public boolean isEmpty() {
                                    return originalAdapter.isEmpty();
                                }

                                @Override
                                public void registerDataSetObserver(DataSetObserver observer) {
                                    originalAdapter.registerDataSetObserver(observer);
                                }

                                @Override
                                public void unregisterDataSetObserver(DataSetObserver observer) {
                                    originalAdapter.unregisterDataSetObserver(observer);
                                }

                                @Override
                                public boolean areAllItemsEnabled() {
                                    return originalAdapter.areAllItemsEnabled();
                                }

                                @Override
                                public boolean isEnabled(int position) {
                                    return originalAdapter.isEnabled(position);
                                }
                            });
                        }
                    });
            Dialog.show();

            try {
                Button btnNeg=Dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnNeg.setTextSize(24F);
                //btnNeg.setTextColor(Color.BLACK);
                btnNeg.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                //btnNeg.setBackgroundColor(btnbackcolor);
            } catch (Exception e) {}


        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Permissions

    private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 20) {

                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }

        } catch (Exception e) {
            msgbox(new Object() { }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startApplication();
            } else {
                super.finish();
            }
        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    //endregion

    //region Activity Events

    @Override
    public void onResume() {
        super.onResume();

        idle=true;//toast("MPos Monitor despacho activado");

        try {
            D_ordenObj.reconnect(Con,db);
        } catch (Exception e) {
            toast(e.getMessage());
        }

        try {
            listItems();
        } catch (Exception e) {
            toast(e.getMessage());
        }
    }

    @Override
    public void onPause() {
        //toast("MPos Monitor despacho desactivado");
        idle=false;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    //endregion

}