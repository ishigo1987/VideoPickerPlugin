package cordova-plugin-videopicker;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

public class VideoPickerPlugin extends CordovaPlugin {
    private static final int REQUEST_CODE = 1;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("pickVideo")) {
            if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionHelper.requestPermission(this, REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openVideoPickerPlugin();
            }
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (requestCode == REQUEST_CODE) {
            if (PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openVideoPickerPlugin();
            } else {
                callbackContext.error("Permission denied");
            }
        }
    }

    private void openVideoPickerPlugin() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        cordova.startActivityForResult(this, intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
                Uri videoUri = data.getData();
                String videoPath = videoUri.getPath();
                callbackContext.success(videoPath);
            } else if (resultCode == cordova.getActivity().RESULT_CANCELED) {
                callbackContext.error("User canceled");
            } else {
                callbackContext.error("Failed to pick video");
            }
        }
    }
}

