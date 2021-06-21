package com.insomenia.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.insomenia.mobile.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaseUtil {
    public static void makeAlert(Activity activity, String title, String message, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no, int yesStringId, int noStringId) {
        new AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(yesStringId, yes)
            .setNegativeButton(noStringId, no).show();
    }


    public static class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String content = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }
            } catch (Exception e) {

            }
            return content;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {

        }
    }

    public static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(Constants.PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getPrefEditor(Context context) {
        return getPref(context).edit();
    }

    public static void setStringPref(Context context, String key, String value) {
        getPrefEditor(context).putString(key, value).commit();
    }

    public static String getStringPref(Context context, String key, String value) {
        return getPref(context).getString(key, value);
    }

    public static void setBoolPref(Context context, String key, boolean value) {
        getPrefEditor(context).putBoolean(key, value).commit();
    }

    public static boolean getBoolPref(Context context, String key, boolean value) {
        return getPref(context).getBoolean(key, value);
    }
}
