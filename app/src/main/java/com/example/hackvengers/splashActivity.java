package com.example.hackvengers;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hackvengers.LoginActivity;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class splashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent in;
            in = new Intent(splashActivity.this,LoginActivity.class);
            startActivity(in);
            finish();
        }, 2000);

    }
}