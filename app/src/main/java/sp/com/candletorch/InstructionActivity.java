package sp.com.candletorch;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class InstructionActivity extends Activity {
    private ImageView gobtn;
    Context context;

    final int PERMISSION_ALL = 1;
    final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // To make activity full screen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        context = this;
        gobtn = findViewById(R.id.gobtn);
        gobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    if (!hasPermissions(context, PERMISSIONS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(InstructionActivity.this);
                        builder.setTitle("Need To Permission");
                        builder.setMessage(getResources().getString(R.string.message));
                        builder.setPositiveButton("Allow permission", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(InstructionActivity.this, PERMISSIONS, PERMISSION_ALL);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                finish();
                            }
                        });
                        builder.show();
                    } else {
                        Intent it = new Intent(getApplicationContext(), HomeActivity.class);
                        it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(it);
                        finish();
                    }
                } else {

                    Intent it = new Intent(getApplicationContext(), HomeActivity.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(it);
                    finish();

                }
            }
        });
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.e("ads", "Ad loaded!");

            }

            @Override
            public void onAdClosed() {
                Log.e("ads", "Ad is closed!");
                //  Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("ads", "Ad failed to load! error code: " + errorCode);
                mAdView.loadAd(adRequest);

                //Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                Log.e("ads", "Ad left application!");

                //Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_ALL:

                if (grantResults.length > 0) {
                    ArrayList<Integer> DENIIED_PERMISSIONS = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            DENIIED_PERMISSIONS.add(grantResults[i]);
                        }
                    }
                    if (DENIIED_PERMISSIONS.size() > 0) {
                        Toast.makeText(InstructionActivity.this, getResources().getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(InstructionActivity.this, getResources().getString(R.string.permission_granted), Toast.LENGTH_LONG).show();
                        Intent it = new Intent(getApplicationContext(), HomeActivity.class);
                        it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(it);
                        finish();
                    }
                }

                break;
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
