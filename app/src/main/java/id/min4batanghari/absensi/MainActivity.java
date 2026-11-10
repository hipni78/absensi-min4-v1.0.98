package id.min4batanghari.absensi;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private Uri cameraImageUri;
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private static final int CAMERA_PERMISSION_REQUEST = 1002;
    private boolean pendingImageChooser = false;
    private boolean pendingDirectCamera = false;
    private String requestedPickerMode = "";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidCameraBridge(), "AndroidCameraBridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                if (!acceptsImages(params)) {
                    pendingImageChooser = false;
                    showDocumentPicker(params);
                    return true;
                }
                pendingImageChooser = true;
                pendingDirectCamera = "camera".equals(requestedPickerMode) || (params != null && params.isCaptureEnabled());
                boolean directGallery = "gallery".equals(requestedPickerMode);
                requestedPickerMode = "";
                if (directGallery) {
                    showGalleryOnly();
                } else if (pendingDirectCamera) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                    } else {
                        showCameraOnly();
                    }
                } else if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                } else {
                    showImagePicker();
                }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class AndroidCameraBridge {
        @JavascriptInterface
        public void setPickerMode(String mode) {
            if ("camera".equals(mode) || "gallery".equals(mode)) requestedPickerMode = mode;
            else requestedPickerMode = "";
        }
    }

    private boolean acceptsImages(FileChooserParams params) {
        if (params == null || params.getAcceptTypes() == null) return false;
        for (String type : params.getAcceptTypes()) {
            if (type == null) continue;
            String t = type.toLowerCase();
            if (t.contains("image/*") || t.startsWith("image/")) return true;
        }
        return false;
    }

    private void showDocumentPicker(FileChooserParams params) {
        try {
            Intent intent = params.createIntent();
            if (intent.getType() == null) intent.setType("*/*");
            startActivityForResult(intent, FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            finishFileChooser(null);
            Toast.makeText(this, "Tidak dapat membuka pemilih berkas: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showGalleryOnly() {
        try {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
            gallery.addCategory(Intent.CATEGORY_OPENABLE);
            gallery.setType("image/*");
            startActivityForResult(Intent.createChooser(gallery, "Pilih foto dari galeri"), FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            finishFileChooser(null);
            Toast.makeText(this, "Tidak dapat membuka galeri: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showCameraOnly() {
        try {
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (camera.resolveActivity(getPackageManager()) == null) {
                finishFileChooser(null);
                Toast.makeText(this, "Kamera tidak tersedia pada perangkat ini", Toast.LENGTH_LONG).show();
                return;
            }
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "MIN4_KAMERA_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (cameraImageUri != null) {
                camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                camera.setClipData(ClipData.newRawUri("Foto MIN4", cameraImageUri));
            }
            startActivityForResult(camera, FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            finishFileChooser(null);
            Toast.makeText(this, "Tidak dapat membuka kamera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showImagePicker() {
        try {
            Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
            gallery.addCategory(Intent.CATEGORY_OPENABLE);
            gallery.setType("image/*");
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraImageUri = null;
            if (camera.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "MIN4_" + System.currentTimeMillis() + ".jpg");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (cameraImageUri != null) {
                    camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    camera.setClipData(ClipData.newRawUri("Foto MIN4", cameraImageUri));
                }
            }
            Intent chooser = Intent.createChooser(gallery, "Pilih foto atau ambil dari kamera");
            if (cameraImageUri != null) chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{camera});
            startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            finishFileChooser(null);
            Toast.makeText(this, "Tidak dapat membuka kamera/galeri: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void finishFileChooser(Uri[] uris) {
        if (uploadCallback != null) uploadCallback.onReceiveValue(uris);
        uploadCallback = null;
        cameraImageUri = null;
        pendingImageChooser = false;
        pendingDirectCamera = false;
        requestedPickerMode = "";
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && pendingImageChooser) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingDirectCamera) showCameraOnly(); else showImagePicker();
            } else {
                finishFileChooser(null);
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && uploadCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) results = new Uri[]{data.getData()};
                else if (pendingImageChooser && cameraImageUri != null) results = new Uri[]{cameraImageUri};
            }
            finishFileChooser(results);
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
