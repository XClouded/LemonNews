package com.GreenLemonMobile.RssReader;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.adapter.ContentCenterAdapter;
import com.GreenLemonMobile.RssReader.database.DatabaseHelper;
import com.GreenLemonMobile.RssReader.entity.ContentCenterEntity;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;
import com.GreenLemonMobile.concurrent.Flag;
import com.GreenLemonMobile.util.EditTextUtils;
import com.GreenLemonMobile.util.MyActivity;

import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends MyActivity {
	private String folder = DatabaseHelper.ROOT_FOLDER;
	
	private View loadingBar;
	private ListView listView;
	private ImageButton searchButton;
	private ImageButton deleteButton;
	private View searchTipContainer;
	private TextView searchTipText;
	private AutoCompleteTextView searchContent;
	
	private ArrayList<ContentCenterEntity> list;
	private ContentCenterAdapter adapter;	
	
	private String rssAddress;
	
	private boolean searchThreadFinish = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_search);
		
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //String query = intent.getStringExtra(SearchManager.QUERY);
            //doMySearch(query);
            finish();
        } else {
            rssAddress = intent.getStringExtra(KEY1);
            if (rssAddress == null)
                rssAddress = "";
            setupView();
            
            if (!TextUtils.isEmpty(rssAddress)) {
                searchContent.setText(rssAddress);
                searchButtonClickListener.onClick(searchButton);
            }
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (loadingBar != null) {
			loadingBar.setBackgroundDrawable(null);
			loadingBar = null;
		}
		
		if (listView != null) {
			listView.setOnItemClickListener(null);
			listView.setOnLongClickListener(null);
			listView = null;
		}
		
		if (searchButton != null) {
			searchButton.setOnClickListener(null);
			searchButton.setBackgroundDrawable(null);
			searchButton = null;
		}
		
		if (deleteButton != null) {
			deleteButton.setOnClickListener(null);
			deleteButton.setBackgroundDrawable(null);
			deleteButton = null;
		}
		
		if (list != null) {
			list.clear();
			list = null;
		}
		
		searchContent = null;
		
		searchButtonClickListener = null;
		deleteButtonClickListener = null;
		
		super.onDestroy();
	}
	
   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.content_center, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }
	    
	public void startLoading() {		
        if (loadingBar != null)
            loadingBar.setVisibility(View.VISIBLE);
	}
	
	public void stopLoading() {
		if (loadingBar != null)
			loadingBar.setVisibility(View.GONE);
	}
	
	private void updateList() {
        startLoading();
        searchTipContainer.setVisibility(View.VISIBLE);
        String format = getResources().getString(list.isEmpty() ? R.string.search_noresult : R.string.search_result);
        String tipText = list.isEmpty() ? format : String.format(format, list.size());
        
        searchTipText.setText(tipText);
        if (!list.isEmpty() && adapter != null) {
            adapter.notifyDataSetChanged();
        }
        stopLoading();
	}

	private void setupView() {
		list = new ArrayList<ContentCenterEntity>();
		adapter = new ContentCenterAdapter(this, list);
		
		searchTipContainer = findViewById(R.id.search_tip_linear);
		searchTipText = (TextView) findViewById(R.id.search_tip_text);
		
		listView = (ListView) findViewById(R.id.listview);
        int iconWidth = getResources().getDimensionPixelSize(R.dimen.rss_icon_width);
        int iconHeight = getResources().getDimensionPixelSize(R.dimen.rss_icon_height);
        listView.setAdapter(new ImageLoaderAdapter(this, adapter, Application.imageCache, new int[]{R.id.item_image}, iconWidth, iconHeight, ImageLoaderAdapter.UNIT_PX));
		
		loadingBar = findViewById(R.id.main_tab_loadingbar);
		
		searchButton = (ImageButton) findViewById(R.id.custom_search_go);
		searchButton.setOnClickListener(searchButtonClickListener);
		
		deleteButton = (ImageButton) findViewById(R.id.custom_search_delete);
		deleteButton.setOnClickListener(deleteButtonClickListener);
		
		searchContent = (AutoCompleteTextView) findViewById(R.id.custom_search_content);
		searchContent.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                searchContent.setError(null);
                return false;
            }
		    
		});
	}
	
	private OnClickListener searchButtonClickListener = new OnClickListener () {

		@Override
		public void onClick(View v) {
		    if (searchContent != null && searchThreadFinish) {
		        String rssAddress2 = searchContent.getText().toString();
		        if (TextUtils.isEmpty(rssAddress2)) {
		            searchContent.setError(EditTextUtils.ShowErrMsg(getString(R.string.no_seach_content)));
		            return;
		        }
		        searchThreadFinish = false;
		        rssAddress = rssAddress2;
		        list.clear();
		        adapter.notifyDataSetChanged();
		        searchTipContainer.setVisibility(View.GONE);
		        
		        final ContentCenterEntity contentFeed = new ContentCenterEntity();
                final SubscribedChannelEntity entity = new SubscribedChannelEntity();
                entity.setChannel(folder);
                entity.setUrl(rssAddress);
                contentFeed.setFolder(folder);
                contentFeed.setUrl(rssAddress);
                contentFeed.setTag(0);
                contentFeed.setSubtag(0);
                contentFeed.setSubscribed(0);

                final Flag flag = new Flag();
                new Thread() {

                    @Override
                    public void run() {
                        new Thread() {

                            @Override
                            public void run() {
                                entity.refresh(SearchActivity.this, new ILoadListener() {

                                    @Override
                                    public void onStart() {
                                        post(new Runnable() {

                                            @Override
                                            public void run() {
                                                startLoading();
                                            }

                                        });
                                    }

                                    @Override
                                    public void onEnd() {
                                        searchThreadFinish = true;
                                        post(new Runnable() {

                                            @Override
                                            public void run() {
                                                flag.set();
                                                if (list != null) {
                                                    list.clear();
                                                    contentFeed.setName(entity.getName());
                                                    contentFeed.setImage(entity.getImage());
                                                    list.add(contentFeed);
                                                    updateList();
                                                }
                                                stopLoading();
                                            }

                                        });
                                    }

                                });
                                super.run();
                            }

                        }.start();

                        while (!flag.get()) {
                            flag.waitFor(TimeUnit.SECONDS, 1);
                        }
                        super.run();
                    }
                    
                }.start();
		    }
		}
		
	};
	
	private OnClickListener deleteButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		}
		
	};
}
