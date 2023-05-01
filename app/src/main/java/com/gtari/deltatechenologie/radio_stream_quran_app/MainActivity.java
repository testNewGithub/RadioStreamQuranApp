package com.gtari.deltatechenologie.radio_stream_quran_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.IslamicCalendar;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;



public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener, Player.EventListener{

    private FloatingActionButton bu_Play;
    private MediaPlayer radioPlayer;
    private SeekBar seekVolume;
    private Button bu_Mute;
    private String stream="http://5.135.194.225:8000/live";
    private float volumNum=0.5f;
    private boolean prepared=false;
    private boolean stated=false;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private Random rand ;
    //exoplayer parametr
    private MediaSource mediaSource;
    private TrackSelector trackSelector;



    private SimpleExoPlayer exoPlayer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocate();
        setContentView(R.layout.radio_stream_layout);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-4168864559615120~6509422746");
        //Banner Ad
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Interstitial Ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4168864559615120/4706166738");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();


        bu_Play= findViewById(R.id.bu_play);
        bu_Play.setEnabled(false);
        bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.loading_bt));


        bu_Mute=findViewById(R.id.bu_mute);

       // radioPlayer= new MediaPlayer();
      //  radioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
       // radioPlayer.setVolume(0.5f,0.5f);
      //  new PlayerTask().execute(stream);
        String userAgent = Util.getUserAgent(getApplicationContext(), "SimpleExoPlayer");
        Uri uri = Uri.parse(stream);
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true);
        // This is the MediaSource representing the media to be played.
        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        trackSelector = new DefaultTrackSelector();

        // Seek Volume Bar
        seekVolume=findViewById(R.id.seekVolume);
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumNum = progress/100f;
                if(stated){
                    exoPlayer.setVolume(volumNum);
                    isMute=false;
                    bu_Mute.setBackgroundResource(R.drawable.mute_btn);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

         rand= new Random();

        loadAudio();
    }


    private void loadAudio() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.prepare(mediaSource);
        prepared=true;
        bu_Play.setEnabled(true);
        bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                bu_Play.setEnabled(false);
                bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.loading_bt));                break;
            case Player.STATE_ENDED:
                bu_Play.setEnabled(true);
                bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));
                break;
            case Player.STATE_IDLE:
                break;
            case Player.STATE_READY:
                break;
            default:
                break;
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if(!isConnected()){
            connexionDialog();
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }


    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-4168864559615120/8557001588",
                new AdRequest.Builder().build());
    }


    public void bu_Play(View view) {
       if(!isConnected()){
            connexionDialog();
            return;
        }

        if(stated){
              stated=false;
         //   radioPlayer.pause();
              exoPlayer.setPlayWhenReady(false);
              //exoPlayer.getPlaybackState();
              bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));

        }else {
            stated=true;
          //  radioPlayer.start();
            if (exoPlayer == null) {
                loadAudio();
                exoPlayer.setPlayWhenReady(true);
                //exoPlayer.getPlaybackState();
            } else {
                exoPlayer.setPlayWhenReady(true);
                //exoPlayer.getPlaybackState();
            }
            bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_pause));
            exoPlayer.setVolume(volumNum);
            if(isMute)
                bu_Mute.setBackgroundResource(R.drawable.mute_btn);
        }
    }



    private boolean isMute=false;
    public void bu_Mute(View view) {

      if(!isMute) {
          exoPlayer.setVolume(0f);
          isMute=true;
          bu_Mute.setBackgroundResource(R.drawable.volum_up);
         }
        else {
          exoPlayer.setVolume(volumNum);
          isMute=false;
          bu_Mute.setBackgroundResource(R.drawable.mute_btn);
        }
    }

    // Share App Button
    public void buShare(View view) {
        dialogLauncher(R.string.share_title,R.string.share_message,1);
    }

    // Rating App
    public void buRate(View view) {
        dialogLauncher(R.string.rate_title,R.string.rate_message,2);
    }

    // Other Apps
    public void buOtehrApps(View view) {
        moreApplication();
    }

    // Share Application method id = 1
    private void shareApplicaion(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_TEXT, "هديّة لكلّ متابعي إذاعة القرآن الكريم التونسيّة, تطبيق إذاعة القرآن الكريم بدون سماعات مـــجـانا على بلاي ستور مع تغطية على كامل التراب التونسي و بجودة عالية.(حجم التطبيق 3 ميغا فـقـط). لا تنسى نشر التطبيق.  "+"https://play.google.com/store/apps/details?id=com.gtari.deltatechenologie.radio_stream_quran_app");
        startActivity(Intent.createChooser(shareIntent, getString((R.string.share_with))));
    }

    // Rate Application method id = 2
    private void rateApplication(){
        Uri marketUri = Uri.parse("market://details?id="+"com.gtari.deltatechenologie.radio_stream_quran_app");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        startActivity(marketIntent);
    }

    // More Appliaction method
    private void moreApplication(){
        Intent intent=new Intent(this,MoreApplications.class);
        startActivity(intent);
    }


    // Dialog
    private void dialogLauncher(int idTitle, final int idMessage, final int idTaskToDo ){

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        TextView messageDialog=(TextView)dialog.findViewById(R.id.message);
        final TextView titleDialog=(TextView)dialog.findViewById(R.id.title);
        titleDialog.setText(idTitle);
        messageDialog.setText(idMessage);
        dialog.show();
        //RunAnimation(R.id.idShinePermission,R.anim.internet_button_anim);

        Animation anim = AnimationUtils.loadAnimation(this,R.anim.dialog_anim);
        final ImageView shine =(ImageView)dialog.findViewById(R.id.idShinePermission);
        shine.startAnimation(anim);

        // Allow
        dialog.findViewById(R.id.positive_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // select tache
                switch (idTaskToDo){
                    case 1 : shareApplicaion();
                    break;
                    case 2 : rateApplication();
                    break;
                    default: shareApplicaion();
                }
                dialog.cancel();
                shine.clearAnimation();
            }
        });

        // Cancel
        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                shine.clearAnimation();
            }

        });

    }

    public void bu_Quit(View view) {
        cancelDialog();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        // Load the next rewarded video ad.
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }


/*
    private class PlayerTask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                radioPlayer.setDataSource(strings[0]);
               // radioPlayer.prepare();
                prepared=true;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
           // bu_Play.setEnabled(true);
           // bu_Play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));

        }

    }
*/

    // Menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id ==R.id.idPlayStoreApps ){
            moreApplication();
        }

        if(id ==R.id.idLangauge ){
            languageSelection();
        }

        if(id ==R.id.idPrivacyPolicy ){
            privacyPolicyDialog();
        }

        return super.onOptionsItemSelected(item);
    }


    // ***Change language part:***//

    //select Locale
    private void setLocale(String language){
        Locale locale=new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale=locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        // /Save Date For Next Time
        SharedPreferences.Editor editor=getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Language",language);
        editor.apply();
    }

    // Load locale language
    private void loadLocate(){
        SharedPreferences perfers=getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        if(!perfers.contains("My_Language")) {
            return;
        }

        String language=perfers.getString("My_Language","");
        setLocale(language);
    }


    // Launch language selection dialog
    public void languageSelection() {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.dialog_language_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();


        //Select Application Language
        dialog.findViewById(R.id.buttonFrensh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("fr");
                dialog.cancel();
                recreate();
            }

        });

        dialog.findViewById(R.id.buttonArabic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("ar");
                dialog.cancel();
                recreate();
            }

        });

        dialog.findViewById(R.id.buttonEnglish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocale("en-us");
                dialog.cancel();
                recreate();
            }

        });

        dialog.findViewById(R.id.close_language_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }

        });

    }


    // Launch Privacy Policy
    private void privacyPolicyDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.privacy_policy));

        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/privacy_policy.html");
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        alert.setView(wv);
        alert.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    // Quit App Dialog Launcher

    private void cancelDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.cancel_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();


        //Launch Full Screen Ad
        //mInterstitialAd.loadAd(new AdRequest.Builder().build());
        // loadRewardedVideoAd();

        final int n=rand.nextInt(2);  // Gives n such that 0 <= n < 2

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(n==0) {mInterstitialAd.show();}
                else{if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();}
                }

            }
        },300); // Millisecond 1000 = 1 sec



        //Dialog Alert
        dialog.findViewById(R.id.positive_button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }

        });

        dialog.findViewById(R.id.cancel_button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                finish();
            }

        });

        dialog.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }

        });

    }


    //Get Connectivity State
    private boolean isConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Connexion State Alert
    private void connexionDialog() {

            final Dialog dialog1 = new Dialog(this);
            dialog1.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog1.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            dialog1.setContentView(R.layout.internet_state_dialog);
            dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog1.setCancelable(true);
            dialog1.show();

            dialog1.findViewById(R.id.idConnectionButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.cancel();
                }

            });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRewardedVideoAd.pause(this);
        if(stated){
            //radioPlayer.pause();
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);
        if(stated){
            //radioPlayer.start();
            if (exoPlayer == null) {
                loadAudio();
                exoPlayer.setPlayWhenReady(true);
            } else {
                exoPlayer.setPlayWhenReady(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRewardedVideoAd.destroy(this);
        if(prepared){
            exoPlayer.release();
        }
    }

    @Override
    public void onBackPressed() {
        cancelDialog();
    }


}

