package in.arjsna.audiorecorder.api;

/**
 * Created by rodolfo on 25/11/17.
 */

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("")
    Call<String> test();
}