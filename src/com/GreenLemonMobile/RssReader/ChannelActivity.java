
package com.GreenLemonMobile.RssReader;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.GreenLemonMobile.RssReader.adapter.ChannelAdapter;
import com.GreenLemonMobile.RssReader.adapter.ChannelAdapter.ViewHolder;
import com.GreenLemonMobile.RssReader.entity.FeedItemEntity;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;
import com.GreenLemonMobile.RssReader.service.SubscribedFeedService;
import com.GreenLemonMobile.concurrent.Flag;
import com.GreenLemonMobile.util.MyActivity;
import com.GreenLemonMobile.util.NetUtils;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ChannelActivity extends MyActivity {

    private TextView titleTextView;
    private View refreshButton;
    private View loadingBar;
    private ListView listView;
    private int currentPage = 0;
    private ArrayList<FeedItemEntity> list;
    private boolean noMoreItems = true;
    private ChannelAdapter adapter;
    private SubscribedChannelEntity entity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_channel);

        Intent intent = getIntent();
        String name = intent.getStringExtra(KEY1);
        String folder = intent.getStringExtra(KEY2);
        entity = SubscribedChannelEntity.findSubscribedChannel(name, folder);

        setupView();
        
        list.clear();
        updateList();
        titleTextView.setText(entity.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (refreshButton != null) {
            refreshButton.setOnClickListener(null);
            refreshButton.setBackgroundDrawable(null);
            refreshButton = null;
        }

        if (listView != null) {
            listView.setOnItemClickListener(null);
            listView.setOnTouchListener(null);
            listView = null;
        }

        refreshButton = null;
        loadingBar = null;

        refreshClickListener = null;
        itemClickListener = null;

        titleTextView = null;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subscribe_menu, menu);
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
            case R.id.menu_refresh:
                if (refreshClickListener != null)
                    refreshClickListener.onClick(refreshButton);
                break;
            case R.id.menu_delete:
                String channelName = entity.getChannel() + "\\" + entity.getName();
                FeedItemEntity.clearChannel(channelName);
                list.clear();
                currentPage = 0;
                updateList();
                ComponentName component = new ComponentName(this, SubscribedFeedService.class);
                intent = new Intent(SubscribedFeedService.IMM_UPDATE_SERVICE);
                intent.setComponent(component);
                startService(intent);
                break;
            case R.id.menu_markasread:
                entity.markAllAsRead();
                for (FeedItemEntity feed : list) {
                    feed.hasRead = true;
                }
                if (adapter != null)
                    adapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupView() {
        titleTextView = (TextView) findViewById(R.id.main_title_bar);

        refreshButton = findViewById(R.id.main_tab_refresh);
        refreshButton.setOnClickListener(refreshClickListener);

        loadingBar = findViewById(R.id.main_tab_loadingbar);

        listView = (ListView) findViewById(R.id.listview);
        list = new ArrayList<FeedItemEntity>();
        adapter = new ChannelAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);

        ((PullToRefreshListView) listView).setOnScrollListener(scrollListener);

        // Set a listener to be invoked when the list should be refreshed.
        ((PullToRefreshListView) listView).setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });
    }

    public void startLoading() {
        if (loadingBar != null)
            loadingBar.setVisibility(View.VISIBLE);

        if (refreshButton != null)
            refreshButton.setVisibility(View.GONE);
    }

    public void stopLoading() {
        if (loadingBar != null)
            loadingBar.setVisibility(View.GONE);

        if (refreshButton != null)
            refreshButton.setVisibility(View.VISIBLE);
    }

    public void updateLastUpdated() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getDefault());
        Date updateDate = null;
        try {
            updateDate = inputFormat.parse(entity.getLatest_refresh_date());
        } catch (ParseException e) {
            e.printStackTrace();
            updateDate = new Date();
        }

        SimpleDateFormat df = new SimpleDateFormat(getResources().getString(
                R.string.yy_mm_dd_hh_mm_refresh_text));
        df.setTimeZone(TimeZone.getDefault());
        ((PullToRefreshListView) listView).setLastUpdated(df.format(updateDate));
    }

    private void updateList() {
        if (list != null && adapter != null) {
            String channelName = entity.getChannel() + "\\" + entity.getName();
            FeedItemEntity.getFeedsList(list, channelName, currentPage);
            if (list.size() > 0)
                noMoreItems = false; 
            updateLastUpdated();
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    private OnScrollListener scrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    if (!noMoreItems && view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        loadMore();
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (!noMoreItems && view.getLastVisiblePosition() == (view.getCount() - 1)) {
                loadMore();
            }
        }

    };
    
    private void loadMore() {
        post(new Runnable() {

            @Override
            public void run() {
                if (listView != null)
                    ((PullToRefreshListView) listView).setFooterStatus(View.VISIBLE);
            }
            
        });
        
        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                post(new Runnable() {

                    @Override
                    public void run() {
                        if (list != null && adapter != null) {
                            int preCount = list.size();
                            ++currentPage;
                            String channelName = entity.getChannel() + "\\" + entity.getName();
                            FeedItemEntity.getFeedsList(list, channelName, currentPage);
                            if (preCount == list.size()) {
                                --currentPage;
                                noMoreItems = true;
                            }
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                        }
                        if (listView != null)
                            ((PullToRefreshListView) listView).setFooterStatus(View.GONE);
                    }
                    
                });
                super.run();
            }
            
        }.start();
    }

    private OnClickListener refreshClickListener = new OnClickListener() {

        @SuppressLint("ShowToast")
        @Override
        public void onClick(View v) {
            if (NetUtils.getType() == NetUtils.NO_NET) {
                Toast.makeText(ChannelActivity.this, getResources().getString(R.string.msg_nonetwork), Toast.LENGTH_SHORT).show();
                return;
            }
            entity.refresh(ChannelActivity.this, new ILoadListener() {

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
                    post(new Runnable() {

                        @Override
                        public void run() {
                            noMoreItems = false;
                            list.clear();
                            currentPage = 0;
                            updateList();
                            stopLoading();
                        }

                    });
                }

            }, false);
        }

    };

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (view.getTag() != null && view.getTag() instanceof ChannelAdapter.ViewHolder) {
                String channelName = entity.getChannel() + "\\" + entity.getName();
                
                ChannelAdapter.ViewHolder viewHolder = (ViewHolder) view.getTag();
                FeedItemEntity entity = (FeedItemEntity) viewHolder.obj;
                entity.setRead(true);
                adapter.notifyDataSetChanged();
                
                Intent intent = new Intent(ChannelActivity.this, ItemDetailActivity.class);
                intent.putExtra(KEY1, channelName);
                intent.putExtra(KEY2, entity.linkMD5);
                intent.putExtra(KEY3, ChannelActivity.this.entity.getName());
                intent.putExtra(KEY4, ChannelActivity.this.entity.getChannel());    
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

    };

    private class GetDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (NetUtils.getType() == NetUtils.NO_NET) {
                post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChannelActivity.this, getResources().getString(R.string.msg_nonetwork), Toast.LENGTH_SHORT).show();
                    }
                    
                });
                return null;
            }
            final Flag flag = new Flag();

            new Thread() {

                @Override
                public void run() {                    
                    entity.refresh(ChannelActivity.this, new ILoadListener() {

                        @Override
                        public void onStart() {
                            post(new Runnable() {

                                @Override
                                public void run() {
                                }

                            });
                        }

                        @Override
                        public void onEnd() {
                            post(new Runnable() {

                                @Override
                                public void run() {
                                    noMoreItems = false;
                                    flag.set();
                                    if (list != null) {
                                        list.clear();
                                        currentPage = 0;
                                        updateList();
                                    }
                                }

                            });
                        }

                    }, false);
                    super.run();
                }

            }.start();

            while (!flag.get()) {
                flag.waitFor(TimeUnit.SECONDS, 1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Call onRefreshComplete when the list has been refreshed.
            if (listView != null)
                ((PullToRefreshListView) listView).onRefreshComplete();

            super.onPostExecute(result);
        }
    }
}
