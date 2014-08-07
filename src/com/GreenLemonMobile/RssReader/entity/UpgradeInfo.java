
package com.GreenLemonMobile.RssReader.entity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.util.NetUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JSONObjectProxy;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

public class UpgradeInfo {
    private static final String UPGRADE_CHECK_URL = "http://ibooksreader.googlecode.com/svn/trunk/rss_reader_latest_version.json";    
    
    enum UpdateMode {
        MUST, OPTIONAL
    }

    public String url;
    public String version;
    public String info;

    // 0: normal upgrade, 1: must upgrade
    public UpdateMode upgrade_mode = UpdateMode.OPTIONAL;

    public static void checkUpdate(final Context context, final boolean silendMode) {
//        String temp = "{\"result\":{\"url\":\"http:\\/\\/ibooksreader.googlecode.com\\/files\\/RssReader_1.0.1.apk\",\"version\":\"1.0.1\",\"info\":\"1. Improve the user experience\\r\\n\"}}";
//        try {
//            String value = URLEncoder.encode(temp, "UTF-8");
//            value = URLDecoder.decode(value, "UTF-8");
//            JSONObject object = new JSONObject(value);
//            temp = object.toString();
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//        } catch (UnsupportedEncodingException e2) {
//            e2.printStackTrace();
//        }        
        
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getText(R.string.request_software_update));
        if (!silendMode) {
            progressDialog.show();
        }
        AsyncHttpClient client = new AsyncHttpClient(false);
        
        String updateCheckUrl = UPGRADE_CHECK_URL;
        if (Locale.getDefault().getCountry().toLowerCase().contains("cn") || Locale.getDefault().getLanguage().toLowerCase().contains("zh")) 
            updateCheckUrl = UPGRADE_CHECK_URL;

        if (NetUtils.getType() != NetUtils.NO_NET) {
            client.get(context, updateCheckUrl, new JsonHttpResponseHandler() {
    
                @Override
                public void onSuccess(JSONObject response) {
                    super.onSuccess(response);
                    progressDialog.dismiss();
    
                    final UpgradeInfo upgradeInfo = new UpgradeInfo();
                    
                    if (response != null && !response.isNull("result")) {
                        try {
                            JSONObject jsonObject = response.getJSONObject("result");
                            upgradeInfo.url = jsonObject.getString("url");
                            upgradeInfo.info = jsonObject.getString("info");
                            upgradeInfo.version = jsonObject.getString("version");
                            upgradeInfo.upgrade_mode = (!jsonObject.isNull("mode") && jsonObject.getInt("mode") == 1) ? UpdateMode.MUST : UpdateMode.OPTIONAL;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    String currentVersion = "";
                    try {
                        PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                        currentVersion = pi.versionName;
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
    
                    if (TextUtils.isEmpty(currentVersion) || TextUtils.isEmpty(upgradeInfo.version) || currentVersion.equalsIgnoreCase(upgradeInfo.version)) {
                        if (!silendMode)
                            currentIsLatestVersion(context);
                        return;
                    }
                    
                    String[] currentVersionArray = currentVersion.split("\\.");
                    String[] latestVersionArray = upgradeInfo.version.split("\\.");
                    if (currentVersionArray == null || latestVersionArray == null) {
                        if (!silendMode)
                            currentIsLatestVersion(context);
                        return;
                    }
                    
                    if (currentVersionArray.length == latestVersionArray.length) {
                        boolean hasLatestVersion = false;
                        
                        for (int index = 0; index < currentVersionArray.length; ++index) {
                            String oldVersion = currentVersionArray[index];
                            String newVersion = latestVersionArray[index];
                            if (Integer.parseInt(newVersion) > Integer.parseInt(oldVersion)) {
                                hasLatestVersion = true;
                                break;
                            } else if (Integer.parseInt(newVersion) < Integer.parseInt(oldVersion)) {
                                break;
                            }
                        }
                        if (!hasLatestVersion) {
                            if (!silendMode)
                                currentIsLatestVersion(context);
                            return;
                        }
                    }
    
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle(R.string.tips);
                    String formatString = context.getResources().getString(R.string.version_new);
                    String message = String.format(formatString, upgradeInfo.version);
                    message += "\n\n" + context.getResources().getString(R.string.version_info) + "\n" + upgradeInfo.info;
                    dialogBuilder.setMessage(message);
                    
                    if (upgradeInfo.upgrade_mode == UpdateMode.OPTIONAL) {
                        dialogBuilder.setNegativeButton(R.string.version_cancel, new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                    
                    dialogBuilder.setPositiveButton(R.string.version_update, new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(upgradeInfo.url));
                            context.startActivity(intent);
                            
                            if (upgradeInfo.upgrade_mode == UpdateMode.MUST) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }
                    });
                    dialogBuilder.show();
                }
    
                @Override
                public void onSuccess(JSONArray response) {
                    super.onSuccess(response);
                    progressDialog.dismiss();
                }
    
                @Override
                protected void handleSuccessMessage(String responseBody) {
                    super.handleSuccessMessage(responseBody);
                }
    
                @Override
                protected Object parseResponse(String responseBody) throws JSONException {
                    try {
                        byte[] buffer = responseBody.getBytes();
                        if (buffer.length >=3 && (buffer[0] & 0xff)  == 0xEF && (buffer[1] & 0xff) == 0xBB && (buffer[2] & 0xff) == 0xBF) {
                            responseBody = new String(buffer, 3, buffer.length - 3);
                        }
                        responseBody = URLDecoder.decode(responseBody, "UTF-8");
                        JSONObjectProxy object = new JSONObjectProxy(new JSONObject(responseBody));
                        return object;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return super.parseResponse(responseBody);
                }
    
                @Override
                public void onFailure(Throwable e, JSONObject errorResponse) {
                    super.onFailure(e, errorResponse);
                    progressDialog.dismiss();
                    if (!silendMode)
                        currentIsLatestVersion(context);
                }
    
                @Override
                public void onFailure(Throwable e, JSONArray errorResponse) {
                    super.onFailure(e, errorResponse);
                    progressDialog.dismiss();
                    if (!silendMode)
                        currentIsLatestVersion(context);
                }
    
                @Override
                protected void handleFailureMessage(Throwable e, String responseBody) {
                    super.handleFailureMessage(e, responseBody);
                    progressDialog.dismiss();
                    if (!silendMode)
                        currentIsLatestVersion(context);
                }
    
            });
        }
    }
    
    private static void currentIsLatestVersion(Context context) {
        Toast.makeText(context, R.string.last_version, Toast.LENGTH_LONG).show();
    }
}
