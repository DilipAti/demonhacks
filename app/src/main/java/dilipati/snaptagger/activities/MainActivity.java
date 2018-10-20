package dilipati.snaptagger.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dilipati.snaptagger.R;

public class MainActivity extends AppCompatActivity {

    private File storageDir;
    private Context context;
    private String mCurrentPhotoPath, base64String;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!(storageDir != null && storageDir.exists())) {
            if (storageDir != null) {
                storageDir.mkdir();
            }
        }
        context = getApplicationContext();
        Button clickPictureButton = findViewById(R.id.clickPictureButton);
        clickPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        if(!runtimePermissionsRequest()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                Uri photoURI = null;
                try {
                    photoURI = FileProvider.getUriForFile(context,
                            "dilipati.snaptagger.fileprovider", createImageFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode != RESULT_CANCELED) {
                if (requestCode == 0 && resultCode == RESULT_OK) {
                    ImageView image = findViewById(R.id.image);
                    base64String = setImageAndReturnBase64String(image);
                    Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException | OutOfMemoryError e) {
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private String setImageAndReturnBase64String(ImageView imageView) throws NullPointerException, OutOfMemoryError {

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        if (bitmap == null) {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, options);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
        } else {
            bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
        }
        imageView.setImageBitmap(bitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private boolean runtimePermissionsRequest() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] != PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                runtimePermissionsRequest();
            }
            runtimePermissionsRequest();
        }
    }
}
