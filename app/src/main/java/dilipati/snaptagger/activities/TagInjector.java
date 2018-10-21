package dilipati.snaptagger.activities;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TagInjector {

    private int limit;
    private String[] mlTags;
    private String resultTags;

    public TagInjector(String[] mlTags){
        this.mlTags = mlTags;
        this.limit = 10 - this.mlTags.length;
        this.injectTags();
    }

    private void injectTags(){
        Tags tags = new Tags();
        String[] toInject = tags.getTags(this.limit);
        this.resultTags = "";
        for(int i = 0; i < 10; i++) {
            if (i < mlTags.length) {
                this.resultTags += (this.mlTags[i] + " ");
            } else {
                this.resultTags += (toInject[i - mlTags.length] + " ");
            }
        }
    }

    public String getTags(){

        return this.resultTags;
    }
}
