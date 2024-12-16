package com.van1164.photoviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.van1164.photoviewer.detail.DetailActivity;
import com.van1164.photoviewer.dto.PhotoResponseDto;
import com.van1164.photoviewer.imageview.ImageAdapter;
import com.van1164.photoviewer.upload.UploadActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private OkHttpClient client;
    private WebSocket webSocket;
    private TextToSpeech tts;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    ImageView imgView;
    TextView textView;
//    String site_url = "https://van133.pythonanywhere.com";
    String site_url = "https://van133.pythonanywhere.com";
    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;
    CloadImage taskDownload;
    private boolean isSoundOn = true; // 소리 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //imgView = (ImageView) findViewById(R.id.imgView);
        textView = (TextView) findViewById(R.id.textView);
        tts = new TextToSpeech(this, this);
        // 초기 버튼 상태 설정
        ImageButton soundToggleButton = findViewById(R.id.btn_sound_toggle);
        updateSoundButtonState(soundToggleButton);

        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://van133.pythonanywhere.com/ws/chat/") // Django 서버의 WebSocket URL
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("DDDDDDDDDDDDDDDDDDD", "WebSocket 연결 성공");
            }


            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                String decodedMessage = decodeMessage(text);
                Log.d("ZZZZZZZZZZZZZZZZZZZ",decodedMessage);

                // WebSocket 메시지 수신 시 TTS로 읽기
                runOnUiThread(() -> speak(decodedMessage));
                runOnUiThread(() -> onClickDownload(textView));
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                t.printStackTrace();
            }
        });
    }
    // 버튼 클릭 이벤트 처리
    public void onSoundToggleClick(View view) {
        isSoundOn = !isSoundOn; // 소리 상태 변경
        ImageButton soundToggleButton = (ImageButton) view;
        updateSoundButtonState(soundToggleButton);
    }

    // 버튼 상태 업데이트 메서드
    private void updateSoundButtonState(ImageButton button) {
        if (isSoundOn) {
            button.setImageResource(R.drawable.baseline_volume_up_150); // 소리 켜짐 아이콘
            speak("소리가 켜졌습니다.");
        } else {
            button.setImageResource(R.drawable.baseline_volume_off_150); // 소리 꺼짐 아이콘
            speakForce("소리가 꺼졌습니다.");
        }
    }
    private String decodeMessage(String text) {
        try {
            // JSON 파싱 및 UTF-8 디코딩
            org.json.JSONObject jsonObject = new org.json.JSONObject(text);
            return jsonObject.getString("message"); // "message" 필드 추출
        } catch (Exception e) {
            return text; // 실패 시 원본 반환
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 언어 설정 (예: 한국어)
            int result = tts.setLanguage(Locale.KOREAN);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 언어 데이터가 없거나 지원되지 않는 경우
                System.out.println("지원되지 않는 언어입니다.");
            } else {
                // Text-to-Speech 준비 완료
//                speak("안녕하세요! 이것은 텍스트를 음성으로 변환하는 테스트입니다.");
            }
        } else {
            System.out.println("TTS 초기화 실패");
        }
    }
    private void speak(String text) {
        if (tts != null && isSoundOn) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
    private void speakForce(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        // TTS 리소스 해제
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Upload", Toast.LENGTH_LONG).show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<PhotoResponseDto>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);  // Show progress bar
        }

        @Override
        protected List<PhotoResponseDto> doInBackground(String... urls) {
            List<PhotoResponseDto> photoResponseDtoList = new ArrayList<>();
            try {
                String apiUrl = urls[0];
                String token = "c41bca6413639ae1a167df5b3e3cda66ee27f94c";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                System.out.println("SSSSSSSSSSSSSSS"+responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);
// 배열 내 모든 이미지 다운로드
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        Log.d("WWWWWWWWWWWWWWWWWWWWW",post_json.toString());
                        imageUrl = post_json.getString("image");
                        String title = post_json.getString("title");
                        String text = post_json.getString("text");
                        String author = post_json.getString("author");
                        String createdDate = post_json.getString("created_date");
                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            photoResponseDtoList.add(new PhotoResponseDto(imageBitmap,title,text,author,createdDate)); // 이미지 리스트에 추가
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            Collections.reverse(photoResponseDtoList);
            return photoResponseDtoList;
        }

        @Override
        protected void onPostExecute(List<PhotoResponseDto> photoResponseDtoList) {
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            if (photoResponseDtoList.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공!");
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(photoResponseDtoList, photo -> {
                    try {
                        File file = new File(recyclerView.getContext().getCacheDir(), "image_" + photo.getTitle() + ".png");
                        FileOutputStream fos = new FileOutputStream(file);
                        photo.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();

                        // Create an intent to navigate to DetailActivity
                        Intent intent = new Intent(recyclerView.getContext(), DetailActivity.class);

                        // Pass the URI of the saved image file instead of the Bitmap
                        intent.putExtra("imageUri", Uri.fromFile(file).toString());
                        intent.putExtra("title", photo.getTitle());
                        intent.putExtra("text", photo.getText());
                        intent.putExtra("author", photo.getAuthor());
                        intent.putExtra("createdDate", photo.getCreatedDate());
                        // Start DetailActivity
                        recyclerView.getContext().startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(recyclerView.getContext(), "Clicked on: " + photo.getTitle(), Toast.LENGTH_SHORT).show();
                });
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }
}
