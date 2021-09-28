package com.kepeng.youthfest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_SITE_URL = "https://dyouthfest.kepeng.io/",
            STRING_TO_MATCH_FOR_BARCODE_SCAN = "https://dyouthfest.kepeng.io/scan",
            POST_URL = "https://dyouthfest.kepeng.io/Dompet/buy/";

    private WebView main_web_view;
    private static final int FILE_SELECT_CODE = 0;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @SuppressLint("JavascriptInterface")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();
        if (uri != null) {
            String path = uri.toString();
            Toast.makeText(MainActivity.this, "Path=" + path, Toast.LENGTH_LONG).show();
        }

        main_web_view = (WebView) findViewById(R.id.webview);
        main_web_view.setWebViewClient(new WebViewClient());

        WebSettings mwebSettings = main_web_view.getSettings();
        mwebSettings.setJavaScriptEnabled(true);
        mwebSettings.setSupportZoom(false);
        mwebSettings.setAllowFileAccess(true);
        mwebSettings.setAllowFileAccess(true);
        mwebSettings.setAllowContentAccess(true);

        if (18 < Build.VERSION.SDK_INT) { //18=JellyBean MR2, KITKAT=19 webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); } if (Build.VERSION.SDK_INT>= 19) {
            main_web_view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        main_web_view.setWebChromeClient(new WebChromeClient() {
                                             //The undocumented magic method override
                                             //Eclipse will swear at you if you try to put @Override here

                                             protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                                                 mUploadMessage = uploadMsg;
                                                 Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                                 i.addCategory(Intent.CATEGORY_OPENABLE);
                                                 i.setType("image/*");
                                                 startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
                                             }

                                             @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                             public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                                                 if (uploadMessage != null) {
                                                     uploadMessage.onReceiveValue(null);
                                                     uploadMessage = null;
                                                 }

                                                 uploadMessage = filePathCallback;

                                                 Intent intent = fileChooserParams.createIntent();
                                                 try {
                                                     startActivityForResult(intent, REQUEST_SELECT_FILE);
                                                 } catch (ActivityNotFoundException e) {
                                                     uploadMessage = null;
                                                     Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                                                     return false;
                                                 }
                                                 return true;
                                             }

                                             //For Android 4.1 only
                                             protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                                                 mUploadMessage = uploadMsg;
                                                 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                                 intent.addCategory(Intent.CATEGORY_OPENABLE);
                                                 intent.setType("image/*");
                                                 startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
                                             }

                                             protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                                 mUploadMessage = uploadMsg;
                                                 Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                                 i.addCategory(Intent.CATEGORY_OPENABLE);
                                                 i.setType("image/*");
                                                 startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                                             }
                                         }
        );        // load site URL
        openUrl(MAIN_SITE_URL);
    } // onCreate();

    public void openUrl(String url) {
        main_web_view = (WebView) findViewById(R.id.webview);
        //enable javascript
        main_web_view.getSettings().setJavaScriptEnabled(true);

        // get the activity context
        final Activity activity = this;

        //set client to handle errors and intercept link clicks
        main_web_view.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                String msg = "error : " + description + " Request URL : " + failingUrl;
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // we will interrupt the link here
                // we will interrupt the link here
                if (isURLMatching(url)) {
                    scanNow();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);

            }

        });

        //load the URL
        main_web_view.loadUrl(url);
    }

    private boolean isURLMatching(String url) {
        return url.toLowerCase().contains(STRING_TO_MATCH_FOR_BARCODE_SCAN.toLowerCase());
    }

    private void scanNow() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan Barcode or QR code");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            view.loadData("Maaf Internet Anda tidak stabil", "text/html", "utf-8");
            super.onReceivedError(view, request, error);

        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK) && main_web_view.canGoBack()) {
            main_web_view.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //we have a result
            String codeContent = scanningResult.getContents();
            String codeFormat = scanningResult.getFormatName();

            //load the URL and Pass the scanned barcode
            openUrl(POST_URL+"?id="+codeContent);
//            Toast toast = Toast.makeText(getApplicationContext(),codeContent, Toast.LENGTH_SHORT);
//            toast.show();

        }else{
//            Toast toast = Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT);
//            toast.show();
        }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(MainActivity.this.getApplicationContext(), "Failed to Upload Image", Toast.LENGTH_LONG).show();
     }

        //retrieve scan result

}