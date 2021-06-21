package com.insomenia.mobile;

import android.content.Context;
import android.webkit.JavascriptInterface;



public class WebAppInterface {
    Context mContext;

    public WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void setUserId(String userId) {
        MainActivity mainActivity = (MainActivity) mContext;
        mainActivity.setUserId(userId);
    }

    @JavascriptInterface
    public void setSessionId(String sessionId) {
        MainActivity mainActivity = (MainActivity) mContext;
        mainActivity.setSessionId(sessionId);
    }

    // 오퍼월 시작
    @JavascriptInterface
    public void openAdList() {
        MainActivity mainActivity = (MainActivity) mContext;
        mainActivity.openAdList();
    }
    // 오퍼월 끝
}
