package com.solo.walimifarmerregistration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_REQUEST = 1;
    private EditText FarmerName;
    private EditText FarmerID;
    private EditText FarmerAddress;
    private EditText LandTitleNum;
    //private EditText GpsCoordinates;
    private EditText CropDescription;
    private TextView Latitude, Longitude, Town;
    private Button SaveButton;
    private Button GetGPSButton;
    private ImageView LandImage;
    File photoFile = null;
    String mCurrentPhotoPath;
    ProgressDialog progressDialog;
    private Bitmap photoBitmap;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager ;
    boolean GpsStatus ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        performInitializations();

        turnOnGps();
    }

    private void showGpsDialog(){
        AlertDialog.Builder myAlertBuilder = new
                AlertDialog.Builder(MainActivity.this);

        // Set the dialog title and message.
        myAlertBuilder.setTitle("Turn on GPS");
        myAlertBuilder.setMessage("Click OK to continue, or Cancel to stop:");

        // Add the dialog buttons.
        myAlertBuilder.setPositiveButton("OK", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked OK button.
                        Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent1);
                    }
                });
        myAlertBuilder.setNegativeButton("Cancel", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled the dialog.
                        Toast.makeText(getApplicationContext(), "You will need to turn on GPS " +
                                        "to retrieve location co-ordinates",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Create and show the AlertDialog.
        myAlertBuilder.show();
    }

    public void turnOnGps(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(GpsStatus == true) {
            //do nothing
        }
        else {
              new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showGpsDialog();
                }
            },2000);
        }
    }

    private void performInitializations() {
        FarmerName = findViewById(R.id.Enter_Farmer_Name);
        FarmerID = findViewById(R.id.Enter_Farmer_ID);
        FarmerAddress = findViewById(R.id.Enter_Farmer_Address);
        LandTitleNum = findViewById(R.id.land_title_num);
        //GpsCoordinates = findViewById(R.id.gps_coordinates1);
        CropDescription = findViewById(R.id.crop_description);
        SaveButton = findViewById(R.id.Farmer_Save_Button);
        LandImage = findViewById(R.id.upload_photo);
        GetGPSButton = findViewById(R.id.get_gps_button1);
        Latitude = findViewById(R.id.latitude);
        Longitude = findViewById(R.id.longitude);
        //Town = findViewById(R.id.town);

        //Initialize fused location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving information...");

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String farmerName = FarmerName.getText().toString();
                String farmerID = FarmerID.getText().toString();
                String farmerAddress = FarmerAddress.getText().toString();
                int titleNum = Integer.parseInt(LandTitleNum.getText().toString());
                String mLatitude = Latitude.getText().toString();
                String mLongitude = Longitude.getText().toString();
                String cropDescription = CropDescription.getText().toString();

                //Farmer farmer = new Farmer(farmerName, farmerID, farmerAddress, String.valueOf(titleNum), mLatitude, mLongitude, cropDescription, photoBitmap);
                Farmer farmer = new Farmer(farmerName, farmerID, farmerAddress, String.valueOf(titleNum), mLatitude, mLongitude, cropDescription);

                saveToDatabase(farmer);

            }
        });

        LandImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        GetGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permission

                if(ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                    //when permission granted
                    getLocation();
                }else{
                    //when permission denied
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }

            }
        });
    }

    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location

                Location location = task.getResult();
                if (location!=null){
                    try {
                        //initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(),1);

                         //Set latitutude, longitutde and town on Textview
                        Latitude.setText(String.valueOf(addresses.get(0).getLatitude()));
                        Longitude.setText(String.valueOf(addresses.get(0).getLongitude()));
                        //displayMessage(MainActivity.this, "Retrieved location sucessfully");
                        //Town.setText(addresses.get(0).getLocality());
                        //textView4.setText(addresses.get(0).getCountryName());
                        //textView4.setText(addresses.get(0).getAddressLine(0));

                        //logging
                        Log.d("Latitude: ", String.valueOf(addresses.get(0).getLatitude()));
                        Log.d("Longitude: ", String.valueOf(addresses.get(0).getLongitude()));
                        Log.d("Locality: ", addresses.get(0).getLocality());

                    } catch (IOException e){
                        e.printStackTrace();
                    }

                }
                else{
                    //When result is null, initialize location request
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    //Initialize location callback
                    LocationCallback locationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            Location location1 = locationResult.getLastLocation();
                            Latitude.setText(String.valueOf(location1.getLatitude()));
                            Longitude.setText(String.valueOf(location1.getLongitude()));
                            //Longitude.setText(String.valueOf(addresses.get(0).getLongitude()));
                            //Town.setText(addresses.get(0).getLocality());
                        }
                    };

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback, Looper.myLooper());
                    //displayMessage(MainActivity.this, "Location is null");
                }
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
                     Toast.makeText(MainActivity.this, "Saved Farmer to DB", Toast.LENGTH_SHORT).show();
                     clearFields();
                     progressDialog.dismiss();
                     //clearFields();
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

    private void clearFields() {
        FarmerName.setText(""); FarmerID.setText("");
        FarmerAddress.setText(""); LandTitleNum.setText("");
        CropDescription.setText(""); Latitude.setText("");
        Longitude.setText("");
        //LandImage.setImageDrawable(getResources().getDrawable(R.id.upload_photo);
        LandImage.setImageResource(R.drawable.ic_add_photo);
        //LandImage.setImageBitmap();
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
