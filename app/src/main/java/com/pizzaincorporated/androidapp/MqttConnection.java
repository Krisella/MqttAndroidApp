package com.pizzaincorporated.androidapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.Ringtone;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.Timestamp;

public class MqttConnection {
    static MqttAndroidClient mqttAndroidClient;
    static final String subscriptionTopic =
            ApplicationContextProvider.getContext().getString(R.string.subscription_topic);
    static final String publishTopic =
            ApplicationContextProvider.getContext().getString(R.string.publish_topic);
    static String clientId =
            ApplicationContextProvider.getContext().getString(R.string.client_id);
    public static Camera.Parameters p;
    public static Camera camera = null;
    static Ringtone r;
    private static final String TAG = MainActivity.class.getName();
    static String serverUri;

    public MqttConnection(Ringtone r) {
        this.r = r;}

    public static void Connect(final boolean publishFreq){

        clientId = clientId + System.currentTimeMillis();

        if(hasCamera()){
            if(camera!=null)
                camera.release();
            camera = Camera.open();
            p = camera.getParameters();
        }

        SharedPreferences preferences = ApplicationContextProvider.getContext().getSharedPreferences("user_preferences", Activity.MODE_PRIVATE);
        String ipAddress = preferences.getString("ip_address", "");
        String port = preferences.getString("port", "");

        if(ipAddress.isEmpty() || port.isEmpty())
            serverUri = ApplicationContextProvider.getContext().getString(R.string.default_server_uri);
        else
            serverUri = "tcp://" + ipAddress + ":" + port;

        mqttAndroidClient = new MqttAndroidClient(
                ApplicationContextProvider.getContext().getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {}

            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                addToHistory("Incoming message: " + new String(message.getPayload()));

                String time = new Timestamp(System.currentTimeMillis()).toString();
                System.out.println("Time:\t" + time +
                        " Topic:\t" + topic +
                        " Message:\t" + new String(message.getPayload()) +
                        " QoS:\t" + message.getQos());

                try {
//                    if (message.toString().equals("Execute Eyes Opened")) {
//                        if(hasCameraFlash()) {
//                            Log.i("info", "turn on flashlight");
//                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                            camera.setParameters(p);
//                            camera.startPreview();
//                        }
//                        if (!r.isPlaying())
//                            r.play();
//                    } else if(message.toString().equals("Execute Eyes Closed")) {
//                        if (hasCameraFlash() && p.getFlashMode().toString().equals("torch")) {
//                            Log.i("info", "turn off flashlight");
//                            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                            camera.setParameters(p);
//                            camera.stopPreview();
//                        }
//                        if (r.isPlaying())
//                            r.stop();
//                    }
                    if (message.toString().equals("Execute Eyes Opened")) {
                        if(hasCameraFlash()) {
                            Log.i("info", "turn on flashlight");
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            camera.setParameters(p);
                            camera.startPreview();
                        }
                        if (!r.isPlaying()) {
                            r.play();
                        }
                        SharedPreferences preferences = ApplicationContextProvider.
                                                        getContext().
                                                        getSharedPreferences("user_preferences",
                                                                            Activity.MODE_PRIVATE);
                        String frequency = preferences.getString("frequency", "");
                        if(frequency == null || frequency.isEmpty()){
                            frequency = String.valueOf(ApplicationContextProvider.getContext().getString(R.string.default_frequency));
                        }
                        System.out.println(frequency);
                        new Party(Integer.parseInt(frequency) * 500, r).
                                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (RuntimeException e) {
                    Log.e("Error Opening Camera ", e.getMessage());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            //addToHistory("Connecting to " + serverUri);
            System.out.println("Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
//                    disconnectedBufferOptions.setBufferEnabled(true);
//                    disconnectedBufferOptions.setBufferSize(100);
//                    disconnectedBufferOptions.setPersistBuffer(false);
//                    disconnectedBufferOptions.setDeleteOldestMessages(false);
//                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    if(publishFreq)
                        publishFrequency();
                    subscribeToTopic();

                    Log.d(TAG, "onSuccess");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to connect to: " + serverUri);
                    Log.d(TAG, "onFailure. Exception when connecting: " + exception);
                }
            });

        } catch (Exception e){
            Log.e(TAG, "Error while connecting to Mqtt broker : " + e);

            e.printStackTrace();
        }
    }

    public static boolean hasCamera(){
        final PackageManager pm = ApplicationContextProvider.getContext().getPackageManager();
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            AlertDialog alertDialog = new AlertDialog.Builder(ApplicationContextProvider.getContext()).create();
            alertDialog.setTitle("No Camera");
            alertDialog.setMessage("This device doesn't support camera.");
            alertDialog.setButton(Activity.RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    Log.e("err", "This device doesn't support camera.");
                }
            });
            alertDialog.show();
            return false;
        }else
            return true;
    }

    public static boolean hasCameraFlash(){
        final PackageManager pm = ApplicationContextProvider.getContext().getPackageManager();
        if(!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            AlertDialog alertDialog = new AlertDialog.Builder(ApplicationContextProvider.getContext()).create();
            alertDialog.setTitle("No Camera Flash");
            alertDialog.setMessage("The device's camera doesn't support flash.");
            alertDialog.setButton(Activity.RESULT_OK, "OK", new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    Log.e("err", "The device's camera doesn't support flash.");
                }
            });
            alertDialog.show();
            return false;
        }
        else
            return true;
    }

    public static void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
//                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    addToHistory("Failed to subscribe");
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public static void publishFrequency(){
        MqttMessage message = new MqttMessage();
        SharedPreferences preferences = ApplicationContextProvider.getContext().getSharedPreferences("user_preferences", Activity.MODE_PRIVATE);
        String frequency = preferences.getString("frequency", "");

        if(frequency == null || frequency.isEmpty())
            frequency = String.valueOf(ApplicationContextProvider.getContext().getString(R.string.default_frequency));
        try {
            message.setPayload(frequency.getBytes());
            mqttAndroidClient.publish(publishTopic, message);

        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
