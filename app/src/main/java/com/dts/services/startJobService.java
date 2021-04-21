package com.dts.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

public class startJobService {

    public static void startService(Context context, String params) {
        try {
            ComponentName serviceComponent = new ComponentName(context, srvOrdenesImport.class);

            PersistableBundle extras = new PersistableBundle();
            extras.putString("params",params);
            //assert scheduler != null;
            //JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent).setOverrideDeadline(0L).setExtras(extras);

            builder.setMinimumLatency(15 * 1000); // wait at least
            builder.setOverrideDeadline(10 * 1000); // maximum delay

            //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            jobScheduler.schedule(builder.build());
        } catch (Exception e) {
            String ss=e.getMessage();
        }


    }
}
