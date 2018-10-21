package labeldetection;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import android.content.Context;
import android.os.AsyncTask;


import java.util.ArrayList;
import java.util.List;

public class LabelDetectorTask extends AsyncTask<Image, Void, List<AnnotateImageResponse> >{
    Context context = null;
    GoogleCredentials myCreds = null;
    ImageAnnotatorSettings imageAnnotatorSettings = null;

    public LabelDetectorTask(Context context) {
        this.context = context;

        try{

            this.myCreds = GoogleCredentials.fromStream( context.getAssets().open("Snaptagger-4f7d5c4284d9.json"));

            // Instantiates a client
            imageAnnotatorSettings =
                    ImageAnnotatorSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(this.myCreds))
                            .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<AnnotateImageResponse> doInBackground(Image... images) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(this.imageAnnotatorSettings)){
            Image image = images[0];
            List<AnnotateImageRequest> requests = new ArrayList<>();
            Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(image)
                    .build();
            requests.add(request);

            // Performs label detection on the image file
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.printf("Error: %s\n", res.getError().getMessage());
                }
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    annotation.getAllFields().forEach((k, v) ->
                            System.out.printf("%s : %s\n", k, v.toString()));

                }
            }
            return responses;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
