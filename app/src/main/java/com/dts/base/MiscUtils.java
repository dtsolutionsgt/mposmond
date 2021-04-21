package com.dts.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.Toast;

import com.dts.mposmon.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;

public class MiscUtils {
	
	public ColorDef color;
	
	private Context cont;
	private appGlobals gl;
	
	private DecimalFormat thnumformat,thdecformat,plainnumformat,decformat;
	private DecimalFormat ffrmdec1,ffrmdec2,ffrmdec3,ffrmdec4;
    private String slang="EN";
	
	public MiscUtils(Context context, appGlobals global) {
		
		cont=context; 
		gl=global;
		
		color=new ColorDef(cont);
		
		thnumformat = new DecimalFormat("#,###");
		thdecformat = new DecimalFormat("#,###.##");
		decformat = new DecimalFormat("#0.00");
		plainnumformat = new DecimalFormat("#0");
        ffrmdec1 = new DecimalFormat("#,##0.#");
	    ffrmdec2 = new DecimalFormat("#,##0.##");
        ffrmdec3 = new DecimalFormat("#,##0.00");
        ffrmdec4 = new DecimalFormat("#,##0.0");
	}
	
	public MiscUtils(Context context) {
		
		cont=context; 
		
		color=new ColorDef(cont);
		
		thnumformat = new DecimalFormat("#,###");
		thdecformat = new DecimalFormat("#,###.##");
		decformat = new DecimalFormat("#0.00");
		plainnumformat = new DecimalFormat("#0");
        ffrmdec1 = new DecimalFormat("#,##0.#");
	    ffrmdec2 = new DecimalFormat("#,##0.##");
        ffrmdec3 = new DecimalFormat("#,##0.00");
        ffrmdec4 = new DecimalFormat("#,##0.0");
	}
	
	public String decfrm(double val) {
		return decformat.format(val);
	}
	
	public String frmintth(double val) {
		return thnumformat.format(val);
	}
	
	public String frmdblth(double val) {
		return thdecformat.format(val);
	}
	
	public String frmintnum(double val) {
		return plainnumformat.format(val);
	}

	public String frmonedec(double val) {
		return ffrmdec4.format(val);
	}

    public String frmdecno(double val) {
        //return ffrmdec2.format(val);
        return ffrmdec1.format(val);
    }

    public String frmmonto(double val) {
         return ffrmdec3.format(val);
    }

    public String frmdecno2(double val) {
        return ffrmdec2.format(val);
     }

    public String frmmin(int min) {
	    String ss=""+min;
	    if (min<10) ss="0"+min;
	    return ss;
    }

	public int CInt(String s) {
		return Integer.parseInt(s);
	}

	public double CDbl(String s) {
		return Double.parseDouble(s);
	}
	
	public String CStr(int v){
		return String.valueOf(v);
	}
	
	public String CStr(double v){
		return String.valueOf(v);
	}
	
	public boolean emptystr(String s){
		if (s==null || s.isEmpty()) {
			return true;
		} else{
			return false;
		}
	}

	public void setlang(){
		slang= Locale.getDefault().getLanguage();
		slang=slang.substring(0,2);
	}
	
	public String lstr(String s1, String s2) {
		if (slang.equalsIgnoreCase("ES")) {
			return s2;
		} else {	
			return s1;
		}
	}
	
	public void msgbox(String msg) {
		
		if (msg==null || msg.isEmpty()) {return;}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);
		//dialog.setIcon(R.drawable.info48);
		
		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    	//Toast.makeText(getApplicationContext(), "Yes button pressed",Toast.LENGTH_SHORT).show();
    	    }
    	});
		dialog.show();
	
	}   
	
	public void msgbox(int v) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(String.valueOf(v));
		//dialog.setIcon(R.drawable.info48);
		
		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
    	    }
    	});
		dialog.show();
	
	}   
	
	public void toast(String msg) {
		Toast.makeText(cont,msg, Toast.LENGTH_SHORT).show();
	}
	
	public void toast(double msg) {
		Toast.makeText(cont,""+msg, Toast.LENGTH_SHORT).show();
	}
	
	public int intdiv(double val,double div) {
		if (div==0) return 0;
		
		val=val/div;
		return (int) val;
	}
	
	public int trunc(double val,double div) {
		if (div==0) return 0;
		
		val=val/div;val= Math.floor(val);
		return (int) val;
	}
	
	public class ColorDef {
		
		public int neonyellow,neongreen,neonorange,neonpink,neonblue;
		
		private Context cont;
		
		public ColorDef(Context context) {
			cont=context; 
			
			neonyellow= Color.rgb(243,243,21);
			neongreen= Color.rgb(193,253,51);
			neonorange= Color.rgb(255,153,51);
			neonpink= Color.rgb(252,90,184);
			neonblue= Color.rgb(13,213,252);
			
		}
		
	}
	
	public boolean fileexists(String fname) {
		try {
			File file = new File(fname);
			return file.exists();
		} catch (Exception e) {
			return false;
		}	
	}

	public String toPounds(double w) {
	    double v1,v2;

	    w=w*2.205;
	    v1= Math.floor(w);
	    v2=(w-v1)*1000/28.35;
	    v2= Math.round(v2);

	    return frmintnum(v1)+" lb "+frmintnum(v2)+" oz";
    }

    public String toFeet(double h) {
        double v1,v2;

        v1=h/30.5;v1= Math.floor(v1);
        v2=h-v1*30.5;
        v2=v2/2.54;v2= Math.round(v2);

        return frmintnum(v1)+"' "+frmintnum(v2)+" ''";
    }

}

