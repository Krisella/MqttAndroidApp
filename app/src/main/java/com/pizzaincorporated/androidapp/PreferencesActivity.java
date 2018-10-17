package com.pizzaincorporated.androidapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttException;

public class PreferencesActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            // ip EditText change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_mqtt_ip_address)));

            // port preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_mqtt_port)));

            // interval preference click listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_mqtt_message_interval)));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @SuppressLint("CommitPrefEdits")
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof  EditTextPreference){
                if(preference.getKey().equals("key_mqtt_ip_address") || preference.getKey().equals("key_mqtt_port")){
                    String[] splitServerUri = MqttConnection.serverUri.split(":");

                    if(preference.getKey().equals("key_mqtt_ip_address") && !((EditTextPreference) preference).getText().equals(stringValue)){
                        try {
                            SharedPreferences preferences = ApplicationContextProvider.getContext().getSharedPreferences("user_preferences", MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("ip_address", stringValue);
                            edit.putString("port", splitServerUri[2]);
                            edit.apply();

                            MqttConnection.mqttAndroidClient.disconnect();
                            MqttConnection.Connect(true);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }else if(preference.getKey().equals("key_mqtt_port") && !((EditTextPreference) preference).getText().equals(stringValue)){
                        try {
                            SharedPreferences preferences = ApplicationContextProvider.getContext().getSharedPreferences("user_preferences", MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString("port", stringValue);
                            edit.putString("ip_address", splitServerUri[1].split("//")[1]);
                            edit.apply();

                            MqttConnection.mqttAndroidClient.disconnect();
                            MqttConnection.Connect(true);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(preference.getKey().equals("key_mqtt_message_interval")){
                    if(!((EditTextPreference) preference).getText().equals(stringValue)){
                        SharedPreferences preferences = ApplicationContextProvider.getContext().getSharedPreferences("user_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putString("frequency", stringValue);
                        edit.apply();

                        MqttConnection.publishFrequency();
                    }
                }
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


}