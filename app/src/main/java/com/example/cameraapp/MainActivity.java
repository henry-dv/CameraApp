package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private File imageFile;
    private int image_counter = 0;
    private com.google.android.flexbox.FlexboxLayout flexlayout;
    private String directory_path;
    private int number_of_images = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        flexlayout = (FlexboxLayout) findViewById(R.id.flexbox1);

        // for tests purposes only because Android does not allow the app access to storage area outside of its own area
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Pull-to-Refresh
        final SwipeRefreshLayout pull_to_refresh = findViewById(R.id.pullToRefresh);
        pull_to_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flexlayout.removeAllViews();
                image_counter = 0;
                updatePreviewOfImages();
                pull_to_refresh.setRefreshing(false);
            }
        });

        // show kram from directory
        directory_path = Environment.getExternalStorageDirectory().toString() + "/" + Environment.DIRECTORY_PICTURES + "/" + getResources().getString(R.string.app_name);
        updatePreviewOfImages();
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
        if (imageFile.length() != 0) {
            Intent iGoToFullScreen = new Intent(this, FullscreenActivity.class);
            iGoToFullScreen.putExtra("picture", imageFile);
            startActivity(iGoToFullScreen);
        }
        else {
            imageFile.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        updatePreviewOfImages();
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

    private void updatePreviewOfImages() {
        File directory = new File(directory_path);
        File[] files = directory.listFiles();

        if (number_of_images > files.length) {
            flexlayout.removeAllViews();
            image_counter = 0;
        }
        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            } });

        for (image_counter = image_counter; image_counter < files.length; image_counter++) {
            final String image_path = files[image_counter].getAbsolutePath();
            final ImageView imageView = new ImageView(getApplicationContext());
            Uri localUri = Uri.fromFile(files[image_counter].getAbsoluteFile());
            imageView.setImageURI(localUri);
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(270, 310));
            imageView.setMaxHeight(320);
            imageView.setMaxWidth(280);
            imageView.setPadding(0,15,0,0);
            imageView.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 File imageFile = new File(image_path);
                                                 Intent intent = new Intent();
                                                 intent.setClass(getApplicationContext(), FullscreenActivity.class);
                                                 intent.putExtra("picture", imageFile);
                                                 startActivity(intent);
                                             }
                                         }
            );
            flexlayout.addView(imageView);
        }
        number_of_images = files.length;
    }

}
