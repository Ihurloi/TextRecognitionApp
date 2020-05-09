package com.example.textrecognitionapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
private Button captureImageBtn, detectTextBtn ;
private ImageView imageView ;
private TextView textView ;
static final int REQUEST_IMAGE_CAPTURE = 1;
private Bitmap imageBitmap;
private boolean imageCaptured=false;
private Button galleryBtn;
private static final int IMAGE_PICK_CODE = 1000;
private static final int PERMISSION_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureImageBtn = findViewById(R.id.capture_image_btn);
        detectTextBtn = findViewById(R.id.detect_text_image_btn);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);
        galleryBtn = findViewById(R.id.gallery);

        captureImageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                      imageCaptured=true;
                      dispatchTakePictureIntent();
                      textView.setText("");
            }
        });
        detectTextBtn.setOnClickListener((new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(imageCaptured)
                    detectTextFromImage();
            }
        }));

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pickImageFromGallery();
                imageCaptured = true;
            }
        });

        }

    private void pickImageFromGallery()
    {
      Intent intent = new Intent(Intent.ACTION_PICK);
      intent.setType("image/*");
      startActivityForResult(intent, IMAGE_PICK_CODE);

    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == RESULT_OK))
            {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }

            if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE)
            {
                Uri imageUri = data.getData();
                try {
                     imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageURI(imageUri);
            }
        }

    private void detectTextFromImage()
    {
                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText)
            {
                 displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error: " , Objects.requireNonNull(e.getMessage()));

                }
        });
        }


    @SuppressLint("ShowToast")
    private void displayTextFromImage(FirebaseVisionText firebaseVisionText)
    {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0)
        {
            Toast.makeText(this, "No Text Found in Image",Toast.LENGTH_SHORT);
        }
        else
            {
                for(FirebaseVisionText.Block block: firebaseVisionText.getBlocks())
                {
                    String text = block.getText();
                    System.out.println(text);
                    textView.setText(text);
                }
        }

    }
}

