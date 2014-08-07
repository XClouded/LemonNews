package com.GreenLemonMobile.RssReader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.GreenLemonMobile.RssReader.adapter.SubscribedChannelAdapter;
import com.GreenLemonMobile.RssReader.adapter.SubscribedChannelAdapter2;
import com.GreenLemonMobile.RssReader.entity.ContentCenterEntity;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;
import com.GreenLemonMobile.RssReader.entity.UpgradeInfo;
import com.GreenLemonMobile.concurrent.Flag;
import com.GreenLemonMobile.util.HandlerExecutor;
import com.GreenLemonMobile.util.MyActivity;
import com.GreenLemonMobile.util.NetUtils;
import com.GreenLemonMobile.util.ShowTools;

import edu.mit.mobile.android.imagecache.ImageLoaderAdapter;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends MyActivity {
	
	private static final int MSG_ALPHA_TRANSLATE_SLIDING_IMAGE = 1;
	
	// 1 : list; 0 : grid
	public enum ViewType {
	    GRIDVIEW, LISTVIEW
	}	
	private static final String VIEW_TYPE_KEY = "view_type";
	private ViewType viewType = ViewType.LISTVIEW;
	
	/**
	 * list 列表相关
	 */
	private View changeViewButton;
	private View refreshButton;
	private View loadingBar;
	private ListView listView;
	private GridView gridView;
	private View listFoot;
	
	private ArrayList<SubscribedChannelEntity> list;
	private SubscribedChannelAdapter listAdapter;
	private SubscribedChannelAdapter2 gridAdapter;
	
	/**
	 * sliding image 相关
	 */
	private View slidingImage;
	private View slidingContainer;
	private View mainContainer;
	private GestureDetector gestureDetector;
	private Animation alpahAnim;
	private Animation slideOutAnim;
	private Animation slideInAnim;
	
	private int slideImageIndex = 0;
	private boolean bSildeGone = false;
	
	private int[] coverDrawableArray = {
			R.drawable.cover1
	};
	
	private boolean press_to_exit = false;
	
	private Random random = new Random();
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ALPHA_TRANSLATE_SLIDING_IMAGE:
//				handler.post(new Runnable() {
//
//					@Override
//					public void run() {
//						if (mainContainer != null) {
//							mainContainer.clearAnimation();
//							mainContainer.startAnimation(alpahAnim);
//						}
//					}
//					
//				});

				break;
			}
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		viewType = (PreferenceManager.getDefaultSharedPreferences(this).getInt(VIEW_TYPE_KEY, (viewType == ViewType.LISTVIEW) ? 1 : 0) == 0) ? ViewType.GRIDVIEW : ViewType.LISTVIEW;

		setupView();
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
	    UpgradeInfo.checkUpdate(this, true);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        canvas.setBitmap(outBitmap);
        getResources().getDrawable(R.drawable.cover1).draw(canvas);
        return super.onCreateThumbnail(outBitmap, canvas);
    }

    private void setupView() {
		/**
		 * list 列表相关
		 */
		changeViewButton = findViewById(R.id.main_tab_list);
		changeViewButton.setOnClickListener(changeViewTypeListener);
		
		refreshButton = findViewById(R.id.main_tab_refresh);
		refreshButton.setOnClickListener(refreshClickListener);
		
		loadingBar = findViewById(R.id.main_tab_loadingbar);
		
		gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setOnItemClickListener(listItemClickListener);
		
		listView = (ListView)findViewById(R.id.main_listview);
		listView.setOnItemClickListener(listItemClickListener);
		
		listFoot = LayoutInflater.from(this).inflate(R.layout.source_add_linear_item, null, false);
		listFoot.setOnClickListener(addRssListener);
		listView.addFooterView(listFoot);
		
		registerForContextMenu(listView);
		registerForContextMenu(gridView);
		
		list = new ArrayList<SubscribedChannelEntity>();
		listAdapter = new SubscribedChannelAdapter(this, list);
		gridAdapter = new SubscribedChannelAdapter2(this, list);
		
        int iconWidth = getResources().getDimensionPixelSize(R.dimen.rss_icon_width);
        int iconHeight = getResources().getDimensionPixelSize(R.dimen.rss_icon_height);
		listView.setAdapter(new ImageLoaderAdapter(this, listAdapter, Application.imageCache, new int[]{R.id.item_image}, iconWidth, iconHeight, ImageLoaderAdapter.UNIT_PX));
		gridView.setAdapter(new ImageLoaderAdapter(this, gridAdapter, Application.imageCache, new int[]{R.id.item_image}, iconWidth, iconHeight, ImageLoaderAdapter.UNIT_PX));
		
		changeViewType(viewType);

		/**
		 * sliding image 相关
		 */		
		alpahAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
		slideOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		
		mainContainer = findViewById(R.id.main_container);
		
		gestureDetector = new GestureDetector(gestureDectectorListener);
		slidingImage = findViewById(R.id.main_sliding_image);
		slidingContainer = findViewById(R.id.main_sliding_container);
		slidingContainer.setClickable(false);
		slidingContainer.setFocusableInTouchMode(false);
		mainContainer.setFocusableInTouchMode(true);
		
		slideOutAnim.setAnimationListener(slideAnimationListener);
		slideInAnim.setAnimationListener(slideAnimationListener);
		
		bSildeGone = true;
		Intent intent = getIntent();
		if (intent != null && intent.getAction() != null) {
		    if (intent.getAction().equals(Intent.ACTION_MAIN)) {
		        bSildeGone = false;		        
		    }
		}
		slidingContainer.setVisibility(bSildeGone ? View.GONE : View.VISIBLE);
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
    
        return super.dispatchTouchEvent(event);
    }
	@Override
	protected void onResume() {
		if (slidingContainer.getVisibility() == View.VISIBLE) {
			slideImageIndex = random.nextInt(coverDrawableArray.length);
			slidingImage.setBackgroundResource(coverDrawableArray[slideImageIndex]);
			
			handler.sendEmptyMessage(MSG_ALPHA_TRANSLATE_SLIDING_IMAGE);
		}
		updateList();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (slidingImage != null) {
			slidingImage.setBackgroundDrawable(null);
			slidingImage.setOnTouchListener(null);
			slidingImage = null;
		}
		
		if (changeViewButton != null) {
			changeViewButton.setBackgroundDrawable(null);
			changeViewButton.setOnClickListener(null);
			changeViewButton = null;
		}
		
		if (refreshButton != null) {
			refreshButton.setBackgroundDrawable(null);
			refreshButton.setOnClickListener(null);
			refreshButton = null;
		}
		
		getWindow().getDecorView().setOnTouchListener(null);
		
		if (listView != null) {
			listView.setOnItemClickListener(null);
			listView.setOnLongClickListener(null);
			listView.setOnTouchListener(null);
			if (listFoot != null)
				listView.removeFooterView(listFoot);
			listView = null;
		}
		
		if (gridView != null) {
		    gridView.setOnItemClickListener(null);
		    gridView.setOnLongClickListener(null);
		    gridView = null;
		}
		
		if (listFoot != null) {
			listFoot.setOnClickListener(null);
			listFoot = null;
		}
		
		mainContainer = null;		
		slidingContainer = null;
		gestureDetector = null;
		slideOutAnim = null;
		slideInAnim = null;
		alpahAnim = null;
		
		slideAnimationListener = null;
		refreshClickListener = null;
		gestureDectectorListener = null;
		listItemClickListener = null;
		changeViewTypeListener = null;
		
		if (list != null)
			list.clear();
		
		list = null;
		listAdapter = null;
		gridAdapter = null;
		
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			if (!press_to_exit) {
				press_to_exit = true;
				Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
				return true;
			} else {
				super.onKeyDown(keyCode, event);
				//android.os.Process.killProcess(android.os.Process.myPid());
				finish();
				return true;
			}
		} else {
			press_to_exit = false;
		}
		return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    press_to_exit = false;
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_content_center:
		    Intent intent = new Intent(MainActivity.this, ContentCenterActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(intent);
		    break;
		case R.id.menu_search:
		    startActivity(new Intent(MainActivity.this, SearchActivity.class));
			break;
		case R.id.menu_setting:
		    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			break;
		case R.id.menu_about:
			startActivity(new Intent(MainActivity.this, AboutActivity.class));
			break;
		case R.id.menu_exit:
			new AlertDialog.Builder(this)
			.setTitle(R.string.exit_dialog_title)
			.setIcon(R.drawable.ic_dialog_pointout)
			.setMessage(R.string.exit_dialog_message)
			.setPositiveButton(R.string.confirm_button, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			})
			.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	    press_to_exit = false;
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    int index = (int) info.id;
	    if (index < list.size()) {	    
    		menu.setHeaderIcon(R.drawable.ic_dialog_pointout);
    		getMenuInflater().inflate(R.menu.main_context_menu, menu);		
    		if (v instanceof ListView) {
    			menu.setHeaderTitle(R.string.app_name);
    		}
	    }
		super.onCreateContextMenu(menu, v, menuInfo);		
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
	    int index = (int) menuInfo.id;
	    final SubscribedChannelEntity entity = list.get(index);
	    switch (item.getItemId()) {
	        case R.id.menu_open:
	            Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
	            intent.putExtra(KEY1, entity.getName());
	            intent.putExtra(KEY2, entity.getChannel());
	            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            break;
	        case R.id.menu_cancel_subscribe:
	            ContentCenterEntity channel = new ContentCenterEntity();
	            channel.setName(entity.getName());
	            String channelFolder = entity.getChannel();
	            int skipLength = 0;
	            String skipString = "\\" + entity.getName();
	            skipLength = skipString.length();
	            channelFolder = channelFolder.substring(0, channelFolder.length() - skipLength);
	            channel.setFolder(channelFolder);
	            ContentCenterEntity.cancelSubscribe(MainActivity.this, channel);
	            updateList();
	            break;
	        case R.id.menu_refresh:
	            if (NetUtils.getType() == NetUtils.NO_NET) {
	                Toast.makeText(MainActivity.this, getResources().getString(R.string.msg_nonetwork), Toast.LENGTH_SHORT).show();
	                break;
	            }
	            entity.refresh(this, new ILoadListener() {

                    @Override
                    public void onStart() {
                        post(new Runnable() {

                            @Override
                            public void run() {
                                startLoading();
                                gridAdapter.notifyDataSetChanged();
                                listAdapter.notifyDataSetChanged();
                            }
                            
                        });
                    }

                    @Override
                    public void onEnd() {
                        post(new Runnable() {

                            @Override
                            public void run() {
                                stopLoading();
                                gridAdapter.notifyDataSetChanged();
                                listAdapter.notifyDataSetChanged();
                            }
                            
                        });
                    }
	                
	            }, true);
	            break;
            default:
                break;
	    }
		return super.onContextItemSelected(item);
	}

	public void startLoading() {
		if (loadingBar != null)
			loadingBar.setVisibility(View.VISIBLE);
		
		if (refreshButton != null)
			refreshButton.setVisibility(View.GONE);
		
		if (changeViewButton != null)
			changeViewButton.setVisibility(View.GONE);
	}
	
	public void stopLoading() {
		if (loadingBar != null)
			loadingBar.setVisibility(View.GONE);
		
		if (refreshButton != null)
			refreshButton.setVisibility(View.VISIBLE);
		
		if (changeViewButton != null)
			changeViewButton.setVisibility(View.VISIBLE);
	}
	
    @SuppressWarnings("unchecked")
    private void updateList() {
        SubscribedChannelEntity.getSubscribedChannelList(list);
        if (!list.isEmpty()) {
            new checkHasUnReadTask().execute(list);
        }
        if (listAdapter != null)
            listAdapter.notifyDataSetChanged();
        if (gridAdapter != null)
            gridAdapter.notifyDataSetChanged();
        refreshButton.setEnabled(list.size() > 0 ? true : false);
    }
    
    class checkHasUnReadTask extends AsyncTask<ArrayList<SubscribedChannelEntity>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(ArrayList<SubscribedChannelEntity>... params) {
            ArrayList<SubscribedChannelEntity> entitys = params[0];
            boolean hasUnRead = false;
            for (SubscribedChannelEntity entity : entitys) {
                entity.setLatest(entity.hasUnReadItems());
                
                hasUnRead = hasUnRead ? hasUnRead : entity.isLatest();
            }
            return Boolean.valueOf(hasUnRead);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && listAdapter != null)
                listAdapter.notifyDataSetChanged();
            if (result && gridAdapter != null)
                gridAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
        
    }
    
	private OnClickListener refreshClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		    press_to_exit = false;
            if (NetUtils.getType() == NetUtils.NO_NET) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.msg_nonetwork), Toast.LENGTH_SHORT).show();
                return;
            }
		    final ArrayList<SubscribedChannelEntity> lists = new ArrayList<SubscribedChannelEntity>();
		    lists.addAll(list);
			new Thread() {

                @Override
                public void run() {
                    super.run();
                    
                    final long startTime = System.currentTimeMillis();
                    
                    HandlerExecutor.getUiThreadExecutor().execute(new Runnable(){

                        @Override
                        public void run() {
                            startLoading();
                        }
                        
                    });
                    
                    ArrayList<Flag> httpRequestList = new ArrayList<Flag>();
                    for (final SubscribedChannelEntity entity : lists) {
                        final Flag flag = new Flag();
                        httpRequestList.add(flag);
                        entity.refresh(MainActivity.this, new ILoadListener() {

                            @Override
                            public void onStart() {
                                post(new Runnable() {

                                    @Override
                                    public void run() {
                                        listAdapter.notifyDataSetChanged();
                                        gridAdapter.notifyDataSetChanged();
                                    }
                                    
                                });
                            }

                            @Override
                            public void onEnd() {
                                post(new Runnable() {

                                    @Override
                                    public void run() {
                                        flag.set();
                                        listAdapter.notifyDataSetChanged();
                                        gridAdapter.notifyDataSetChanged();
                                    }
                                    
                                });
                            }
                            
                        }, false);
                    }
                    
                    boolean refreshFinished = false;
                    while (!refreshFinished) {
                        refreshFinished = true;
                        for (Flag flag : httpRequestList) {
                            if (!flag.get()) {
                                refreshFinished = false;
                                break;
                            }
                        }
                        
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    HandlerExecutor.getUiThreadExecutor().execute(new Runnable(){

                        @Override
                        public void run() {
                            stopLoading();
                            listAdapter.notifyDataSetChanged();
                            gridAdapter.notifyDataSetChanged();
                            
                            long endTime = System.currentTimeMillis() - startTime;
                            endTime /= 1000;
                            String format = getResources().getString(R.string.refresh_consume_seconds);
                            String notice = String.format(format, endTime);
                            ShowTools.toastInThread(notice);
                            
                            updateList();
                        }
                        
                    });
                }
			    
			}.start();
		}
		
	};
	
	private void changeViewType(ViewType type) {
	    viewType = type;
        switch (viewType) {
            case GRIDVIEW:
                listView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                changeViewButton.setBackgroundResource(R.drawable.button_home_edit_bg);
                break;
            case LISTVIEW:
                listView.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                changeViewButton.setBackgroundResource(R.drawable.button_home_complete_bg);
                break;
        }
        
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(VIEW_TYPE_KEY, (viewType == ViewType.GRIDVIEW) ? 0 : 1);
        editor.commit();
        
        listAdapter.notifyDataSetInvalidated();
        gridAdapter.notifyDataSetChanged();
	}
	
	private OnClickListener changeViewTypeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		    changeViewType(viewType == ViewType.GRIDVIEW ? ViewType.LISTVIEW : ViewType.GRIDVIEW);
		}
		
	};
	
    private OnClickListener addRssListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, ContentCenterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    };
	
	private OnItemClickListener listItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
		    press_to_exit = false;
		    if (arg2 >= list.size()) {
	            Intent intent = new Intent(MainActivity.this, ContentCenterActivity.class);
	            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
		    } else {
                final SubscribedChannelEntity entity = list.get(arg2);
    
                Intent intent = new Intent(MainActivity.this, ChannelActivity.class);
                intent.putExtra(KEY1, entity.getName());
                intent.putExtra(KEY2, entity.getChannel());
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
		    }
		}
		
	};
	
	private AnimationListener slideAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			// TODO Auto-generated method stub
			slidingContainer.setVisibility(bSildeGone ? View.GONE : View.VISIBLE);
			if (arg0 == slideInAnim) {
				handler.sendEmptyMessage(MSG_ALPHA_TRANSLATE_SLIDING_IMAGE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private OnGestureListener gestureDectectorListener = new OnGestureListener() {

		@Override
		public boolean onDown(MotionEvent arg0) {
			if (slidingContainer.getVisibility() == View.VISIBLE)
			    return true;
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {			
			float absoluteY = Math.abs(e1.getY() - e2.getY());
			float absoluteX = Math.abs(e1.getX() - e2.getX());

			if (absoluteX >= absoluteY && absoluteX > 150) {
				// 水平滑动
				if (e2.getX() < e1.getX()) {
					bSildeGone = true;
					if (slidingContainer.getVisibility() == View.VISIBLE) {
					    slideImageIndex = random.nextInt(coverDrawableArray.length);
					    slidingImage.setBackgroundResource(coverDrawableArray[slideImageIndex]);
					    slidingImage.invalidate();
					    
						slidingContainer.clearAnimation();
						slidingContainer.startAnimation(slideOutAnim);
					}
				} else {
					bSildeGone = false;
					
					if (slidingContainer.getVisibility() == View.GONE) {
						slidingContainer.setVisibility(bSildeGone ? View.GONE : View.VISIBLE);
						slidingContainer.setBackgroundResource(coverDrawableArray[slideImageIndex]);
						slidingContainer.clearAnimation();
						slidingContainer.startAnimation(slideInAnim);
					}
				}
				return true;
			} else {
				// 垂直滑动
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}		
	};
}
