package com.pizzaincorporated.androidapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class MainActivity extends AppCompatActivity {

    private Context context = this;
    boolean doubleBackToExitPressedOnce = false;
    NetworkCheck bgcheck = new NetworkCheck(context);
    private boolean startedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("\tMqtt Receiver");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        if(MqttConnection.hasCamera()) {
            if (MqttConnection.camera != null)
                MqttConnection.camera.release();
            MqttConnection.camera = Camera.open();
            MqttConnection.p = MqttConnection.camera.getParameters();
        }
        bgcheck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        FloatingActionButton fab_camera = findViewById(R.id.floatingActionButton6);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final PackageManager pm = context.getPackageManager();
                if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                        pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    if (MqttConnection.p.getFlashMode().toString().equals("torch")) {
                        MqttConnection.p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        MqttConnection.camera.setParameters(MqttConnection.p);
                        MqttConnection.camera.stopPreview();
                    }
                }
            }
        });

        FloatingActionButton fab_sound = findViewById(R.id.floatingActionButton7);
        fab_sound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(r.isPlaying())
                    r.stop();
            }
        });

        if(!startedFlag) {
            MqttConnection.r = r;
            MqttConnection.Connect(!startedFlag);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);

        }else if(id == R.id.action_exit) {
            try {
                MqttConnection.mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            finishAffinity();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        bgcheck.cancel(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        bgcheck = new NetworkCheck(context);
        bgcheck.execute();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (isFinishing()) {
            startedFlag = false;
        }
    }
}
