package dilipati.snaptagger.activities;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.twitter.sdk.android.core.BuildConfig;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import dilipati.snaptagger.R;
import labeldetection.LabelDetectorTask;

public class MainActivity extends AppCompatActivity {

    private File storageDir;
    private Context context;
    private String mCurrentPhotoPath, base64String;
    private String tagString;
    Uri photoURI = null;

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

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.CONSUMER_KEY), getString(R.string.CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    private void dispatchTakePictureIntent() {
        if(!runtimePermissionsRequest()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                // Uri photoURI = null;
                try {
                    this.photoURI = FileProvider.getUriForFile(context,
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
                    //base64String = setImageAndReturnBase64String(image);
                    byte[] imgByteArr = setImageAndReturnBase64String(image);
                    ByteString imgByteStr = ByteString.copyFrom(imgByteArr);
                    Image gcpImage = Image.newBuilder().setContent(imgByteStr).build();
                    LabelDetectorTask labelTask =  new LabelDetectorTask(this.context);

                    System.out.println("MAKING REQUEST TO VISION API");
                    labelTask.execute(gcpImage);

                    List<AnnotateImageResponse> responses = labelTask.get();
                    this.getTags(responses);
                    System.out.println("VISION API HAS RETURNED A RESULT");

                    this.displayTags();


                }
            }
        } catch (NullPointerException | OutOfMemoryError e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void getTags(List<AnnotateImageResponse> list){
        RelevantTags convert = new RelevantTags(list);
        TagInjector injector = new TagInjector(convert.getTags());
        this.tagString = injector.getTags();

    }

    private void displayTags() {
        Button clickPictureButton = findViewById(R.id.clickPictureButton);
        clickPictureButton.setVisibility(View.GONE);

        EditText tagView = findViewById(R.id.editTagText);
        tagView.setText(this.tagString);
        tagView.setVisibility(View.VISIBLE);

        Button twitterLoginButton = findViewById(R.id.sendTweet);
        twitterLoginButton.setVisibility(View.VISIBLE);
        twitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTweetIntent();
                Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendTweetIntent() {
        /*
        Uri imageUri = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".file_provider",
                new File("/path/to/image"));
                */

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text(this.tagString)
                .image(this.photoURI);
        builder.show();
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

    private byte[] setImageAndReturnBase64String(ImageView imageView) throws NullPointerException, OutOfMemoryError {

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

        //return Base64.encodeToString(byteArray, Base64.DEFAULT);
        return byteArray;
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
