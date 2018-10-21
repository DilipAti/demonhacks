package dilipati.snaptagger.activities;

import java.util.Random;
import java.math.*;

public class Tags {

    private String[] tagArr;
    public Tags(){
       String tags = "#love, #instagood, #photooftheday, #millenial_stuff, #hireus, #cute, #tbt, #like4like, #followme, #picoftheday, #follow, #art, #instadaily, #repost, #instalike, #iswearimhuman, #igers, #tagsforlikes, #follow4follow, #nofilter, #thanosdidnothingwrong, #amazing, #instamood, #instagramisnotthis, #photography, #humanmade, #imsocool, #bestoftheday, #demonhacks, #swag, #motivation, #lol, #instapic, #funny, #needsleep, #yummy, #lifestyle, #hot, #instafood, #no_life, #inspiration, #instacool, #goodmorning, #iphoneonly";
       this.tagArr = tags.split(", ");
    }

    public String[] getTags(){
        return this.tagArr;
    }

    public String[] getTags(int limit){
        int index = 0;
        String[] partial = new String[limit];
        for(int i = 0;  i < limit; i++){
            partial[i] = this.tagArr[(int)(Math.random() * this.tagArr.length)];
        }
        return partial;
    }
}
