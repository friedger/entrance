package de.friedger.entrance;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import de.friedger.entrance.lib.Const;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SCAN = 1;
    private String ipaddress;
    private ToneGenerator toneG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        ((Button) findViewById(R.id.scan_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateScan();
            }
        });

        ipaddress = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_key_ip_address), null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initiateScan() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, REQUEST_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SCAN:
                if (resultCode == RESULT_OK) {
                    onScanResult(data);
                }
        }
    }

    private void onScanResult(Intent data) {
        broadcast(data.getStringExtra("SCAN_RESULT"));
    }

    private void broadcast(final String text) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(String.format("http://%s:%s?%s",ipaddress, Const.PORT, URLEncoder.encode(text, "utf-8")));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    } else {
                        toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    toneG.startTone(ToneGenerator.TONE_CDMA_CALLDROP_LITE, 200);
                }
            }
        }).start();

    }
}
