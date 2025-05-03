package Api;

import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

import com.example.trabajofinal_ag.DownloadActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class ApiUtils {

    private static final String BASE_URL = "http://192.168.1.14:50010/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // -----------------------------
    // REGISTRO DE USUARIO
    // -----------------------------
    public static void registerUser(String nombre,String surname, String email, String contrasena,String username, ApiCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("nombre", nombre);
            json.put("surname", surname);
            json.put("email", email);
            json.put("contrasena", contrasena);
            json.put("username", username);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "registro")
                .post(body)
                .build();

        client.newCall(request).enqueue(new SimpleCallback(callback));
    }

    // -----------------------------
    // INICIAR SESIÓN
    // -----------------------------
    public static void loginUser(String email, String contrasena, String username, ApiCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("contrasena", contrasena);
            json.put("username", username);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new SimpleCallback(callback));
    }

    // -----------------------------
    // REGISTRAR DESCARGA
    // -----------------------------
    public static void registerDownload(String username, String url, String filename,String format, ApiCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            json.put("url", url);
            json.put("filename", filename);
            json.put("formato", format);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "register_download")
                .post(body)
                .build();

        client.newCall(request).enqueue(new SimpleCallback(callback));
    }


}
