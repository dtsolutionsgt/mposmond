package com.dts.webservice;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class wsCommit extends wsBase {

    private String command;

    public wsCommit(String Url) {
        super(Url);
    }

    public void execute(String commandlist, Runnable afterfinish) {
        command=commandlist;
        super.execute(afterfinish);
    }

    @Override
    protected void wsExecute() {
        commit();
    }

    public void commit() {
        String mNAME = "Commit";
        String sstr;

        error="";errflag=false;

        try {

            SoapObject request = new SoapObject(NAMESPACE, mNAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");
            param.setValue(command);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE + mNAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString();

            if (!sstr.equalsIgnoreCase("#")) {
                error=sstr;errflag=true;
            }
        } catch (Exception e) {
            error=e.getMessage();errflag=true;
        }
    }

}
