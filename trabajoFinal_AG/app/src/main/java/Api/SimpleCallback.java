package Api;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONObject;

import java.io.IOException;

public class SimpleCallback implements Callback {

    private final ApiCallback callback;

    public SimpleCallback(ApiCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        callback.onFailure(e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        ResponseBody responseBody = response.body();
        String bodyString = responseBody != null ? responseBody.string() : null;

        try {
            if (bodyString != null) {
                JSONObject json = new JSONObject(bodyString);
                callback.onSuccess(json); // Pasa el JSON a tu clase de actividad
            } else {
                callback.onFailure(new Exception("Respuesta vacía del servidor."));
            }
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
}
