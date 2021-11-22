package com.dts.mposmon;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
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
import com.dts.classes.clsD_ordendObj;
import com.dts.classes.clsRepBuilder;
import com.dts.ladapter.LA_D_orden;
import com.dts.ladapter.LA_D_ordend;
import com.dts.services.srvCommit;
import com.dts.services.startJobService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;

public class MainActivity extends PBase {

    private GridView gridView;
    private ImageView imgCon,imgWifi;
    private TextView lblErr,lblHora,lblSuc,lble,lblp,lblr;

    private clsD_ordenObj D_ordenObj;

    private LA_D_orden adapter;

    public ArrayList<String> corels= new ArrayList<String>();

    private String params,errstr,path,mac;
    private boolean idle=true,wifi;

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
            imgWifi=findViewById(R.id.imageView6);imgWifi.setVisibility(View.INVISIBLE);
            lblErr=findViewById(R.id.textView3);lblErr.setVisibility(View.INVISIBLE);
            lblHora=findViewById(R.id.textView);
            lblSuc=findViewById(R.id.textView17);
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

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Object lvObj = gridView.getItemAtPosition(position);
                    clsClasses.clsD_orden item = (clsClasses.clsD_orden)lvObj;

                    adapter.setSelectedIndex(position);

                    corels.clear();
                    corels.add(""+item.codigo_orden);
                    imprimeOrdenes();

                    SystemClock.sleep(5000);
                } catch (Exception e) {
                }
                return true;
            }
        });


    }

    //endregion

    //region Main

    private void processTimer() {
        try {
            lblHora.setText(du.shora(du.getActDateTime()));

            if (wifi) {
                if (isOnWifi()==0) {
                    if (imgCon.getVisibility()==View.INVISIBLE) toastlong("SIN CONEXIÓN A INTERNET");
                    imgCon.setVisibility(View.VISIBLE);
                } else {
                    if (imgCon.getVisibility()==View.VISIBLE) toastlong("Internet conectado");
                    imgCon.setVisibility(View.INVISIBLE);
                    if (idle) recibeOrdenes();
                }
            } else {
                if (idle) recibeOrdenes();
            }
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

            D_ordenObj.fill("WHERE ESTADO<3 ORDER BY ESTADO DESC,FECHA_INICIO ");
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
            //toast(e.getMessage());
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

            getParams();
            if (gl.wsurl.isEmpty()) return false;


            wifi=conexionWiFi();if (wifi) imgWifi.setVisibility(View.VISIBLE);

            initTimer();

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
                if (pp>0) {
                    pp=fname.indexOf("_");
                    if (pp<0) {
                        agregaOrden(path+"/"+fname,path+"/error/"+fname,fname);
                    } else {
                        enviaPendiente(path+"/"+fname,fname);
                    }
                }
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

            //if (corels.size()>0) imprimeOrdenes();

            D_ordenObj.fill("WHERE (ESTADO=0)");
            if (D_ordenObj.count>0) enviaConfirmacion();
            D_ordenObj.items.clear();
        } catch (Exception e) {
            toast("recibeOrdenes2 : "+e.getMessage());
        }

        corels.clear();
        listItems();
    }

    public boolean agregaOrden(String fname,String ename,String cor) {
        File file=null;
        BufferedReader br=null;
        ArrayList<String> items=new ArrayList<String>();
        String sql,fi;
        int lim;
        boolean flag=true;

        cor=cor.replace(".txt","");//corels.add(cor);

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
            //toast("Ocurrio error en lectura de orden :\n"+errstr);
            return false;
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
            intent.putExtra("orderid",du.getCorelTimeStr());
            startService(intent);

        } catch (Exception e) {
            toast(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    public void enviaPendiente(String fname,String ordenid) {
        File file=null;
        BufferedReader br=null;
        String ss="";

        try {
            file=new File(fname);
            br = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            return;
        }

        try {
            ss=br.readLine();
        } catch (Exception e) {
            ss="";
        }

        try {
            br.close();
        } catch (Exception e) {}

        if (ss.isEmpty()) return;

        file.delete();

        try {
            Intent intent = new Intent(MainActivity.this, srvCommit.class);
            intent.putExtra("URL",gl.wsurl);
            intent.putExtra("command",ss);
            intent.putExtra("orderid","");
            startService(intent);
        } catch (Exception e) {
            toast(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    //endregion

    //region Impresion

    private void imprimeOrdenes() {
        if (!tieneImpresion()) return;
        if (generaImpresion()) imprimir();
    }

    private boolean generaImpresion() {
        clsRepBuilder rep;
        String ordid,ss;

        try {
            clsD_ordenObj D_ordenObj=new clsD_ordenObj(this,Con,db);
            clsD_ordendObj D_ordendObj=new clsD_ordendObj(this,Con,db);

            rep=new clsRepBuilder(this,36,true,"",2,"print.txt");

            for (int i = 0; i <corels.size(); i++) {

                ordid=corels.get(i);

                try {
                    D_ordenObj.fill("WHERE (CODIGO_ORDEN="+ordid+")");
                    D_ordendObj.fill("WHERE (CODIGO_ORDEN="+ordid+")");

                    ss=D_ordenObj.first().num_orden.toUpperCase()+" "+D_ordenObj.first().nota;

                    rep.add("");
                    rep.add("ORDEN : "+ss);
                    rep.add("");
                    rep.add("Fecha : "+du.sfecha(du.getActDateTime())+" "+du.shora(du.getActDateTime()));
                    rep.line();

                    for (int ii = 0; ii <D_ordendObj.count; ii++) {
                        rep.add(D_ordendObj.items.get(ii).nombre);
                    }

                    rep.add("");rep.add("");rep.add("");

                } catch (Exception e) {
                    toast("generaImpresion : "+e.getMessage());
                }
            }

            rep.save();rep.clear();

            return true;
        } catch (Exception e) {
            toastlong("generaImpresion : "+e.getMessage());
            return false;
        }
    }

    private void imprimir() {
        try {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage("com.dts.epsonprint");
            intent.putExtra("mac","BT:"+mac);
            intent.putExtra("fname", Environment.getExternalStorageDirectory()+"/print.txt");
            intent.putExtra("askprint",1);
            intent.putExtra("copies",1);
            this.startActivity(intent);
        } catch (Exception e) {
            toastlong("El controlador de Epson TM BT no está instalado");
        }
    }

    private boolean tieneImpresion() {
        String line;
        int imp=0;

        try {
            File file1 = new File(Environment.getExternalStorageDirectory(), "/mposmonimp.txt");

            FileInputStream fIn = new FileInputStream(file1);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

            line = myReader.readLine();if (line.equalsIgnoreCase("1")) imp=1;
            line = myReader.readLine();mac=line;

            myReader.close();
        } catch (Exception e) {
           toastlong("Impresion "+e.getMessage()); mac="";imp=0;
        }

        return imp==1;
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
            toastlong("Archivo de configuracion no existe o incompleto");gl.wsurl="";
        }

        nombreSucursal();
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

    public int isOnWifi(){
        int activo=0;

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()){
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) activo=1;
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) activo = 2;
            }
        } catch (Exception ex) {}

        return activo;
    }

    private  void nombreSucursal()  {
        lblSuc.setText(gl.sucursal);
    }

    private void saveConSettings(int modo) {
        FileWriter wfile=null;
        BufferedWriter writer=null;
        String fname;

        try {

            fname = Environment.getExternalStorageDirectory().getPath() + "/mposmoncon.txt";
            wfile = new FileWriter(fname, false);

            if (modo==1) {

                writer = new BufferedWriter(wfile);
                writer.write("ETHERNET");
                writer.write("\r\n");
                writer.close();writer = null;wfile = null;

                restart();
            } else {
                File file=new File(fname);
                try {
                    file.delete();
                    restart();
                } catch (Exception e) {
                    msgbox("No se puede guardar la configuración.\n"+e.getMessage());
                }
            }
        } catch (Exception e) {
            String ss=e.getMessage();
        }
    }

    private void restart() {

        toastlong("Se guardó la configuración. La aplicacion se reiniciará");

        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity( this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void restart2() {

        toastlong("Ordenes borrados,\nla aplicacion se reiniciará");

        Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity( this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private boolean conexionWiFi() {
        try {
            String fname = Environment.getExternalStorageDirectory().getPath() + "/mposmoncon.txt";
            File file=new File(fname);
            return !file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private void borrarOrdenes() {
        try {
            db.beginTransaction();

            db.execSQL("DELETE FROM D_ORDEN");
            db.execSQL("DELETE FROM D_ORDEND");

            db.setTransactionSuccessful();
            db.endTransaction();

            restart2();
        } catch (Exception e) {
            db.endTransaction();
            msgbox(e.getMessage());
        }

    }

    //endregion

    //region Dialogs

    private void showItemMenu() {
        final String[] selitems = {"ORDENES ENTREGADAS","ORDENES ANULADAS",
                "SUCURSAL","IMPRESORA","CONEXIÓN","BORRAR ORDENES","CERRAR MONITOR"};
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
                            startActivity(new Intent(MainActivity.this, Sucursal.class));
                            break;
                        case 3:
                            startActivity(new Intent(MainActivity.this, Impresora.class));
                            break;
                        case 4:
                            msgAskEthernet("Tipo de conexión a la red");
                            break;
                        case 5:
                            msgAskBorrar("Borra todos los ordenes");
                            break;
                        case 6:
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

    private void msgAskEthernet(String msg) {
        ExDialog dialog = new ExDialog(this);
        dialog.setTitle("Conexión");
        dialog.setMessage(msg);

        dialog.setNegativeButton("  Ethernet  ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saveConSettings(1);
            }
        });

        dialog.setPositiveButton("  WiFi  ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                saveConSettings(0);
            }
        });

        dialog.show();
    }

    private void msgAskBorrar(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Borrar ordenes");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                msgAskBorrar2("Está seguro");
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();
    }

    private void msgAskBorrar2(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Borrar ordenes");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                borrarOrdenes();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

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

        if (gl.restart) {
            restart();
            return;
        }

        try {
            D_ordenObj.reconnect(Con,db);
        } catch (Exception e) {
            //toast(e.getMessage());
        }

        try {
            listItems();
        } catch (Exception e) {
            //toast(e.getMessage());
        }

        nombreSucursal();
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