package com.GreenLemonMobile.RssReader;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.GreenLemonMobile.RssReader.entity.FeedItemEntity;
import com.GreenLemonMobile.RssReader.entity.UpgradeInfo;
import com.GreenLemonMobile.RssReader.service.SubscribedFeedService;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);
        setTitle(R.string.setting_activity_title_text);
        addPreferencesFromResource(R.xml.setting);
        
        setupView();
    }

    private void setupView() {
        String currentVersion = "";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            currentVersion = pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        String format = getResources().getString(R.string.pref_version_value_format_text);
        currentVersion = String.format(format, currentVersion);
        
        getPreferenceScreen().findPreference("system_setting_update").setSummary(currentVersion);
        
        getPreferenceScreen().findPreference("system_setting_textsize").setOnPreferenceChangeListener(this);
        
        setupFontSize(null);
    }
    
    private void setupFontSize(Object newValue) {
        String fontText = "";
        String fontSize = (newValue == null) ? PreferenceManager.getDefaultSharedPreferences(this).getString(
                "system_setting_textsize", "0") : (String)newValue;        
        if (fontSize.equals("-1"))
            fontText = getResources().getString(R.string.setting_text_size_entryvalue_4);
        else if (fontSize.equals("0"))
            fontText = getResources().getString(R.string.setting_text_size_entryvalue_3);
        else if (fontSize.equals("1"))
            fontText = getResources().getString(R.string.setting_text_size_entryvalue_2);
        else if (fontSize.equals("2"))
            fontText = getResources().getString(R.string.setting_text_size_entryvalue_1);
        
        String format = getResources().getString(R.string.pref_text_size_format_text);
        fontText = String.format(format, fontText);
        getPreferenceScreen().findPreference("system_setting_textsize").setSummary(fontText);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("system_setting_about")) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (preference.getKey().equals("system_setting_update")) {
            UpgradeInfo.checkUpdate(this, false);
        } else if (preference.getKey().equals("system_setting_clear_cache")) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getText(R.string.clear_cache_tip_text));
            progressDialog.show();

            new Thread() {

                @Override
                public void run() {
                    FeedItemEntity.clearAll();
                    ComponentName component = new ComponentName(SettingsActivity.this, SubscribedFeedService.class);
                    Intent intent = new Intent(SubscribedFeedService.IMM_UPDATE_SERVICE);
                    intent.setComponent(component);
                    startService(intent);
                    progressDialog.dismiss();
                    super.run();
                }

            }.start();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("system_setting_textsize")) {
            setupFontSize(newValue);
        } else if (preference.getKey().equals("system_setting_auto_refresh")) {
        }
        return true;
    }

}
