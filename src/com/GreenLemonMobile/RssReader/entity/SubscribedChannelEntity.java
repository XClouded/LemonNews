package com.GreenLemonMobile.RssReader.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.GreenLemonMobile.RssReader.Application;
import com.GreenLemonMobile.RssReader.ILoadListener;
import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.provider.DataProvider;
import com.GreenLemonMobile.feed4j.FeedIOException;
import com.GreenLemonMobile.feed4j.FeedParser;
import com.GreenLemonMobile.feed4j.FeedXMLParseException;
import com.GreenLemonMobile.feed4j.UnsupportedFeedException;
import com.GreenLemonMobile.feed4j.bean.Feed;
import com.GreenLemonMobile.feed4j.bean.FeedHeader;
import com.GreenLemonMobile.feed4j.bean.FeedItem;
import com.GreenLemonMobile.network.HttpGroup;
import com.GreenLemonMobile.network.HttpGroup.HttpError;
import com.GreenLemonMobile.network.HttpGroup.HttpRequest;
import com.GreenLemonMobile.network.HttpGroup.HttpResponse;
import com.GreenLemonMobile.network.HttpGroup.HttpSetting;
import com.GreenLemonMobile.util.MyActivity;
import com.GreenLemonMobile.util.MyApplication;
import com.GreenLemonMobile.util.ShowTools;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.StringHttpResponseHandler;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class SubscribedChannelEntity implements Serializable {
	
	/**
     * 
     */
    private static final long serialVersionUID = 100001L;
    private String name;
	private String url;
	private String image;
	private String channel;
	private long order;
	private long unread_news;
	private String latest_refresh_date;
	private boolean isLatest = false;
	private boolean editable = false;
	private boolean isChecked = false;
	private boolean loading = false;
	
	public static SubscribedChannelEntity findSubscribedChannel(final String channelName, final String folderName) {
	    SubscribedChannelEntity entity = null;
        String selection = "channel=? AND name=?";
        String[] selectionArgs = {folderName, channelName};
        String sortOrder = null;
        Cursor cursor = MyApplication.getInstance().getContentResolver().query(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, null, selection, selectionArgs, sortOrder);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex("name");
            int urlIndex = cursor.getColumnIndex("url");
            int imageIndex = cursor.getColumnIndex("image");
            int orderIndex = cursor.getColumnIndex("order_index");
            int channelIndex = cursor.getColumnIndex("channel");
            int unreadeIndex = cursor.getColumnIndex("unread_news");
            int latest_refresh_dateIndex = cursor.getColumnIndex("latest_refresh_date");
            
            entity = new SubscribedChannelEntity();
            entity.setName(cursor.getString(nameIndex));
            entity.setUrl(cursor.getString(urlIndex));
            entity.setImage(cursor.getString(imageIndex));
            entity.setOrder(cursor.getLong(orderIndex));
            entity.setChannel(cursor.getString(channelIndex));
            entity.setUnread_news(cursor.getLong(unreadeIndex));
            entity.setLatest_refresh_date(cursor.getString(latest_refresh_dateIndex));
        }
        
        if (cursor != null)
            cursor.close();
        
        return entity;
	}
	
	public static void getSubscribedChannelList(ArrayList<SubscribedChannelEntity> lists) {
	    lists.clear();
		Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, null, null, null, "order_index ASC");
		
		if (cursor != null && cursor.getCount() > 0) {
			int nameIndex = cursor.getColumnIndex("name");
			int urlIndex = cursor.getColumnIndex("url");
			int imageIndex = cursor.getColumnIndex("image");
			int orderIndex = cursor.getColumnIndex("order_index");
			int channelIndex = cursor.getColumnIndex("channel");
			int unreadeIndex = cursor.getColumnIndex("unread_news");
			int latest_refresh_dateIndex = cursor.getColumnIndex("latest_refresh_date");
			while (cursor.moveToNext()) {
				SubscribedChannelEntity entity = new SubscribedChannelEntity();
				
				entity.setName(cursor.getString(nameIndex));
				entity.setUrl(cursor.getString(urlIndex));
				entity.setImage(cursor.getString(imageIndex));
				entity.setOrder(cursor.getLong(orderIndex));
				entity.setChannel(cursor.getString(channelIndex));
				entity.setUnread_news(cursor.getLong(unreadeIndex));
				entity.setLatest_refresh_date(cursor.getString(latest_refresh_dateIndex));
				lists.add(entity);
			}
		}
		
		if (cursor != null)
			cursor.close();
		
		return;
	}
	
	public SubscribedChannelEntity() {
		super();
	}	

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOrder() {
		return order;
	}

	public void setOrder(long order) {
		this.order = order;
	}
	
    public long getUnread_news() {
        return unread_news;
    }

    public void setUnread_news(long unread_news) {
        this.unread_news = unread_news;
    }

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isLatest() {
		return isLatest;
	}

	public void setLatest(boolean isLatest) {
		this.isLatest = isLatest;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean hasUnReadItems() {
	    boolean hasUnReadItems = false;
	    String channelName = channel + "\\" + name;
	    String[] projection = {"count(*)"};
        String selection = "channelName=? AND hasRead=?";
        String[] selectionArgs = {channelName, Integer.toString(0)};
        String sortOrder = null;
        Cursor cursor = MyApplication.getInstance().getContentResolver().query(DataProvider.CONTENT_URI_FEEDDATABSE, projection, selection, selectionArgs, sortOrder);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
            hasUnReadItems = cursor.getInt(0) > 0 ? true : false;
        }
        
        if (cursor != null)
            cursor.close();
        
        return hasUnReadItems;
	}
	
    public void markAllAsRead() {
        String channelName = channel + "\\" + name;
        String where = "channelName=?";
        String[] selectionArgs = {
            channelName
        };

        ContentValues values = new ContentValues();
        values.put("hasRead", Integer.toString(1));

        MyApplication.getInstance().getContentResolver()
                .update(DataProvider.CONTENT_URI_FEEDDATABSE, values, where, selectionArgs);
    }
    
    public void refresh(final Context context, final ILoadListener listener) {
        AsyncHttpClient client = new AsyncHttpClient(false);
        if (listener != null)
            listener.onStart();
        try {
	        client.get(context, url, new StringHttpResponseHandler() {
	
	            @Override
	            public void onSuccess(String response) {
	                if (!TextUtils.isEmpty(response)) {
	                    try {
	                        Feed feed = FeedParser.parse(new URL(url), response);
	
	                        FeedHeader header = feed.getHeader();
	                        setName(header.getTitle());
	                        if (header.getImage() != null && header.getImage().getURL() != null)
	                            setImage(header.getImage().getURL().toString());
	                    } catch (FeedIOException e) {
	                        e.printStackTrace();
	                    } catch (FeedXMLParseException e) {
	                        e.printStackTrace();
	                    } catch (UnsupportedFeedException e) {
	                        e.printStackTrace();
	                    } catch (MalformedURLException e) {
	                        e.printStackTrace();
	                    }
	                }
	                if (listener != null)
	                    listener.onEnd();
	                super.onSuccess(response);
	            }
	
	            @Override
	            public void onFailure(String errorResponse) {
	                if (listener != null)
	                    listener.onEnd();
	                super.onFailure(errorResponse);
	            }
	            
	        });
        } catch (IllegalArgumentException e) {
            if (listener != null)
                listener.onEnd();
        }
    }
    
    public void refresh2(final MyActivity activity, final ILoadListener listener) {        
        HttpGroup httpGroup = activity.getStringHttpGroupAsynPool();
        HttpSetting setting = new HttpSetting();
        setting.setUrl(url);
        setting.setListener(new HttpGroup.CustomOnAllListener() {
            
            @Override
            public void onProgress(long max, long progress) {
            }
            
            @Override
            public void onError(HttpError error) {
                if (listener != null)
                    listener.onEnd();
            }
            
            @Override
            public void onEnd(HttpResponse httpResponse) {
                String content = httpResponse.getString();
                if (!TextUtils.isEmpty(content)) {
                    try {
                        Feed feed = FeedParser.parse(new URL(url), content);

                        FeedHeader header = feed.getHeader();
                        setName(header.getTitle());
                        if (header.getImage() != null && header.getImage().getURL() != null)
                            setImage(header.getImage().getURL().toString());
                    } catch (FeedIOException e) {
                        e.printStackTrace();
                    } catch (FeedXMLParseException e) {
                        e.printStackTrace();
                    } catch (UnsupportedFeedException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                if (listener != null)
                    listener.onEnd();
            }
            
            @Override
            public void onStart() {
                if (listener != null)
                    listener.onStart();
            }
        });
        
        httpGroup.add(setting);
    }
    
    public void refresh(final Context context, final ILoadListener listener, final boolean toast) {
        AsyncHttpClient client = new AsyncHttpClient(false);
        
        final long startTime = System.currentTimeMillis();
        if (listener != null)
            listener.onStart();
        try {
	        client.get(context, url, new StringHttpResponseHandler() {
	
	            @Override
	            public void onSuccess(String response) {
	                if (!TextUtils.isEmpty(response)) {
	                    try {
	                        Feed feed = FeedParser.parse(new URL(url), response);
	
	                        FeedHeader header = feed.getHeader();
	                        int items = feed.getItemCount();
	                        for (int i = 0; i < items; i++) {
	                            FeedItem item = feed.getItem(i);
	                            String channelName = channel + "\\" + name;
	                            FeedItemEntity.save(channelName, item);
	                        }
	                        ContentValues values = new ContentValues();
	                        Date date = new Date(System.currentTimeMillis());
	                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                        df.setTimeZone(TimeZone.getDefault());
	                        latest_refresh_date = df.format(date);
	                        values.put("latest_refresh_date", latest_refresh_date);
	                        String where = "name=? AND channel = ?";
	                        String[] selectionArgs = {name, channel};
	                        Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, values, where, selectionArgs);
	                    } catch (FeedIOException e) {
	                        e.printStackTrace();
	                    } catch (FeedXMLParseException e) {
	                        e.printStackTrace();
	                    } catch (UnsupportedFeedException e) {
	                        e.printStackTrace();
	                    } catch (MalformedURLException e) {
	                        e.printStackTrace();
	                    }
	                }
	                loading = false;
	                if (listener != null)
	                    listener.onEnd();
	                long endTime = System.currentTimeMillis() - startTime;
	                endTime /= 1000;
	                String format = context.getResources().getString(R.string.refresh_consume_seconds);
	                String notice = String.format(format, endTime);
	                if (toast)
	                    ShowTools.toastInThread(notice);
	                super.onSuccess(response);
	            }
	
	            @Override
	            public void onFailure(String errorResponse) {
	                if (listener != null)
	                    listener.onEnd();
	                super.onFailure(errorResponse);
	            }
	            
	        });
        } catch (IllegalArgumentException e) {
            if (listener != null)
                listener.onEnd();
        }
    }
	
	public HttpRequest refresh2(final MyActivity activity, final ILoadListener listener, final boolean toast) {
	    
	    HttpGroup httpGroup = activity.getStringHttpGroupAsynPool();
	    HttpSetting setting = new HttpSetting();
	    setting.setUrl(url);
	    setting.setListener(new HttpGroup.CustomOnAllListener() {
            
            @Override
            public void onProgress(long max, long progress) {
            }
            
            @Override
            public void onError(HttpError error) {
                if (listener != null)
                    listener.onEnd();
            }
            
            @Override
            public void onEnd(HttpResponse httpResponse) {
                String content = httpResponse.getString();
                if (!TextUtils.isEmpty(content)) {
                    try {
                        Feed feed = FeedParser.parse(new URL(url), content);

                        FeedHeader header = feed.getHeader();
                        int items = feed.getItemCount();
                        for (int i = 0; i < items; i++) {
                            FeedItem item = feed.getItem(i);
                            String channelName = channel + "\\" + name;
                            FeedItemEntity.save(channelName, item);
                        }
                        ContentValues values = new ContentValues();
                        Date date = new Date(System.currentTimeMillis());
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        df.setTimeZone(TimeZone.getDefault());
                        latest_refresh_date = df.format(date);
                        values.put("latest_refresh_date", latest_refresh_date);
                        String where = "name=? AND channel = ?";
                        String[] selectionArgs = {name, channel};
                        Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, values, where, selectionArgs);
                    } catch (FeedIOException e) {
                        e.printStackTrace();
                    } catch (FeedXMLParseException e) {
                        e.printStackTrace();
                    } catch (UnsupportedFeedException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                loading = false;
                if (listener != null)
                    listener.onEnd();
                long endTime = System.currentTimeMillis() - startTime;
                endTime /= 1000;
                String format = activity.getResources().getString(R.string.refresh_consume_seconds);
                String notice = String.format(format, endTime);
                if (toast)
                    ShowTools.toastInThread(notice);
            }
            
            @Override
            public void onStart() {
                if (listener != null)
                    listener.onStart();
                startTime = System.currentTimeMillis();
            }
            
            private long startTime;
        });
	    
	    return httpGroup.add(setting);
	}
	
//	public static void cancelSubscribe(Context context, SubscribedChannelEntity entity) {
//        String selection = "channel=? AND name=?";
//        String[] selectionArg = {entity.getChannel(), entity.getName()};
//        Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, selection, selectionArg);
//        
//        String channel = entity.getChannel() + "\\" + entity.getName();
//        String where = "channelName=?";
//        String[] selectionArgs = {channel};
//        int row = Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_FEEDDATABSE, where, selectionArgs);
//        if (row != 0) {
//            ComponentName component = new ComponentName(context, SubscribedFeedService.class);
//            Intent intent = new Intent(SubscribedFeedService.IMM_UPDATE_SERVICE);
//            intent.setComponent(component);
//            context.startService(intent);
//        }
//	}

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getLatest_refresh_date() {
        return latest_refresh_date;
    }

    public void setLatest_refresh_date(String latest_refresh_date) {
        this.latest_refresh_date = latest_refresh_date;
    }
}
