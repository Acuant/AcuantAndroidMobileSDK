package com.cssn.samplesdk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by tapasbehera on 3/1/17.
 */

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private int previous_webservice = 0 ; // 0-Acufill , 1 = Connect
    private SharedPreferences sharedPref = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        previous_webservice = sharedPref.getInt("WEBSERVICE",0);
        addPreferencesFromResource(R.xml.preference);

        Preference subscriptionPref = findPreference("AssureID_Subscription");
        Preference usernamePref = findPreference("AssureID_Username");
        Preference passwordPref = findPreference("AssureID_Password");
        Preference cloudUrlPref = findPreference("AssureID_Cloud_URL");
        Preference enabledPref = findPreference("AssureID_Enable");

        subscriptionPref.setSummary(sharedPref.getString("AssureID_Subscription",""));
        usernamePref.setSummary(sharedPref.getString("AssureID_Username",""));
        passwordPref.setSummary(sharedPref.getString("AssureID_Password",""));
        cloudUrlPref.setSummary(sharedPref.getString("AssureID_Cloud_URL","https://services.assureid.net/AssureIDService"));


        subscriptionPref.setOnPreferenceChangeListener(this);
        usernamePref.setOnPreferenceChangeListener(this);
        passwordPref.setOnPreferenceChangeListener(this);
        enabledPref.setOnPreferenceChangeListener(this);
        cloudUrlPref.setOnPreferenceChangeListener(this);

    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey()!=null){
            String key = preference.getKey();
            SharedPreferences.Editor editor = sharedPref.edit();
            if(newValue!=null) {
                if (key.equalsIgnoreCase("AssureID_Subscription")) {
                    editor.putString(key, newValue.toString());
                    editor.commit();
                    preference.setSummary(newValue.toString());
                } else if (key.equalsIgnoreCase("AssureID_Username")) {
                    editor.putString(key, newValue.toString());
                    editor.commit();
                    preference.setSummary(newValue.toString());
                } else if (key.equalsIgnoreCase("AssureID_Password")) {
                    editor.putString(key, newValue.toString());
                    editor.commit();
                    preference.setSummary(newValue.toString());
                }else if (key.equalsIgnoreCase("AssureID_Cloud_URL")) {
                    editor.putString(key, newValue.toString());
                    editor.commit();
                    preference.setSummary(newValue.toString());
                } else if (key.equalsIgnoreCase("AssureID_Enable")) {
                    editor.putBoolean(key, Boolean.valueOf(newValue.toString()));
                    editor.commit();
                    boolean enabled = Boolean.valueOf(newValue.toString());
                    int ws = 0;
                    if(enabled){
                        ws = 1;
                    }
                    editor.putInt("WEBSERVICE",ws);
                    editor.commit();
                }
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        boolean enabled = sharedPref.getBoolean("AssureID_Enable",false);
        boolean changed = false;
        if(enabled && previous_webservice==0){
            changed = true;
        }else if(!enabled && previous_webservice==1){
            changed = true;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("WSCHANGED", changed);
        editor.commit();
    }
}
