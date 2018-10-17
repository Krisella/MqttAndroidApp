package com.pizzaincorporated.androidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.net.InetAddress;

import static android.app.Activity.RESULT_OK;
import static java.lang.Thread.sleep;



public class NetworkCheck extends AsyncTask<Void, Void, Void> {
    private Context     context;
    private int sleepTime = 10000;
    AlertDialog alertDialog = null;

    public  NetworkCheck(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids){
        Boolean netAccess;
        while(!this.isCancelled()){
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");
                netAccess = !ipAddr.equals("");
            } catch (Exception e) {
                netAccess = false;
            }
            if(!netAccess){
                publishProgress();
            }
            try {
                sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... voids){
        if(alertDialog == null) {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Internetz lost!");
            alertDialog.setMessage("Network connectivity lost. Please restore internet access.");
            alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    alertDialog = null;
                    Log.e("err", "Network connectivity lost. Please restore internet access.");
                }
            });
            alertDialog.show();
        }
    }

//    protected void onPostExecute(Void result){
//        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//        alertDialog.setTitle("Internetz lost!");
//        alertDialog.setMessage("Network connectivity lost. Please restore internet access.");
//        alertDialog.setButton(RESULT_OK, "OK", new DialogInterface.OnClickListener() {
//            public void onClick(final DialogInterface dialog, final int which) {
//                Log.e("err", "Network connectivity lost. Please restore internet access.");
//            }
//        });
//        alertDialog.show();
//    }
}
