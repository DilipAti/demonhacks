package dilipati.snaptagger.activities;

import com.google.cloud.*;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;

import java.util.List;
import java.util.ArrayList;

public class RelevantTags {

    private ArrayList<EntityAnnotation> annotations;

    public RelevantTags(List<AnnotateImageResponse> originList){
        this.annotations = new ArrayList<EntityAnnotation>();
        for(AnnotateImageResponse resList : originList){
            List<EntityAnnotation> l = resList.getLabelAnnotationsList();
            for(EntityAnnotation entity : l){
                if(entity.getScore()>= 0.4){
                    this.annotations.add(entity);
                }
            }
        }
    }

    public String[] getTags(){
        String[] sAnnotations = new String[this.annotations.size()];
        for(int i =0; i < this.annotations.size(); i++){
            sAnnotations[i] = "#"+this.annotations.get(i).getDescription();
            sAnnotations[i] = sAnnotations[i].replaceAll(" ", "_");

        }
        return sAnnotations;
    }
}
