package com.dulemata.emiliano.biker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dulemata.emiliano.biker.util.Keys;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                Intent intent;
                if (getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE).getBoolean(Keys.AUTO_LOGIN, false)) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        };
        thread.run();
    }
}
