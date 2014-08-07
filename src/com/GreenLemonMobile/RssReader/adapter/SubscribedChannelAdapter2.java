package com.GreenLemonMobile.RssReader.adapter;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;

import java.util.ArrayList;

public class SubscribedChannelAdapter2 extends BaseAdapter {
	
	private Activity context;
	private final ArrayList<SubscribedChannelEntity> channels;

	public SubscribedChannelAdapter2(Activity context, ArrayList<SubscribedChannelEntity> channels) {
		super();
		this.channels = channels;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return channels.size() + 1;
	}

	@Override
	public Object getItem(int position) {
	    if (position >= channels.size())
	        return null;
		return channels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			holder.obj = getItem(position);
			convertView = LayoutInflater.from(context).inflate(R.layout.subscribed_grid_item, null);
			
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.image = (ImageView) convertView.findViewById(R.id.item_image);
			holder.newImage = (ImageView) convertView.findViewById(R.id.new_tag);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.obj = getItem(position);
		}
		
		if (getItem(position) == null) {
		    holder.title.setText("");
		    holder.newImage.setVisibility(View.GONE);
	        holder.image.setImageDrawable(null);
	        holder.image.setTag(null);
		    holder.image.setImageResource(R.drawable.but_icon_add_bg);
		} else {
    		SubscribedChannelEntity entity = (SubscribedChannelEntity) holder.obj;
    		holder.title.setText(entity.getName());
            holder.image.setImageDrawable(null);
            holder.image.setTag(null);
            holder.image.setImageResource(R.drawable.rss);
            if (!TextUtils.isEmpty(entity.getImage())) {
                holder.image.setTag(Uri.parse(entity.getImage()));
            }
    		
    		holder.newImage.setVisibility(entity.isLatest() ? View.VISIBLE : View.GONE);
		}
		return convertView;
	}

	private class ViewHolder {
		TextView title;
		ImageView image;
		ImageView newImage;
		
		Object obj;
	}
}
