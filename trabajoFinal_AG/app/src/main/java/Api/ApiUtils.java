package Api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

import com.example.trabajofinal_ag.DownloadActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import Models.Clip;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class ApiUtils {

    // ApiUtils.java
    private static String BASE_URL = "http://192.168.1.14:50010/"; // Valor por defecto

    public static void loadBaseUrlFromPreferences(SharedPreferences prefs) {
        BASE_URL = prefs.getString("base_url", BASE_URL);
    }

    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }


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

    // -----------------------------
    // Ver descargas
    // -----------------------------
    public static void fetchDownloadHistory(String username, ApiCallback callback) {
        String url = BASE_URL + "history/" + username;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Llamamos al callback en caso de error
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject jsonResponse = new JSONObject(body);
                        callback.onSuccess(jsonResponse); // Llamamos a onSuccess con la respuesta
                    } catch (JSONException e) {
                        callback.onFailure(e); // Si ocurre un error al parsear, llamamos a onFailure
                    }
                } else {
                    callback.onFailure(new Exception("Error en la respuesta de la API"));
                }
            }
        });
    }

    // -----------------------------
    // ELIMINAR HISTORIAL DE DESCARGAS
    // -----------------------------
    public static void deleteDownloadHistory(String username, ApiCallback callback) {
        String url = BASE_URL + "history/" + username + "/delete";

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Llamamos al callback en caso de error
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject jsonResponse = new JSONObject(body);
                        callback.onSuccess(jsonResponse); // Llamamos a onSuccess con la respuesta
                    } catch (JSONException e) {
                        callback.onFailure(e); // Si ocurre un error al parsear, llamamos a onFailure
                    }
                } else {
                    callback.onFailure(new Exception("Error en la respuesta de la API"));
                }
            }
        });
    }
    // -----------------------------
// SUBIR VIDEO Y EXTRAER CLIPS
// -----------------------------
    // Subir archivo sin extracción
    public static void uploadFile(File videoFile, ApiCallback callback) {
        MediaType mediaType = MediaType.parse("video/mp4");
        RequestBody fileBody = RequestBody.create(videoFile, mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", videoFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        callback.onSuccess(new JSONObject(body));
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new IOException("Error en la respuesta: " + response.code()));
                }
            }
        });
    }

    // Llamar al endpoint de extracción
    public static void extractClipsFromFile(String fileName, ApiCallback callback) {
        HttpUrl url = HttpUrl.parse(BASE_URL + "extract_clips")
                .newBuilder()
                .addQueryParameter("filename", fileName)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        callback.onSuccess(new JSONObject(body));
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new IOException("Error al extraer: " + response.code()));
                }
            }
        });
    }
    // -----------------------------
    // DESCARGAR CLIP
    // -----------------------------
    public static void downloadClip(Clip clip, ApiCallback callback) {
        // Aquí crea la request para backend con nombre archivo, start y end
        // Puedes usar OkHttp o Retrofit

        // Ejemplo con OkHttp:
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("fileName", clip.getDownloadUrl());
            json.put("start", clip.getStart());
            json.put("end", clip.getEnd());
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + "/download_clip")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Guarda el archivo mp4 en almacenamiento
                    InputStream is = response.body().byteStream();
                    // Guarda en disco con FileOutputStream...
                    // Lógica de guardado aquí

                    // Notifica éxito
                    try {
                        callback.onSuccess(new JSONObject().put("message", "Clip descargado correctamente"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    callback.onFailure(new Exception("Error en descarga: " + response.message()));
                }
            }
        });
    }



}
