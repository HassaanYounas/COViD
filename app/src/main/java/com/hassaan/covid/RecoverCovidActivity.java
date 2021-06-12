package com.hassaan.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class RecoverCovidActivity extends AppCompatActivity {

    ImageView backBtnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_covid);

        backBtnImage = findViewById(R.id.backBtnImage);
        backBtnImage.setOnClickListener(v -> {
            finish();
        });
    }
}