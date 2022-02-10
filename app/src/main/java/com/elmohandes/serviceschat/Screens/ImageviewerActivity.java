package com.elmohandes.serviceschat.Screens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.elmohandes.serviceschat.R;
import com.squareup.picasso.Picasso;

public class ImageviewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String receivedUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

        getSupportActionBar().hide();

        imageView = findViewById(R.id.imageviewer);
        receivedUrl = getIntent().getStringExtra("imgUrl");

        Picasso.get().load(receivedUrl).placeholder(R.drawable.person_or_avatar)
                .into(imageView);

    }
}