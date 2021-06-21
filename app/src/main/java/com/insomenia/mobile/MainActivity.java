package com.insomenia.mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.tnkfactory.ad.TemplateLayoutUtils;
import com.tnkfactory.ad.TnkSession;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    WebView webview;
    // 오퍼월 시작
    WebView newWebView;
    // 오퍼월 끝
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;

    Activity activity;
    String currentUrl;

    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;
    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private Uri cameraImageUri = null;
    private Toast toast;
    private long backKeyPressedTime = 0;
    private String token = "";


    public void setUserId(String userId) {
        String token = BaseUtil.getStringPref(getApplicationContext(), "token", "");
        Log.d("DEVICE TOKEN", userId + " " + token);
        String sessionId = BaseUtil.getStringPref(getApplicationContext(), "sessionId", "");
        if (token != "") {
            new BaseUtil.GetUrlContentTask().execute(getString(R.string.domain) + "/users/" + userId + "/token?token=" + token + "&device_type=android" + "&session_id=" + sessionId);
        }
    }

    public void setSessionId(String sessionId) {
        BaseUtil.setStringPref(getApplicationContext(), "sessionId", sessionId);
    }

    // 오퍼월 시작
    // https://github.com/tnkfactory/android-sdk-rwd/blob/master/Android_Guide.md 문서 참조
    public void openAdList() {
        TnkSession.setUserName(MainActivity.this, BaseUtil.getStringPref(getApplicationContext(), "userId", null));
        TnkSession.showAdList(MainActivity.this,"Your title here", TemplateLayoutUtils.getBlueStyle_01());
    }
    //오퍼월 끝

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        webview = findViewById(R.id.webview);
        final WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(webSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            cookieManager.setAcceptThirdPartyCookies(webview, true);
        }

        if (!BaseUtil.getBoolPref(this, "pushChecked", false)) {
            BaseUtil.makeAlert(this, "마케팅 수신 동의", "앱 전용 혜택, 실시간 할인 정보 등의 유용한 정보를 받아보시겠습니까", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BaseUtil.setBoolPref(MainActivity.this, "push", true);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BaseUtil.setBoolPref(MainActivity.this, "push", false);
                }
            }, R.string.yes, R.string.no);
            BaseUtil.setBoolPref(this, "pushChecked", true);
        }

        webview.addJavascriptInterface(new WebAppInterface(MainActivity.this), "Android");
        webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebChromeClient webClient = new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("알림 메시지")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();

                return true;
            }

            // For Android 5.0+
//            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            public boolean onShowFileChooser(
//                    WebView webView, ValueCallback<Uri[]> filePathCallback,
//                    FileChooserParams fileChooserParams) {
//                Log.d("MainActivity", "5.0+");
//
//                // Callback 초기화 (중요!)
//                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
//                    filePathCallbackLollipop = null;
//                }
//                filePathCallbackLollipop = filePathCallback;
//
//                boolean isCapture = fileChooserParams.isCaptureEnabled();
//                runCamera(isCapture);
//                return true;
//            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i,"File Chooser"), FCR);
            }
            // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
            public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FCR);
            }
            //For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FCR);
            }
            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams){
                if(mUMA != null){
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    }catch(IOException ex){
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if(photoFile != null){
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    }else{
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                if(takePictureIntent != null){
                    intentArray = new Intent[]{takePictureIntent};
                }else{
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }

            // 오퍼월 시작
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                Log.d("Test", "11111");
                // Dialog Create Code
                MainActivity mActivity = MainActivity.this;
                newWebView = new WebView(mActivity);
                WebSettings webSettings = newWebView.getSettings();

                webSettings.setJavaScriptEnabled(true);
                webSettings.setAllowFileAccess(true);
                webSettings.setAllowFileAccessFromFileURLs(true);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
                webSettings.setBuiltInZoomControls(false);
                webSettings.setSupportZoom(false);
                webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
                webSettings.setUseWideViewPort(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setAllowContentAccess(true);
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSettings.setDomStorageEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                webSettings.setSupportMultipleWindows(true);
                webSettings.setUseWideViewPort(true);

                final Dialog dialog = new Dialog(mActivity);
                dialog.setContentView(newWebView);

                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            webview.removeView(newWebView);
                            newWebView.clearHistory();
                            newWebView.destroy();
                            dialog.dismiss();
                        }
                        return true;
                    }
                });


                ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
                dialog.show();


                newWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onCloseWindow(WebView window) {
                        super.onCloseWindow(window);
                        webview.removeView(newWebView);
                        newWebView.clearHistory();
                        newWebView.destroy();
                        dialog.dismiss();
                    }
                });

                // WebView Popup에서 내용이 안보이고 빈 화면만 보여 아래 코드 추가
                newWebView.setWebViewClient(new NiceWebViewClient());

                ((WebView.WebViewTransport)resultMsg.obj).setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;

            }
            // 오퍼월 끝

            private File createImageFile() throws IOException{
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "img_"+timeStamp+"_";
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                return File.createTempFile(imageFileName,".jpg",storageDir);
            }
        };




        webview.setWebChromeClient(webClient);
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

//                if (url.startsWith("intent://")){
//                    Intent intent = null;
//                    try{
//                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//                        if(intent != null){
//                            startActivity(intent);
//                        }
//                    } catch(URISyntaxException e){
//                        e.printStackTrace();
//                    } catch(ActivityNotFoundException e){
//                        String packageName = intent.getPackage();
//                        if(!packageName.equals("")){
//                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
//                        }
//                    }
//                    return true;
//                }
//                else if(url.startsWith("https://play.google.com/store/apps/details?id=") || url.startsWith("market://details?id=")){
//                    Uri uri = Uri.parse(url);
//                    String packageName = uri.getQueryParameter("id");
//                    if(packageName != null && !packageName.equals("")){
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
//                    }
//                    return true;
//                }
//                else
                if (url.contains("intent")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if(existPackage != null){
                            startActivity(intent);
                        } else {
                            Intent introIntent = new Intent(Intent.ACTION_VIEW);
                            introIntent.setData(Uri.parse(intent.getPackage()));
                            startActivity(introIntent);
                        }
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if(fallbackUrl != null){
                            view.loadUrl(fallbackUrl);
                            return true;
                        }
                        return true;
                    } catch (URISyntaxException ex) {
                        return false;
                    }  catch (Exception e){
                        e.printStackTrace();
                        return false;
                    }
                }
                else if (!Uri.parse(url).getScheme().equals("http") && !Uri.parse(url).getScheme().equals("https") && !url.startsWith("javascript:")) {
                    Intent newIntent = null;
                    try {
                        newIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                        Uri newUri = Uri.parse(newIntent.getDataString());

                        activity.startActivity(new Intent(Intent.ACTION_VIEW, newUri));
                        return true;
                    } catch (URISyntaxException ex) {
                        return false;
                    } catch (ActivityNotFoundException e) {
                        if (newIntent == null) return false;

                        if (handleNotFoundPaymentScheme(newIntent.getScheme())) return true;

                        String packageName = newIntent.getPackage();
                        if (packageName != null) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }
                        return false;
                    }
                }
                else if (url.startsWith("tel:")) {
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(tel);
                    return true;
                }
                else if (Uri.parse(url).getHost().equals("m.facebook.com") || Uri.parse(url).getHost().equals("www.facebook.com") || Uri.parse(url).getHost().contains("kakao.com") ) {
                    currentUrl = url;
                    webview.loadUrl(currentUrl);
                    return false;
                }
                else {
                    if (url.contains("#!/")) {
                        currentUrl = null;
                    } else {
                        currentUrl = url;
                    }
                    webview.loadUrl(currentUrl);
                }
                return false;
            }
        });

        String url = checkMessage(getIntent());
        if(url == null) {
            webview.loadUrl(getResources().getString(R.string.url));
        }else{
            webview.loadUrl(url);
        }
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>(){
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if(!task.isSuccessful()){
                        Log.i("FCM Log","getInstanceId failed", task.getException());
                        return;
                    }
                    token = task.getResult().getToken();
                    BaseUtil.setStringPref(getApplicationContext(), "token", token);
                }
            });
        checkVerify();
    }

    private void runCamera(boolean _isCapture)
    {
//        if (!_isCapture)
//        {// 갤러리 띄운다.
//            Intent pickIntent = new Intent(Intent.ACTION_PICK);
//            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//
//            String pickTitle = "사진 가져올 방법을 선택하세요.";
//            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
//
//            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
//            return;
//        }

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        File path = getFilesDir();
        File file = new File(path, "image.png");
        // File 객체의 URI 를 얻는다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        } else {
            cameraImageUri = Uri.fromFile(file);
        }
        Log.d("RUN CAMERA", cameraImageUri.toString());
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

//        if (!_isCapture)
//        { // 선택팝업 카메라, 갤러리 둘다 띄우고 싶을 때..
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            pickIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            String pickTitle = "사진 가져올 방법을 선택하세요.";
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);

            // 카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
            startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
//        }
//        else
//        {// 바로 카메라 실행..
//            startActivityForResult(intentCamera, FILECHOOSER_LOLLIPOP_REQ_CODE);
//        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {

        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            }

            requestPermissions(new String[]{Manifest.permission.INTERNET,Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i=0; i<grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();

                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();
                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
//        switch (requestCode)
//        {
//            case FILECHOOSER_NORMAL_REQ_CODE:
//                if (resultCode == RESULT_OK)
//                {
//                    Log.d("ACTIVITY RESULT", "파일 첨부");
//                    if (filePathCallbackNormal == null) return;
//                    Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
//                    filePathCallbackNormal.onReceiveValue(result);
//                    filePathCallbackNormal = null;
//                }
//                break;
//            case FILECHOOSER_LOLLIPOP_REQ_CODE:
//                if (resultCode == RESULT_OK)
//                {
//                    if (filePathCallbackLollipop == null) return;
//                    if (data == null)
//                        data = new Intent();
//                    if (data.getData() == null)
//                        data.setData(cameraImageUri);
//                    Log.d("ACTIVITY RESULT", "롤리팝 파일 첨부" + cameraImageUri.toString());
//
//                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
//                    filePathCallbackLollipop = null;
//                }
//                else
//                {
//                    if (filePathCallbackLollipop != null)
//                    {
//                        filePathCallbackLollipop.onReceiveValue(null);
//                        filePathCallbackLollipop = null;
//                    }
//
//                    if (filePathCallbackNormal != null)
//                    {
//                        filePathCallbackNormal.onReceiveValue(null);
//                        filePathCallbackNormal = null;
//                    }
//                }
//                break;
//            default:
//
//                break;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onBackPressed() {
        if (webview.getUrl().equals("")) {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                finish();
                toast.cancel();
            }
        } else {
            if (webview.canGoBack()) webview.goBack();
            else super.onBackPressed();
        }
    }


    private String checkMessage(Intent intent) {
        String url = null;
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                url = bundle.getString("url");
            }
        }
        return url;
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected boolean handleNotFoundPaymentScheme(String scheme) {
        if (PaymentScheme.ISP.equalsIgnoreCase(scheme)) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_ISP)));
            return true;
        } else if (PaymentScheme.BANKPAY.equalsIgnoreCase(scheme)) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PaymentScheme.PACKAGE_BANKPAY)));
            return true;
        }

        return false;
    }


    public class NiceWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.startsWith("intent://")) {
                Intent intent = null;
                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        //앱실행
                        startActivity(intent);
                    }
                } catch (URISyntaxException e) {
                    //URI 문법 오류 시 처리 구간

                } catch (ActivityNotFoundException e) {
                    String packageName = intent.getPackage();
                    if (!packageName.equals("")) {
                        return false;
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    }
                }
                //return  값을 반드시 true로 해야 합니다.
                return true;

            } else if (url.startsWith("https://play.google.com/store/apps/details?id=") || url.startsWith("market://details?id=")) {
                //표준창 내 앱설치하기 버튼 클릭 시 PlayStore 앱으로 연결하기 위한 로직
                Uri uri = Uri.parse(url);
                String packageName = uri.getQueryParameter("id");
                if (packageName != null && !packageName.equals("")) {
                    // 구글마켓 이동
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                }
                //return  값을 반드시 true로 해야 합니다.
                return true;
            }else{
                if (url.contains("#!/")) {
                    currentUrl = null;
                } else {
                    currentUrl = url;
                }

            }

            //return  값을 반드시 false로 해야 합니다.
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(webview, (Object[]) null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onResume", (Class[]) null)
                    .invoke(webview, (Object[]) null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



}
