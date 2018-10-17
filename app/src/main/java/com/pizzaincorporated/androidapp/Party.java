package com.pizzaincorporated.androidapp;

import android.hardware.Camera;
import android.media.Ringtone;
import android.os.AsyncTask;



public class Party extends AsyncTask<Void, Void, Void> {
    private int         duration;
    private Ringtone    ringtone;

    Party(int duration, Ringtone ringtone){
        this.duration = duration;
        this.ringtone = ringtone;
    }

    @Override
    protected Void doInBackground(Void... voids){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        if(ringtone.isPlaying()) {
            ringtone.stop();
        }
        if(MqttConnection.hasCameraFlash()) {
            MqttConnection.p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            MqttConnection.camera.setParameters(MqttConnection.p);
            MqttConnection.camera.stopPreview();
        }
    }
}
