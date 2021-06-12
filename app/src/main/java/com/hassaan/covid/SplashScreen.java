package com.hassaan.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    Handler handler;
    TextView brandingText;
    Animation topAnim, textAnim;
    ImageView topImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        brandingText = findViewById(R.id.branding);
        SpannableStringBuilder spannable = new SpannableStringBuilder("COViD");
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#FF82D3")),
                3,
                4,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        );
        brandingText.setText(spannable);
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        textAnim = AnimationUtils.loadAnimation(this, R.anim.text_anim);
        brandingText.setAnimation(textAnim);
        topImage = findViewById(R.id.topImage);
        topImage.setAnimation(topAnim);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },600);
    }
}