package jp.dai1741.android.imgviewer;

import android.app.Application;
import android.preference.PreferenceManager;

public class ImageViewerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初回起動時にデフォルト設定をセット
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        ViewerPreferenceManager.INSTANCE.setContext(this);
    }

}
