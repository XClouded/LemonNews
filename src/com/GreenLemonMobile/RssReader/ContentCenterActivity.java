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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.GreenLemonMobile.RssReader.adapter.ContentCenterAdapter;
import com.GreenLemonMobile.RssReader.adapter.ContentCenterAdapter.ViewHolder;
import com.GreenLemonMobile.RssReader.database.DatabaseHelper;
import com.GreenLemonMobile.RssReader.entity.ContentCenterEntity;
import com.GreenLemonMobile.util.EditTextUtils;
import com.GreenLemonMobile.util.MyActivity;

import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;

import java.util.ArrayList;

public class ContentCenterActivity extends MyActivity {
	
	private int level;
	private String folder = DatabaseHelper.ROOT_FOLDER;
	
	private View loadingBar;
	private ListView listView;
	private ImageButton searchButton;
	private ImageButton deleteButton;
	private AutoCompleteTextView searchContent;
	private Button subscribedButton;
	
	private ArrayList<ContentCenterEntity> list;
	private ContentCenterAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_content_center);
		
		setupView();
	}
	
	@Override
	protected void onResume() {
	    updateList();

		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		
		if (subscribedButton != null) {
		    subscribedButton.setOnClickListener(null);
		    subscribedButton = null;
		}
		
		searchContent = null;
		
		itemClickListener = null;
		searchButtonClickListener = null;
		deleteButtonClickListener = null;
		mySubscribedClickListener = null;
		
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
	        if (level > 0) {
	            --level;
	            folder = folder.substring(0, folder.lastIndexOf('\\'));
	            updateList();
	            return true;
	        }
	    }
		return super.onKeyDown(keyCode, event);
	}
	
	public void startLoading() {		
		if (subscribedButton != null)
		    subscribedButton.setVisibility(View.GONE);
		
        if (loadingBar != null)
            loadingBar.setVisibility(View.VISIBLE);
	}
	
	public void stopLoading() {
		if (loadingBar != null)
			loadingBar.setVisibility(View.GONE);
		
		if (subscribedButton != null)
		    subscribedButton.setVisibility(View.VISIBLE);
	}
	
	private void updateList() {
        startLoading();
        list.clear();
        ContentCenterEntity.getContentCenterList(list, level, folder);
        if (adapter != null)
            adapter.notifyDataSetChanged();
        stopLoading();
	}

	private void setupView() {
		list = new ArrayList<ContentCenterEntity>();
		adapter = new ContentCenterAdapter(this, list);
		
		listView = (ListView) findViewById(R.id.listview);
		listView.setOnItemClickListener(itemClickListener);

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
		
		subscribedButton = (Button) findViewById(R.id.mySubscribedBtn);
		subscribedButton.setOnClickListener(mySubscribedClickListener);
	}
	
	private OnClickListener mySubscribedClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            finish();
            Intent intent = new Intent(ContentCenterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
	    
	};
	
	private OnClickListener searchButtonClickListener = new OnClickListener () {

		@Override
		public void onClick(View v) {
		    if (searchContent != null) {
		        String rssAddress = searchContent.getText().toString();
		        if (TextUtils.isEmpty(rssAddress)) {
		            searchContent.setError(EditTextUtils.ShowErrMsg(getString(R.string.no_seach_content)));
		            return;
		        }
		        Intent intent = new Intent(ContentCenterActivity.this, SearchActivity.class);
		        intent.putExtra(KEY1, rssAddress);
		        startActivity(intent);
		    }
		}
		
	};
	
	private OnClickListener deleteButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		    Object obj = view.getTag();
		    if (obj != null && obj instanceof ContentCenterAdapter.ViewHolder) {
		        ViewHolder holder = (ViewHolder) obj;
		        ContentCenterEntity entity = (ContentCenterEntity)(holder.obj);
		        if (entity != null) {
		            if (entity.getSubtag() == 1) {
		                ++level;
		                folder += "\\" + entity.getName();
		                updateList();
		                if (!list.isEmpty())
		                    listView.setSelection(1);
		            }
		        }
		    }
		}
		
	};
}
