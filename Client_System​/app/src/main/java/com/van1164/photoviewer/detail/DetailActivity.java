package com.van1164.photoviewer.detail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.van1164.photoviewer.R;

public class DetailActivity extends AppCompatActivity {
    private int likeCount = 0;  // 좋아요 수 초기값

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView imageView = findViewById(R.id.detailImageView);
        TextView titleView = findViewById(R.id.detailTitle);
        TextView textView = findViewById(R.id.detailText);
        TextView authorView = findViewById(R.id.detailAuthor);
        TextView createdDateView = findViewById(R.id.detailCreatedDate);
        TextView likeCountTextView = findViewById(R.id.likeCountTextView);  // 좋아요 수 텍스트뷰 초기화
        Button likeButton = findViewById(R.id.likeButton);

        // Retrieve data from the Intent
        Intent intent = getIntent();
        String imageUriString = intent.getStringExtra("imageUri");
        Uri imageUri = Uri.parse(imageUriString);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        String author = intent.getStringExtra("author");
        String createdDate = intent.getStringExtra("createdDate");

        // Load the image from the URI
        imageView.setImageURI(imageUri);
        titleView.setText(title);
        textView.setText(text);
        authorView.setText(author);
        createdDateView.setText(createdDate);

        likeCountTextView.setText("좋아요 수 : " + likeCount);  // 초기 좋아요 수 설정

        // Like button click event
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeCount++;
                likeCountTextView.setText("좋아요 수 : " + likeCount);
            }
        });
    }

}
