package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout1);

        // for tests purposes only because Android does not allow the app access to storage area which outside the app
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // show kram from directory
        String path = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_PICTURES + "/" + getResources().getString(R.string.app_name);
        Log.d("Info", path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i=0; i < files.length; i++) {
            Log.e("Info", files[i].getName());
            final ImageView imageView = new ImageView(getApplicationContext());
            Uri localUri = Uri.fromFile(files[i].getAbsoluteFile());
            imageView.setImageURI(localUri);
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(260,300));
            imageView.setMaxHeight(20);
            imageView.setMaxWidth(40);
            layout.addView(imageView);
        }
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    imageFile = createImageFile();
                    cameraIntent(imageFile);
                }
                catch (IOException ioe) {
                    Log.e("onCreate", "Error creating Image File");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent iGoToFullScreen = new Intent(this, FullscreenActivity.class);
        iGoToFullScreen.putExtra("picture", imageFile);
        startActivity(iGoToFullScreen);
    }

    private void cameraIntent(File imageFile) {
        Intent iOpenCamera = new Intent();
        iOpenCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            if (imageFile.exists()) {
                Uri outuri = Uri.fromFile(imageFile);
                iOpenCamera.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                startActivityForResult(iOpenCamera, 2);
            }

    }

    private File createImageFile() throws IOException {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File storeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File galleryDir = new File(storeDir, getResources().getString(R.string.app_name));

            if (!galleryDir.exists()) {
                boolean createdDir = galleryDir.mkdirs();

                if (!createdDir) {
                    Toast.makeText(getApplicationContext(), "Unable to create Directory. The image cannot be displayed in its original quality", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }

            return File.createTempFile("ms_image", ".png", galleryDir);

        } else {
            Toast.makeText(getApplicationContext(), "No permissions to write to data storage. Aborting...", Toast.LENGTH_SHORT).show();
            return null;
        }
    }



}
