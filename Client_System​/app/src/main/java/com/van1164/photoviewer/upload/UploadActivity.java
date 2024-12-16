package com.van1164.photoviewer.upload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.van1164.photoviewer.R;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView selectedImage;
    private EditText etTitle, etText, etAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());
        selectedImage = findViewById(R.id.selectedImage);
        etTitle = findViewById(R.id.etTitle);
        etText = findViewById(R.id.etText);

        findViewById(R.id.btnSelectImage).setOnClickListener(view -> selectImageFromGallery());
        findViewById(R.id.btnUpload).setOnClickListener(view -> new UploadTask().execute());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click here
            finish(); // Finish UploadActivity and return to MainActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {
        String token = "b37773d5ad2d82b029b1a2b21d4b92d70ce1eb6b";
        @Override
        protected String doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://van133.pythonanywhere.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService apiService = retrofit.create(ApiService.class);
            try {
                // Convert image Uri to RequestBody
                RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), etTitle.getText().toString());
                RequestBody textPart = RequestBody.create(MediaType.parse("text/plain"), etText.getText().toString());

                // Prepare the image part
                RequestBody imageRequestBody = getImageRequestBody(UploadActivity.this, imageUri);
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", getFileName(UploadActivity.this, imageUri), imageRequestBody);

                // Make synchronous Retrofit call (for use in AsyncTask)
                Call<ResponseBody> call = apiService.uploadImage("Token "+token,titlePart, textPart, imagePart);
                Response<ResponseBody> response = call.execute();

                // Check response
                if (response.isSuccessful()) {
                    return "Upload Successful";
                } else {
                    Log.e("Upload Error", response.message());
                    return "Upload Failed";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Upload Failed";
            }
        }

        public RequestBody getImageRequestBody(Context context, Uri uri) {
            try {
                // Open an InputStream from the Uri
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                // Create and return RequestBody
                return RequestBody.create(MediaType.parse("image/*"), buffer);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressLint("Range")
        public String getFileName(Context context, Uri uri) {
            String result = null;
            if (uri.getScheme().equals("content")) {
                try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                }
            }
            if (result == null) {
                result = uri.getLastPathSegment();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!result.isEmpty()) {
                Toast.makeText(UploadActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(UploadActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
