package sp.com.candletorch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.DataOutputStream;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    // Candle Data
    ImageView smokeImage;
    ImageView flameImage;
    RelativeLayout relative;

    private AudioRecord mRecorder;
    private Context context;
    private Camera camera;
    private Camera.Parameters parameters;
    private Handler handler;

    private static int[] mSampleRates = new int[]{44100, 22050, 11025, 16000, 8000};
    private short[] mBuffer;
    public static final int SAMPLE_RATE = 16000;
    private final static int max = 8000;
    static int count = 0;

    private boolean mIsRecording = true;
    boolean isFlashLightOn = false;
    boolean flame = false;
    // Audio Data
    private AdView mAdView;
    public static boolean isRestarted = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        Log.e("torch", "on create");
        MyApps.getInstance().setLogin(true);
        bindView();
        init();
        initializeADS();
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flame && isFlashLightOn) {
                    stopRecording();
                    startSmokeAnimation();
                    offFlashLight();
                } else {
                    startRecording();
                    startFlamAnimation();
                    setFlash();
                }
            }
        });
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdClosed() {
                Log.e("ads", "Ad is closed!");

//                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.e("ads", "Ad failed to load! error code: " + errorCode);
                mAdView.loadAd(adRequest);

//                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                Log.e("ads", "Ad left application!");

                // Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });

        mAdView.loadAd(adRequest);
    }

    private InterstitialAd mInterstitialAd;

    private void initializeADS() {
        MobileAds.initialize(this,
                "ca-app-pub-8219976241333499~2146989028");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8219976241333499/1791975232");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                mInterstitialAd.show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        setRecorder();
        setFlash();
        startFlamAnimation();
        startRecording();

    }

    private boolean isFlashSupported() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void setFlash() {
        if (isFlashSupported()) {
            camera = null;
            if (camera == null) {
                try {
                    camera = Camera.open();
                    parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();
                } catch (RuntimeException e) {
                    Log.e("torch", "Camera Error. Failed to Open. Error: " + e.getMessage());
                }
            }
            isFlashLightOn = true;
        } else {
            //  showNoFlashAlert();
        }

    }

    private void startRecording() {
        if (mRecorder == null)
            mRecorder = findAudioRecord();
        try {
            mRecorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            mRecorder = findAudioRecord();
        }
        mIsRecording = true;
        startBufferedWrite();

    }

    private void startBufferedWrite() {
        new Thread(new Runnable() {
            public void run() {
                DataOutputStream output = null;
                try {
                    ///output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    while (mIsRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            //output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            final int progress = (int) Math.sqrt(amplitude);

                            Log.e("record", " progress " + progress);
                            if (progress > max) {
                                count++;
                                handler.post(new Runnable() {

                                    public void run() {
                                        // TODO Auto-generated method stub

                                        if (20000 < progress && progress < 31000) {

                                            if (flame) {
                                                // genrate smoke
                                                startSmokeAnimation();
                                                offFlashLight();
//                                                flameImage.setVisibility(View.GONE);
//                                                smokeImage.setVisibility(View.VISIBLE);
//                                                flame = false;
                                            } else {
                                                // genrate flame

//												flameImage.setVisibility(View.VISIBLE);
//												smokeImage.setVisibility(View.GONE);
//												flame= true;

                                            }


                                        } else {

                                            count = 0;
                                        }

                                    }
                                });
                            }
                        } else {
                            if (!isRestarted) {
                                isRestarted = true;
                                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, 0);
                            }
                            Log.e("handler", " else  size 0");
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {

                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {

                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
//                                    .show();
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {

                                e.printStackTrace();
//                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT)
//                                        .show();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void setRecorder() {
        mRecorder = findAudioRecord();
    }

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {
                        Log.e("recording", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig);

                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        mBuffer = new short[bufferSize];

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("recording", rate + " Exception, keep trying." + e);
                    }
                }
            }
        }
        return null;
    }


    private void init() {
        handler = new Handler();
    }

    private void bindView() {
        flameImage = (ImageView) findViewById(R.id.aniView);
        smokeImage = (ImageView) findViewById(R.id.aniView2);
        relative = (RelativeLayout) findViewById(R.id.relative_two);
        flameImage.setBackgroundResource(R.drawable.flag);
        smokeImage.setBackgroundResource(R.drawable.flag2);
    }

    public void startFlamAnimation() {
        flameImage.setVisibility(View.VISIBLE);
        smokeImage.setVisibility(View.GONE);
        flame = true;
        final AnimationDrawable frameAnimation = (AnimationDrawable) flameImage.getBackground();
        flameImage.post(new Runnable() {
            public void run() {
                frameAnimation.start();
            }
        });

    }

    public void startSmokeAnimation() {

        flameImage.setVisibility(View.GONE);
        smokeImage.setVisibility(View.VISIBLE);
        flame = false;
        final AnimationDrawable frameAnimation2 = (AnimationDrawable) smokeImage.getBackground();
        smokeImage.post(new Runnable() {
            public void run() {
                frameAnimation2.start();
            }
        });

    }

    public void stopRecording() {

        mIsRecording = false;
        if (mRecorder != null) {
            if (mIsRecording) {
                mRecorder.stop();
            }
            mRecorder.release();
        }
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void offFlashLight() {
        if (camera == null || parameters == null) {
            return;
        }
        try {
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            camera.release();
            Log.i("info", "torch is turn off!");

            isFlashLightOn = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }


    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
