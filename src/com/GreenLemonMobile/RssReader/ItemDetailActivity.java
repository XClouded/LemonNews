package com.GreenLemonMobile.RssReader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.entity.FeedItemEntity;
import com.GreenLemonMobile.util.MyActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ItemDetailActivity extends MyActivity implements OnClickListener {
    private WebView webView;
    private TextView title;
    private TextView subTitle;
    private View loadingBar;
    private ArrayList<FeedItemEntity> list;
    private int index;
    
    private String subscribedChannelName;
    private String subscribedChannelFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String channelName = intent.getStringExtra(KEY1);
        list = new ArrayList<FeedItemEntity>();
        FeedItemEntity.getFeedsList(list, channelName);
        String linkMD5 = intent.getStringExtra(KEY2);
        subscribedChannelName = intent.getStringExtra(KEY3);
        subscribedChannelFolder = intent.getStringExtra(KEY4);
        index = 0;
        for (FeedItemEntity entity : list) {
            if (entity.linkMD5.equals(linkMD5)) {
                break;
            }
            ++index;
        }
        setupView();
        
        setupItem();
    }

    @Override
    protected void onResume() {        
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.menu_article_list:
                finish();
                intent = new Intent(this, ChannelActivity.class);
                intent.putExtra(KEY1, subscribedChannelName);
                intent.putExtra(KEY2, subscribedChannelFolder);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean usingColumnKey = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("system_setting_columnkey", true);
        if (usingColumnKey && (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode || KeyEvent.KEYCODE_VOLUME_UP == keyCode)) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (index < list.size() - 1) {
                        ++index;
                        list.get(index).setRead(true);
                        setupItem();
                    }
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (index > 0) {
                        --index;
                        list.get(index).setRead(true);
                        setupItem();
                    }
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupView() {
        webView = (WebView) findViewById(R.id.webview);
        title = (TextView) findViewById(R.id.item_title);
        subTitle = (TextView) findViewById(R.id.item_subtitle);
        loadingBar = findViewById(R.id.main_tab_loadingbar);
        
        webView.setClickable(true);
        WebSettings settings = webView.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setPluginsEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
//        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setRenderPriority(RenderPriority.HIGH);
        
        int fontSize = 20;
        String fontString = PreferenceManager.getDefaultSharedPreferences(this).getString(
                "system_setting_textsize", "0");        
        if (fontString.equals("-1"))
            fontSize = 16;
        else if (fontString.equals("0"))
            fontSize = 20;
        else if (fontString.equals("1"))
            fontSize = 24;
        else if (fontString.equals("2"))
            fontSize = 28;
        settings.setDefaultFontSize(fontSize);
        settings.setDefaultFixedFontSize(fontSize);
        
//        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
//            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                startLoading();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                stopLoading();
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {
                stopLoading();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            
        });
    }
    
    private void setupItem() {
        if (index >= 0 && index < list.size()) {
            FeedItemEntity entity = list.get(index);
            
            title.setText(entity.title.trim());
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            inputFormat.setTimeZone(TimeZone.getDefault());
            Date publishDate = null;
            try {
                publishDate = inputFormat.parse(entity.pubDate);
            } catch (ParseException e) {
                e.printStackTrace();
                publishDate = new Date();
            }

            String subText;
            SimpleDateFormat df = new SimpleDateFormat(getResources().getString(R.string.yy_mm_dd_hh_mm_format_text));
            df.setTimeZone(TimeZone.getDefault());
            subText = df.format(publishDate);
            
            if (!TextUtils.isEmpty(entity.author)) {
                subText += "  " + getResources().getString(R.string.cover_author_from) + entity.author;
            }
            subTitle.setText(subText);
            
            String URI_PREFIX = entity.link;
            String html = ("<html><body><header></header><div>"
                    + (!TextUtils.isEmpty(entity.descriptionAsText) ? entity.descriptionAsText : "") + "</div></body></html>");

            webView.loadDataWithBaseURL(URI_PREFIX,
                    html,
                    "text/html",
                    "utf-8",
                    "");
        }
    }

    private void startLoading() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    private void stopLoading() {
        loadingBar.setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_item:
                if (index > 0) {
                    --index;
                    list.get(index).setRead(true);
                    setupItem();
                }
                break;
            case R.id.next_item:
                if (index < list.size() - 1) {
                    ++index;
                    list.get(index).setRead(true);
                    setupItem();
                }
                break;
            case R.id.view_original_link:
                if (!TextUtils.isEmpty(list.get(index).link)) {
                    webView.loadUrl(list.get(index).link);
                }
                break;
        }
    }
}
