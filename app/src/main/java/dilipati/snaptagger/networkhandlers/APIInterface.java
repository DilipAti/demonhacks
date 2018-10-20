package dilipati.snaptagger.networkhandlers;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIInterface {


    // The get apis would be this way
    @GET("")
    Call<String> getData(@Header("headerToken") String headerToken);

    // Posting some data
    @POST("")
    Call<String> postData(@Header("headerToken") String headerToken, @Body String data, @Path("id") String id, @Path("token") String token);

}