package jp.dai1741.android.imgviewer;

import static jp.dai1741.android.imgviewer.ImageViewerConstants.*;

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
        detectPrefenreceDependency();

        Preference imageSizePref = findPreference(getResources().getString(
                R.string.pref_key_max_imagesize));
        imageSizePref
                .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        // detectPrefenreceDependency();
                        findPreference(
                                getResources().getString(
                                        R.string.pref_key_custom_max_imagesize))
                                .setEnabled(
                                        Integer.toString(PREF_IMAGESIZE_CUSTOM).equals(
                                                newValue));
                        return true;
                    }
                });

        setResult(RESULT_OK); // 後で変えるかも
        ViewerPreferenceManager.INSTANCE.setPrefferedOrientation(this);
    }

    private void detectPrefenreceDependency() {
        Preference customImageSizePref = findPreference(getResources().getString(
                R.string.pref_key_custom_max_imagesize));
        ListPreference imageSizePref = (ListPreference) findPreference(getResources()
                .getString(R.string.pref_key_max_imagesize));
        customImageSizePref.setEnabled(imageSizePref.getValue().equals(
                Integer.toString(PREF_IMAGESIZE_CUSTOM)));
    }


}
