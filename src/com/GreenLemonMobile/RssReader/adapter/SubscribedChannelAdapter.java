package com.GreenLemonMobile.RssReader.adapter;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;

import java.util.ArrayList;

public class SubscribedChannelAdapter extends BaseAdapter {
	
	private Activity context;
	private final ArrayList<SubscribedChannelEntity> channels;

	public SubscribedChannelAdapter(Activity context, ArrayList<SubscribedChannelEntity> channels) {
		super();
		this.channels = channels;
		this.context = context;
	}

	@Override
	public int getCount() {
		return channels.size();
	}

	@Override
	public Object getItem(int position) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.subscribed_list_item, null);
			
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.image = (ImageView) convertView.findViewById(R.id.item_image);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.item_checkbox);
			holder.newImage = (ImageView) convertView.findViewById(R.id.new_tag);
			holder.loadingBar = convertView.findViewById(R.id.main_tab_loadingbar);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.obj = getItem(position);
		}
		
		SubscribedChannelEntity entity = (SubscribedChannelEntity) holder.obj;
		holder.title.setText(entity.getName());
        holder.image.setImageDrawable(null);
        holder.image.setTag(null);
        holder.image.setImageResource(R.drawable.rss);
        if (!TextUtils.isEmpty(entity.getImage())) {
            holder.image.setTag(Uri.parse(entity.getImage()));
        }
		
		holder.newImage.setVisibility(entity.isLatest() ? View.VISIBLE : View.GONE);
		holder.checkbox.setVisibility(entity.isEditable() ? View.VISIBLE : View.GONE);
		holder.loadingBar.setVisibility(entity.isLoading() ? View.VISIBLE : View.GONE);
		return convertView;
	}

	private class ViewHolder {
		TextView title;
		ImageView image;
		CheckBox checkbox;
		ImageView newImage;
		View loadingBar;
		
		Object obj;
	}
}
