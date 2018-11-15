package sp.com.candletorch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                if (MyApps.getInstance().getLogin()) {
                    Intent it = new Intent(getApplicationContext(), HomeActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                    finish();
                } else {
                    Intent it = new Intent(getApplicationContext(), InstructionActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                    finish();
                }

            }
        }, SPLASH_TIME_OUT);
    }


}