package in.arjsna.audiorecorder.api;

/**
 * Created by rodolfo on 25/11/17.
 */

import retrofit2.Retrofit;

public class ApiClient {

    public static final String BASE_URL = "https://165.227.182.104:5000/";    // Nuestro server con nuestra API
    private static Retrofit retrofit = null;                                    // Objeto retrofit para las conexiones

    public static Retrofit getClient() {
        if (retrofit == null){                                                  // Nos aseguramos que no se haya creado un objeto retrofit
            retrofit = new Retrofit.Builder()                                   // Inicializamos el objeto Retrofit
                    .baseUrl(BASE_URL)                                      // Indicamos a cual sitio se estará conectando
                    .build();                                               // Construimos el objeto
        }
        return retrofit;
    }

}
