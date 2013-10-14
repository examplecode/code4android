package com.mx.demo.tinycrawler;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class CrawlPreferenceActivity extends PreferenceActivity implements
Preference.OnPreferenceChangeListener {
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        Preference e = findPreference(CrawlSettings.PREF_CRAWL_LEVEL);
        ListPreference level = (ListPreference) e;
        level.setValue(CrawlSettings.getInstance().mCrawlLevel +"");
        e.setOnPreferenceChangeListener(this);
       
        e = findPreference(CrawlSettings.PREF_CRAWL_DOWNLOAD_IMAGE);
        CheckBoxPreference c1 = (CheckBoxPreference) e;
        c1.setChecked(CrawlSettings.getInstance().mDownloadImage);
        e.setOnPreferenceChangeListener(this);
        
        e = findPreference(CrawlSettings.PREF_CRAWL_IN_WIFI);
        CheckBoxPreference c2 = (CheckBoxPreference) e;
        c2.setChecked(CrawlSettings.getInstance().mJustCrawlInWifiType);
        e.setOnPreferenceChangeListener(this);
        
        e = findPreference(CrawlSettings.PREF_CRAWL_DATA_DIR);
        EditTextPreference editTextPre = (EditTextPreference) e;
        editTextPre.setText(CrawlSettings.getInstance().mCrawlDataDir);
        e.setOnPreferenceChangeListener(this);
    }

	@Override
	public boolean onPreferenceChange(Preference paramPreference,
			Object paramObject) {
		CrawlSettings.getInstance().loadSetings(this);
		return true;
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		CrawlSettings.getInstance().syncSharedPreferences(
				getPreferenceScreen().getSharedPreferences());
	}
}
