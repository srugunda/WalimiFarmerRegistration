package com.solo.walimifarmerregistration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_REQUEST = 1;
    private EditText FarmerName;
    private EditText FarmerID;
    private EditText FarmerAddress;
    private EditText LandTitleNum;
    private EditText GpsCoordinates;
    private EditText CropDescription;
    private Button SaveButton;
    private ImageView LandImage;
    File photoFile = null;
    String mCurrentPhotoPath;
    ProgressDialog progressDialog;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        performInitializations();
    }

    private void performInitializations() {
        FarmerName = findViewById(R.id.Enter_Farmer_Name);
        FarmerID = findViewById(R.id.Enter_Farmer_ID);
        FarmerAddress = findViewById(R.id.Enter_Farmer_Address);
        LandTitleNum = findViewById(R.id.land_title_num);
        GpsCoordinates = findViewById(R.id.gps_coordinates);
        CropDescription = findViewById(R.id.crop_description);
        SaveButton = findViewById(R.id.Farmer_Save_Button);
        LandImage = findViewById(R.id.upload_photo);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String farmerName = FarmerName.getText().toString();
                String farmerID = FarmerID.getText().toString();
                String farmerAddress = FarmerAddress.getText().toString();
                int titleNum = Integer.parseInt(LandTitleNum.getText().toString());
                String gps = GpsCoordinates.getText().toString();
                String cropDescription = CropDescription.getText().toString();

                Farmer farmer = new Farmer(farmerName, farmerID, farmerAddress, String.valueOf(titleNum), gps, cropDescription, photoBitmap);

                saveToDatabase(farmer);

            }
        });

        LandImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    private void captureImage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        else {

            /*this is the basic implmentation (returns thumbnail)
            Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(photoIntent, REQUEST_CODE);
            */

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                // Create the File where the photo should go
                try {

                    photoFile = createImageFile();
                    displayMessage(getBaseContext(),photoFile.getAbsolutePath());
                    Log.i("Absolute path: ",photoFile.getAbsolutePath());

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.solo.walimifarmerregistration.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    displayMessage(getBaseContext(),ex.getMessage().toString());
                }


            }else
            {
                displayMessage(getBaseContext(),"Nullll");
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* This works in the initial code using thumbnail
        Bundle extras = data.getExtras();
        Bitmap imageBitmap  = (Bitmap) extras.get("data");
        LandImage.setImageBitmap(imageBitmap);
         */

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            photoBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            LandImage.setImageBitmap(photoBitmap);
        }
        else
        {
            displayMessage(getBaseContext(),"Request cancelled or something went wrong.");
        }
    }

    private void saveToDatabase(Farmer farmer){

        progressDialog.show();

        DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Farmers").child(farmer.getId()).setValue(farmer)
         .addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                 if (task.isSuccessful()){
                     Toast.makeText(MainActivity.this, "Saved to firebase", Toast.LENGTH_SHORT).show();
                     progressDialog.dismiss();
                 }
                 else{
                     String error = task.getException().toString();
                     Log.d("TAG", "Error: " + error);
                     Toast.makeText(MainActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                     progressDialog.dismiss();
                 }
             }
         });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
            else{
                displayMessage(getApplicationContext(), "App needs camera permission");
            }
        }

    }

    private void displayMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
