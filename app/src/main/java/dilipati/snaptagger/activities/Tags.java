package dilipati.snaptagger.activities;

import java.util.Random;
import java.math.*;

public class Tags {

    private String[] tagArr;
    public Tags(){
       String tags = "#love, #instagood, #photooftheday, #beautiful, #happy, #cute, #tbt, #like4like, #followme, #picoftheday, #follow, #art, #instadaily, #repost, #instalike, #likeforlike, #igers, #tagsforlikes, #follow4follow, #nofilter, #life, #amazing, #instamood, #instagram, #photography, #photo, #followforfollow, #bestoftheday, #vsco, #l4l, #f4f, #swag, #motivation, #cool, #lol, #instapic, #funny, #tflers, #yummy, #lifestyle, #hot, #instafood, #handmade, #inspiration, #instacool, #goodmorning, #iphoneonly";
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
