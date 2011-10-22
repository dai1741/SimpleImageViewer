package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * 設定画面。
 * xmlで完結させたかったがやり方がわからない。
 * 
 * @author dai
 * 
 */
public class ViewerPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // android2ではdeprecated
        addPreferencesFromResource(R.xml.preferences);

        detectPrefenreceStates();
        setResult(RESULT_OK); // 後で変えるかも
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = ViewerPreferenceManager.INSTANCE.getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    SharedPreferences prefs;
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                String key) {
            detectPrefenreceStates();
        }
    };

    private void detectPrefenreceStates() {
        Preference customImageSizePref = findPreference(getResources().getString(
                R.string.pref_key_custom_max_imagesize));
        ListPreference imageSizePref = (ListPreference) findPreference(getResources()
                .getString(R.string.pref_key_max_imagesize));
        customImageSizePref.setEnabled(imageSizePref.getValue().equals(
                Integer.toString(PREF_IMAGESIZE_CUSTOM)));
        ViewerPreferenceManager.INSTANCE.setPrefferedOrientation(this);
    }


}
