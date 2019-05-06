package com.example.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements View.OnKeyListener {
    private static final int FILECHOOSER_RESULTCODE = 123;
    public EditText myEdit;
    public WebView myWebView;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> uploadMessage;

    private static final int REQUEST_SELECT_FILE = 124;

    //变量


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListen();
    }

    private void initView() {
        myEdit = (EditText) findViewById(R.id.editText);
        myWebView = (WebView) findViewById(R.id.myWebView);

    }

    private void setListen() {
        myEdit.setOnKeyListener(this);
        myWebView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        myWebView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        myWebView.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        myWebView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        myWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setJavaScriptEnabled(true); // must
        myWebView.getSettings().setDomStorageEnabled(true); //must
        myWebView.getSettings().setDisplayZoomControls(false);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("https://mclient.alipay.com")){
                    view.setVisibility(View.INVISIBLE);
                    String jsFunction="javascript:function startBodyHide() {"+
                            "document.body.setAttribute('style','display:none')";
                    view.loadUrl(jsFunction);
                    view.loadUrl("javascript:startBodyHide();");
                }
                view.setVisibility(View.VISIBLE);
                //网页唤醒微信支付
                if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;


                }
                //网页唤醒支付宝支付
                if (url.startsWith("alipays:") || url.startsWith("alipay")) {
                    try {
                        MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    } catch (Exception e) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("未检测到支付宝客户端，请安装后重试。")
                                .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                        MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                    }
                                }).setNegativeButton("取消", null).show();
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            // https支持
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.startsWith("https://mclient.alipay.com")){
                    view.setVisibility(View.INVISIBLE);
                    String jsFunction="javascript:function startBodyHide() {"+
                            "document.body.setAttribute('style','display:none')";
                    view.loadUrl(jsFunction);
                    view.loadUrl("javascript:startBodyHide();");
                }
                else{                view.setVisibility(View.VISIBLE);
                }
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if(url.startsWith("https://mclient.alipay.com")){
                    view.setVisibility(View.INVISIBLE);
                    String jsFunction="javascript:function startHide() {"+
                            "document.getElementsByClassName('result')[0].style.cssText='display:none';}";
                    view.loadUrl(jsFunction);
                    view.loadUrl("javascript:startHide();");
                }else {
                    view.setVisibility(View.VISIBLE);

                }

                super.onPageFinished(view, url);
            }

        });
        myWebView.setWebChromeClient(new WebChromeClient(){
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);

            }
            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType ) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }
            //For Android >4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult( Intent.createChooser( i, "File Chooser" ),FILECHOOSER_RESULTCODE );

            }

            // For Lollipop 5.0+ Devices
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
                    return false;
                }
                return true;
            }
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("webView",consoleMessage.message() );
                    return super.onConsoleMessage(consoleMessage);
            }
        });
    }
   @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出H5界面
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER) {
            // 监听到回车键，会执行2次该方法。按下与松开
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                showToast(myEdit.getText().toString());
                myWebView.loadUrl(myEdit.getText().toString());
            }
        }
        return false;
    }

    public void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // do take photo detail
        super.onActivityResult(requestCode, resultCode, data);
    }
}
