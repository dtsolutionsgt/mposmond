package com.dts.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.dts.mposmon.R;


public class BaseDatosScript {
	
	private Context vcontext;
	
	public BaseDatosScript(Context context) {
		vcontext=context;
	}
	
	public int scriptDatabase(SQLiteDatabase database) {
		try {
			if (scriptTablas(database)==0) return 0; else return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private int scriptTablas(SQLiteDatabase db) {
		String sql;

		try {

            sql="CREATE TABLE [D_orden] ("+
                    "CODIGO_ORDEN INTEGER NOT NULL,"+
                    "COREL TEXT NOT NULL,"+
                    "EMPRESA INTEGER NOT NULL,"+
                    "CODIGO_RUTA INTEGER NOT NULL,"+
                    "TIPO INTEGER NOT NULL,"+
                    "NUM_ORDEN TEXT NOT NULL,"+
                    "ESTADO INTEGER NOT NULL,"+
                    "FECHA_INICIO INTEGER NOT NULL,"+
                    "FECHA_FIN INTEGER NOT NULL,"+
                    "TIEMPO_LIMITE INTEGER NOT NULL,"+
                    "TIEMPO_TOTAL INTEGER NOT NULL,"+
                    "NOTA TEXT NOT NULL,"+
                    "PRIMARY KEY ([CODIGO_ORDEN])"+
                    ");";
            db.execSQL(sql);


            sql="CREATE TABLE [D_ordend] ("+
                    "CODIGO_ORDEN INTEGER NOT NULL,"+
                    "COREL TEXT NOT NULL,"+
                    "EMPRESA INTEGER NOT NULL,"+
                    "ITEMID INTEGER NOT NULL,"+
                    "TIPO INTEGER NOT NULL,"+
                    "NOMBRE TEXT NOT NULL,"+
                    "NOTA TEXT NOT NULL,"+
                    "MODIF TEXT NOT NULL,"+
                    "PRIMARY KEY ([CODIGO_ORDEN],[ITEMID])"+
                    ");";
            db.execSQL(sql);

            sql="CREATE INDEX D_ordend_idx1 ON D_ordend(COREL)";db.execSQL(sql);
            sql="CREATE INDEX D_ordend_idx2 ON D_ordend(EMPRESA)";db.execSQL(sql);


            //-------------------------------------------

            sql = "CREATE TABLE [Params] (" +
					"ID integer NOT NULL," +
					"dbver INTEGER  NOT NULL," +
					"param1 TEXT  NOT NULL," +
					"param2 TEXT  NOT NULL," +
					"param3 INTEGER  NOT NULL," + // Sort By : 0 - Name , 1- ID
					"param4 INTEGER  NOT NULL," +
					"lic1 TEXT  NOT NULL," +
					"lic2 INTEGER  NOT NULL," +
					"PRIMARY KEY ([ID])" +
					");";
			db.execSQL(sql);

			return 1;

		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	public int scriptData(SQLiteDatabase db) {
		try {
			db.execSQL("INSERT INTO Params VALUES (1,1,'','',0,0,'',0);");

            return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}
	
	private void msgbox(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);

		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {}
    	});
		dialog.show();
	
	}   	
	
}