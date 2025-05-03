package Api;

import org.json.JSONObject;

public interface ApiCallback {
    void onSuccess(JSONObject response);
    void onFailure(Exception e);
}
