package com.gtari.deltatechenologie.radio_stream_quran_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MoreApplications extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_application_layout);
    }

    public void buDownloadRechrgerFacilement(View view) {
        playStoreApps("com.gtari.deltatechenologie.rechargerfacilement");
    }

    public void buDownloadKolFiKol(View view) {
        playStoreApps("com.gtari.deltatechenologie.elkolxelkol");
    }

    private void playStoreApps(String app_id) {
        Uri marketUri = Uri.parse("market://details?id=" + app_id);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        startActivity(marketIntent);
    }

    public void buBack(View view) {
        onBackPressed();
    }


    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
