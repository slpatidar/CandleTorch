package sp.com.candletorch;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.ads.MobileAds;

public class MyApps extends Application {
    private static MyApps instance;
    private static boolean activityVisible;

    public String KEY_LOGIN = "login";
    SharedPreferences.Editor editor;
    public static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        sharedPreferences = getSharedPreferences("candle", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        instance = this;
    }

    public static MyApps getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
        // or return instance.getApplicationContext();
    }

    public void setLogin(Boolean value) {
        // Storing login value as TRUE

        editor.putBoolean(KEY_LOGIN, value);
        // commit changes
        editor.commit();

    }

    public Boolean getLogin() {
        Boolean vjson = false;
        try {
            vjson = sharedPreferences.getBoolean(KEY_LOGIN, false);
        } catch (Exception e) {

        }
        return vjson;
    }

}
