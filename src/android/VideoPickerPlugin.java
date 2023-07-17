package cordova.plugin.videopicker;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

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
                String videoPath = getVideoPathFromUri(videoUri);
                callbackContext.success(videoPath);
            } else if (resultCode == cordova.getActivity().RESULT_CANCELED) {
                callbackContext.error("User canceled");
            } else {
                callbackContext.error("Failed to pick video");
            }
        }
    }

    private String getVideoPathFromUri(Uri uri) {
        Context context = this.cordova.getActivity().getApplicationContext();
        ContentResolver contentResolver = context.getContentResolver();
        String videoPath = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                InputStream inputStream = contentResolver.openInputStream(uri);
                File cacheDir = context.getCacheDir();
                File tempFile = File.createTempFile("temp_", ".mp4", cacheDir);
                FileOutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                videoPath = tempFile.getAbsolutePath();
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String[] projection = {MediaStore.Video.Media.DATA};
            Cursor cursor = contentResolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                videoPath = cursor.getString(columnIndex);
                cursor.close();
            }
        }

        return videoPath;
    }
}

