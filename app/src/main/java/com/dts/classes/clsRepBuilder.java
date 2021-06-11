package com.dts.classes;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;


public class clsRepBuilder {
	
	public String fname,cursym;
	public int prw;
	
	public ArrayList<String> items=new ArrayList<String>();
	private Context cont;
	private BufferedWriter writer = null;
	private FileWriter wfile;
	private DecimalFormat decfrm;
	
	private int seplen,prwq,prwt,prwh,decimp;
	private double aux;
	private String frmstr,ts;
	
	public clsRepBuilder(Context context, int printwidth, boolean regular, String cursymbol, int decimpres, String archivo) {

		cont=context; 
		prw=printwidth;
		seplen=prw;
		cursym=cursymbol;
		decimp=decimpres;
				
		aux=prw;
		prwq=(int) Math.floor(aux/4);
		prwt=(int) Math.floor(aux/3);
		prwh=(int) Math.floor(aux/2);

		System.setProperty("line.separator","\r\n");
		if (regular) {
			if (!archivo.isEmpty()){
				fname = Environment.getExternalStorageDirectory()+"/"+archivo;
			}else{
				fname = Environment.getExternalStorageDirectory()+"/print.txt";
			}
		} else {
			fname = Environment.getExternalStorageDirectory()+"/SyncFold/findia.txt";
		}
		
		decfrm = new DecimalFormat("#,##0.00");
	}	
	
		
	// Main
	
	public String build() {
		String s,ss="";
		
		if (items.size()==0) return "";
		
		try {
		    for (int i = 0; i < items.size(); i++) {
			   	s=items.get(i);
			   	ss=ss+s+"\r\n";
			}
		    
		    return ss;
		} catch(Exception e){
			return "";
		}

	}

	public boolean save(){
		return saverep(false);
	}

	public boolean saveappend(){
		return saverep(true);
	}

	public boolean saverep(boolean append){
		String s;
		int lns=0;

		if (items.size()==0) return true;

		try {

			wfile=new FileWriter(fname,append);
			writer = new BufferedWriter(wfile);

			if (append) {
				writer.write("\r\n");
				writer.write("\r\n");
			}

			for (int i = 0; i < items.size(); i++) {
				try {
					s=trim(items.get(i));
				} catch (Exception e) {
					s="";
				}

				writer.write(s);writer.write("\r\n");lns++;
			}

			writer.close();
			items.clear();

		} catch(Exception e){
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public boolean save(int cnt){
		return saverep(cnt,false);
	}

	public boolean saveappend(int cnt){
		return saverep(cnt,true);
	}

	public boolean saverep(int cnt,boolean append){
		String s;
		int lns=0;

		if (items.size()==0) return true;

		try {

			wfile=new FileWriter(fname,append);
			writer = new BufferedWriter(wfile);

			if (append) {
				writer.write("\r\n");
				writer.write("\r\n");
			}

			for (int j = 0; j < cnt; j++) {

				for (int i = 0; i < items.size(); i++) {
					try {
						s = trim(items.get(i));
					} catch (Exception e) {
						s = "";
					}

					writer.write(s);
					writer.write("\r\n");
					lns++;
				}

				writer.write("\r\n");
				writer.write("\r\n");
				writer.write("\r\n");
				writer.write("\r\n");

			}

			writer.close();
			items.clear();

		} catch(Exception e){
			Toast.makeText(cont,e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public void clear(){
		items.clear();
	}

	// Simple 
	
	public void empty() {
		items.add(" ");	
	}
	
	public void line() {
		char[] fill = new char[seplen];
		Arrays.fill(fill, '-');
		String s = new String(fill);
		
		items.add(s);
	}
	
	public String ltrim(String ss, int sw) {
		int l=ss.length();
		if (l>sw) {
			ss=ss.substring(0,sw);	
		} else {
			frmstr="%-"+sw+"s";	
			ss= String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	public String rtrim(String ss, int sw) {
		int sl,l;
		String sp="";

		ss=ss.trim();
        l=ss.length();

		if (l>=sw) {
			ss=ss.substring(0,sw);
		} else {
		    /*
		    sl=sw-l;
            for (int i = 0; i <sl; i++) {
                sp=sp+" ";
            }
            ss=sp+ss;  */
			frmstr="%"+sw+"s";
			ss= String.format(frmstr,ss);
		}
		
		return ss;
	}
	
	public String ctrim(String ss) {
		int l=ss.length();
		if (l>prw) {
			ss=ss.substring(0,prw);	
		} else {
			int ldisp=(prw-l)/2;
			frmstr=fillempty(ldisp);	
			ss=frmstr+ss;
		}
		
		return ss;		
	}
	
	
	// Composed 

	public void add(String tn){
		items.add(tn);
	}
	
	public void addc(String tn){
		items.add(ctrim(tn));
	}
	
	public void add3lrr(String s1, String s2, String s3) {
		ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
		items.add(ts);
	}
	
	public void add3lrr(String s1, String s2, double v3) {
        String s3=decfrm.format(v3);

        ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
        items.add(ts);
    }

    public void add3lrrTot(String s1, String s2, double v3) {
        String sval;

        sval=cursym+decfrm.format(v3);

        ts=ltrim(s1,prwt-2)+rtrim(s2,prwt-5)+rtrim(sval,prwt+5);
        items.add(ts);
    }

    public void add4lrrTot(String s1, String s2, String s3, double v3) {
        String sval;
        int val;

        sval=cursym+decfrm.format(v3);

        val=sval.length()-2;

        ts=ltrim(s1,prwt)+ltrim(s2,prwt-2)+ltrim(s3,prwt-val)+ltrim(sval,prwt);
        items.add(ts);
    }

    public void add3Tot(int s1,double v2,double v3,double v4) {
        String sval,sval2,sval3;
        int val;

        sval=cursym+decfrm.format(v2);
        sval2=cursym+decfrm.format(v3);
        sval3=cursym+decfrm.format(v4);

        val=sval.length()-2;

        ts=ltrim(Integer.toString(s1),prwt)+ltrim(sval,prwt-2)+ltrim(sval2,prwt-val)+ltrim(sval3,prwt);
        items.add(ts);
    }

    public void add3Tot2(int s1,double v2,double v3,double v4) {
        String sval,sval2,sval3;
        int val;

        sval=cursym+decfrm.format(v2);
        sval2=cursym+decfrm.format(v3);
        sval3=cursym+decfrm.format(v4);

        val=sval.length()-2;

        ts=ltrim(Integer.toString(s1),prwq-6)+rtrim(sval,prwq+2)+rtrim(sval2,prwq+2)+rtrim(sval3,prwq+2);
        items.add(ts);
    }

	public void add4lrrTotPorc(String s1, String s2, double s3, double v3) {
		String stot;
		int tot;
		String sval;

		if(v3==0.0){
			sval="";
		}else {
			sval = aproxDec(v3);
		}

		stot=cursym+decfrm.format(s3);

		tot=sval.length();

		ts=ltrim(s1,prwt-4)+ltrim(s2,prwt-2)+ltrim(stot,prwt-tot)+ltrim(sval,prwt);
		items.add(ts);
	}

    public void add4lrrTotPorc2(String s1, String s2, double s3, double v3) {
        String stot;
        int tot;
        String sval;

        if(v3==0.0){
            sval="";
        }else {
            sval = aproxDec(v3);
        }

        stot=cursym+decfrm.format(s3);

        tot=sval.length();

        ts=ltrim(s1,prwq-1)+rtrim(s2,prwq-2)+rtrim(stot,prwq+4)+rtrim(sval,prwq-4);
        items.add(ts);
    }

    public void add4lrrTot(String s1, String s2, double s3, double v3) {
        String stot,scom;
        int tot;

        scom=cursym+decfrm.format(v3);
        stot=cursym+decfrm.format(s3);

        tot=scom.length();

        ts=ltrim(s1,prwt)+ltrim(s2,prwt)+ltrim(stot,prwt)+ltrim(scom,prwt);
        items.add(ts);
    }

	public void add4lrrTotV(String s1, String s2, double s3, double v3) {
		String stot,scom;
		int tot;

		scom=cursym+decfrm.format(v3);
		stot=cursym+decfrm.format(s3);

		tot=scom.length()-5;

		ts=ltrim(s1,prwt-2)+ltrim(s2,prwt-4)+ltrim(stot,prwt-tot)+ltrim(scom,prwt);
		items.add(ts);
	}

	public void add4lrrTotZ(double s2,double s3,double v3) {
		String stot,scom,sini;
		int tot;

		scom=cursym+decfrm.format(v3);
		stot=cursym+decfrm.format(s3);
		sini=cursym+decfrm.format(s2);

		tot=scom.length()-3;

		ts=ltrim(sini,prwt+4)+ltrim(stot,prwt+2)+ltrim(scom,prwt);
		items.add(ts);
	}

	public void add4lrrT(String s1, String s2, double s3, double v3) {
		String stot,scom,str,error,error1;
		int tot,rest;

		try {
			if(s1.length()>=12){
				rest=s1.length()-11;
				str = s1.substring(0, s1.length()-rest);
				str = str+" ";
				s1 = str;
			} else if(s1.length()<12 && !s1.isEmpty()){
				str = s1.substring(0, s1.length());
				s1 = str;
			}

			scom=cursym+decfrm.format(v3);
			stot=cursym+decfrm.format(s3);

			tot=scom.length()-5;

			ts=ltrim(s1,prwt+1)+ltrim(s2,prwt-6)+ltrim(stot,prwt-tot-1)+ltrim(scom,prwt);
			items.add(ts);

		} catch (Exception e){
			error=e+"";error1=error;
		}

	}

	public void add4(double s1,double s2,double s3,double v3) {
        String s3tot,s1tot,s2tot;
        int tot;
        String sval;

        if(v3==0.0){
            sval="";
        }else {
            sval = aproxDec(v3);
        }

        s1tot=cursym+decfrm.format(s1);
        s2tot=cursym+decfrm.format(s2);
        s3tot=cursym+decfrm.format(s3);

        tot=sval.length()-2;

        ts=ltrim(s1tot,prwt-1)+ltrim(s2tot,prwt-1)+ltrim(s3tot,prwt-2)+ltrim(sval,prwt);
        items.add(ts);
    }

	public void add3lrr(String s1, double v2, double v3) {
		String s2,s3;
		
		s2=cursym+decfrm.format(v2);
		s3=cursym+decfrm.format(v3);
		
		ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
		items.add(ts);
	}

    public void add3sss(String s1, String s2, String s3) {
        ts=ltrim(s1,prwt)+rtrim(s2,prwt)+rtrim(s3,prwt);
        items.add(ts);
    }
	
	public void add3fact(String s1, double v2, double v3) {
		String s2,s3;
		
		s2=cursym+decfrm.format(v2);
		s3=cursym+decfrm.format(v3);
		
		ts=ltrim(s1,prwh)+rtrim(s2,prwq)+rtrim(s3,prwq);
		items.add(ts);
	}

	public void add3fact(String s1, String s2, String s3) {
		ts=ltrim(s1,prwh)+rtrim(s2,prwq)+rtrim(s3,prwq);
		items.add(ts);
	}
	
	public void addtot(String s1, String val) {

		String str;

		str = val.substring(0, val.length()-1);
		val = str;

		ts=ltrim(s1,prw-30)+" "+ltrim(val,15);
		items.add(ts);
	}

    public void addtot2(String s1, String val) {
	    ts=ltrim(s1,4)+" "+ltrim(val,prw-5);
        items.add(ts);
    }

    public void addtwo(String s1, String val) {
		ts=ltrim(s1,prw-23)+" "+ltrim(val,25);
		items.add(ts);
	}

	public void addtotD(String s1, double val) {
		String sval;
		sval = Double.toString(val);
		ts=ltrim(s1,prw-13)+" "+rtrim(sval,12);
		items.add(ts);
	}

	public void addtot(String s1, double val) {
		String sval;
		
		sval=cursym+decfrm.format(val);
		ts=ltrim(s1,prw-13)+" "+rtrim(sval,12);
		items.add(ts);
	}

    public void addtot3(String s1, String s2, double val) {
        String sval;

        sval = cursym+decfrm.format(val);

        ts=ltrim(s1,prw-25)+ltrim(s2,prw-24)+rtrim(sval,12);
        items.add(ts);
    }

	public void addtotint(String s1, int val) {
		ts=ltrim(s1,prw-13)+" "+rtrim(Integer.toString(val),12);
		items.add(ts);
	}

	public void addtotpeso(String s1, double val) {
		String sval;

		sval = Double.toString(val);
		ts=ltrim(s1,prw-13)+" "+rtrim(sval,12);
		items.add(ts);
	}

	public void addtotsp(String s1, double val) {
		String sval;
		
		sval=cursym+decfrm.format(val);
		ts=ltrim(s1,prw-14)+" "+rtrim(sval,12)+"  ";
		items.add(ts);
	}
	
	public void addtot(String s1, String val, int wid) {
		ts=ltrim(s1,prw-wid-1)+" "+rtrim(val,wid);
		items.add(ts);
	}
	
	public void addt(String tn){
		items.add("\t"+tn);	
	}
	
	public void add(String tn, int tbs){
		String ss="";
		
		for (int i = 0; i <tbs; i++) {
			ss+="\t";
		}
		
		items.add(ss+tn);	
	}
	
	public void addg(String s1, String s2, double val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,15);ss=s1+"\t";
		s2="("+s2+")";
		s2=StringUtils.leftPad(s2,6);ss=ss+s2+"\t";
		s3=decfrm.format(val);
		s3=StringUtils.leftPad(s3,12);ss+=s3;
		
		items.add(ss);
		
	}

    public void addmp(String s1, double val, String s3, double costo) {
        String ss,s2,s4;
        double ccos;

        s1=StringUtils.rightPad(s1,16);ss=s1+"\t";

        s2=decfrm.format(val);
        s2=StringUtils.leftPad(s2,7);ss+=s2+"\t";

        s3=StringUtils.leftPad(s3,4);ss=ss+s3;

        ccos=val*costo;
        s4=decfrm.format(ccos);
        s4=StringUtils.leftPad(s4,10);ss=ss+s4;

        items.add(ss);

    }

    public void addmptot(double total) {
        String ss,s2,s4;

        s2=decfrm.format(total);
        s4=StringUtils.leftPad(s2,39);

        items.add(s4);

    }
	
	public void addg(String s1, String s2, String val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,15);ss=s1+"\t";
		s2=StringUtils.leftPad(s2,6);ss=ss+s2+"\t";
		s3=val;
		s3=StringUtils.leftPad(s3,12);ss+=s3;
		
		items.add(ss);
		
	}

	public void addp(String s1, String val) {
		String ss,s3;
		
		s1=StringUtils.rightPad(s1,24);ss=s1+"";
		s3=val;
		s3=StringUtils.leftPad(s3,12);ss+=s3;
		
		items.add(ss);
		
	}
	
	public void addpu(String s1, String s2, int mw) {
		String ss,st;
		
		ss=StringUtils.rightPad(s1,mw);
		st=StringUtils.rightPad(s2,mw);		
		items.add(ss+" "+st);		
	}
	
	
	
	// Aux
	
	public String frmdec(double val) {
		return cursym+decfrm.format(val);
	}
	
	private String trim(String ss) {
		int l=ss.length();
		if (l>prw) ss=ss.substring(0,prw);	
		return ss;
	}
	
	private String fillempty(int cn) {
		char[] fill = new char[cn];
		Arrays.fill(fill, ' ');
		String s = new String(fill);
		
		return s;
	}

	public String aproxDec(double pc){
	    String val;
	    val = Long.toString(Math.round(pc));
	    return val+"%";
    }
	
}
