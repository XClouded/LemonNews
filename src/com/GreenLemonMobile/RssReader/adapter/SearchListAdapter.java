package com.GreenLemonMobile.RssReader.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.entity.ContentCenterEntity;

import java.util.ArrayList;

public class SearchListAdapter extends BaseAdapter {
	
	private Context context;
	private final ArrayList<ContentCenterEntity> contentList;
	
	public SearchListAdapter(Context context,
			ArrayList<ContentCenterEntity> contentList) {
		super();
		this.context = context;
		this.contentList = contentList;
	}

	@Override
	public int getCount() {
		return contentList.size();
	}

	@Override
	public Object getItem(int position) {
		return contentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			holder.obj = getItem(position);
			convertView = LayoutInflater.from(context).inflate(R.layout.content_center_list_item, null);
			
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.subTitle = (TextView) convertView.findViewById(R.id.item_subtitle);
			holder.image = (ImageView) convertView.findViewById(R.id.item_image);
			holder.addImage = (ImageView) convertView.findViewById(R.id.item_add);
			holder.addText = (TextView) convertView.findViewById(R.id.item_add_text);
			holder.nextTag = (ImageView) convertView.findViewById(R.id.item_next_tag);
			holder.subContainer = convertView.findViewById(R.id.sub_container);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.obj = getItem(position);
		}
		
		ContentCenterEntity entity = (ContentCenterEntity) holder.obj;
		holder.title.setText(entity.getName());
		holder.subTitle.setText(entity.getSubName());
        holder.image.setImageDrawable(null);
        holder.image.setTag(null);
        holder.image.setImageResource(R.drawable.rss);
        if (!TextUtils.isEmpty(entity.getImage())) {
            holder.image.setTag(Uri.parse(entity.getImage()));
        }
		
		if (TextUtils.isEmpty(entity.getSubName()))
		    holder.subTitle.setVisibility(View.GONE);
		else
		    holder.subTitle.setVisibility(View.VISIBLE);
		
		if (entity.getSubtag() == 1) {
		    holder.subContainer.setVisibility(View.INVISIBLE);
			holder.nextTag.setVisibility(View.VISIBLE);
			holder.addImage.setVisibility(View.GONE);
			holder.addText.setVisibility(View.GONE);
		} else {
			holder.nextTag.setVisibility(View.GONE);
			holder.addImage.setVisibility(View.VISIBLE);
			holder.addText.setVisibility(View.GONE);
			holder.subContainer.setVisibility(View.VISIBLE);
			
			holder.addImage.setBackgroundResource((entity.getSubscribed() == 1) ? R.drawable.but_icon_cancel : R.drawable.but_icon_add);
			holder.addText.setText((entity.getSubscribed() == 1) ? R.string.cancel_subscribe : R.string.add_subscribe);
		}
		
		holder.subContainer.setTag(entity);
		holder.subContainer.setOnClickListener(clickListener);
		
		return convertView;
	}
	
	private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() != null && v.getTag() instanceof ContentCenterEntity) {
                ContentCenterEntity entity = (ContentCenterEntity)v.getTag();                
                if (entity.getSubscribed() == 1) {
                    ContentCenterEntity.cancelSubscribe(context, entity);
//                    String channel = entity.getFolder() + "\\" + entity.getName();
//                    String selection = "channel=?";
//                    String[] selectionArg = {channel};
//                    Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, selection, selectionArg);
//                    entity.setSubscribed(0);
//                    
//                    ContentValues values = new ContentValues();
//                    values.clear();
//                    values.put("subscribed", entity.getSubscribed());
//                    String where = "name=? AND folder=?";
//                    String[] selectionArgs = {entity.getName(), entity.getFolder()};
//                    Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_CONTENTCENTER, values, where, selectionArgs);
                    SearchListAdapter.this.notifyDataSetChanged();
                } else {
                    ContentCenterEntity.subscribe(context, entity);
//                    String channel = entity.getFolder() + "\\" + entity.getName();
//                    ContentValues values = new ContentValues();
//                    values.put("name", entity.getName());
//                    values.put("url", entity.getUrl());
//                    values.put("image", entity.getImage());
//                    values.put("channel", channel);
//                    String[] projection = {"count(*)"};
//                    int orderIndex = 0;
//                    Cursor cursor = Application.getInstance().getContentResolver().query(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, projection, null, null, null);
//                    if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext())
//                        orderIndex = cursor.getInt(0);
//                    values.put("order_index", orderIndex);
//                    Application.getInstance().getContentResolver().insert(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, values);
//                    entity.setSubscribed(1);
//                    
//                    values.clear();
//                    values.put("subscribed", entity.getSubscribed());
//                    String where = "name=? AND folder=?";
//                    String[] selectionArgs = {entity.getName(), entity.getFolder()};
//                    Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_CONTENTCENTER, values, where, selectionArgs);
                    SearchListAdapter.this.notifyDataSetChanged();
                }
            }
        }
	    
	};

	public class ViewHolder {
		TextView title;
		TextView subTitle;
		ImageView image;
		ImageView nextTag;
		ImageView addImage;
		TextView addText;
		View subContainer;
		
		public Object obj;
	}
}
