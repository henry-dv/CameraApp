package com.example.cameraapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
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

    public static final String EXTRA_PICTURE = "picture";

    private File imageFile;
    private int image_counter = 0;
    private com.google.android.flexbox.FlexboxLayout flexlayout;
    private String directory_path;
    private File appDir;
    private boolean appDirCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions if we don't have them
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        flexlayout = (FlexboxLayout) findViewById(R.id.flexbox1);

        // for tests purposes only because Android does not allow the app access to storage area outside of its own area
        // use fileprovider to get access to the storage (the right way)
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        directory_path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + getResources().getString(R.string.app_name);

        appDirCreated = createAppDirectory();
        if (!appDirCreated) {
            Log.e("AppDirectory", "Could not create App Directory. Aborting");
            System.exit(1);
        }

        // show kram from directory
        updatePreviewOfImages();

        // make button clickable
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    imageFile = createImageFile();
                    cameraIntent(imageFile);
                }
                catch (IOException ioe) {
                    Log.e("ioException", "Error creating Image File");
                }
            }
        });

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if nothing is written in the temp file, delete it
        if (imageFile.length() != 0) {
            Intent iGoToFullScreen = new Intent(this, FullscreenActivity.class);
            iGoToFullScreen.putExtra(EXTRA_PICTURE, imageFile);
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
            // Use FileProvider to give write access to the camera
            Uri outuri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", imageFile);
            iOpenCamera.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
            iOpenCamera.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(iOpenCamera, 2);
        }

    }

    // directory where images will be stored
    private boolean createAppDirectory() {
        // check permissions
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            appDir = new File(directory_path);
            // check if directory exists
            if (!appDir.exists())
                return appDir.mkdirs();
            else
                return true;
        }
        else {
            Log.e("Permissions", "No permissions to create Directories. Aborting...");
            Toast.makeText(getApplicationContext(), "No permissions to create Directories. Aborting...", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // create a temp file which will later be the image file
    private File createImageFile() throws IOException {
        if (!appDirCreated) {
            Log.e("image", "There is no directory where the image could be saved.");
            Toast.makeText(getApplicationContext(), "There is no directory where the image could be saved.", Toast.LENGTH_SHORT).show();
            return null;
        }

        return File.createTempFile("ms_image", ".png", appDir);
    }

    // get all files in the image directory and set for each element an ImageView
    private void updatePreviewOfImages() {
        File[] files = null;
        files = appDir.listFiles();
        if (image_counter > files.length) {
            flexlayout.removeAllViews();
            image_counter = 0;
        }

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        for (;image_counter < files.length; image_counter++) {
            final String image_path = files[image_counter].getAbsolutePath();
            // specific view to display an image
            final ImageView imageView = new ImageView(getApplicationContext());
            Uri localUri = Uri.fromFile(files[image_counter].getAbsoluteFile());
            imageView.setImageURI(localUri);
            // each imageView will have the same width and height
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(270, 310));
            imageView.setMaxHeight(320);
            imageView.setMaxWidth(280);
            imageView.setPadding(0, 15, 0, 0);
            // image is clickable
            imageView.setOnClickListener(
                    new View.OnClickListener() {
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
    }
}
