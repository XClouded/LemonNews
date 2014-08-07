package com.GreenLemonMobile.RssReader.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.entity.FeedItemEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ChannelAdapter extends BaseAdapter {
    private final ArrayList<FeedItemEntity> list;
    private Context context;
    
    public ChannelAdapter(Context context, ArrayList<FeedItemEntity> feedsList) {
        super();
        this.context = context;
        this.list = feedsList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();            
            convertView = LayoutInflater.from(context).inflate(R.layout.channel_list_item, null);
            
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            holder.content = (TextView) convertView.findViewById(R.id.item_subtitle);
            holder.image = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        FeedItemEntity entity = (FeedItemEntity) getItem(position);
        holder.obj = entity;
        
        holder.title.setText(Html.fromHtml(entity.title));
        holder.title.setTextColor(entity.hasRead ? Color.GRAY : Color.BLACK);
        holder.content.setTextColor(Color.GRAY);
        
        String dateText = "";
        Date currentDate = new Date(System.currentTimeMillis());
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getDefault());
        Date publishDate = null;
        try {
            publishDate = inputFormat.parse(entity.pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
            publishDate = new Date();
        }

        if (currentDate.getYear() == publishDate.getYear() && currentDate.getMonth() == publishDate.getMonth() && currentDate.getDate() == publishDate.getDate()) {
            SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.one_hour_before_to_time_zero_format_text));
            df.setTimeZone(TimeZone.getDefault());
            dateText = df.format(publishDate);
        } else if (currentDate.getYear() == publishDate.getYear() && currentDate.getMonth() == publishDate.getMonth() && (currentDate.getDate() == publishDate.getDate() + 1)) {
            SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.yesterday_time_format_text));
            df.setTimeZone(TimeZone.getDefault());
            dateText = df.format(publishDate);
        } else {
            SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.yy_mm_dd_hh_mm_format_text));
            df.setTimeZone(TimeZone.getDefault());
            dateText = df.format(publishDate);
        }
        
        if (!TextUtils.isEmpty(entity.descriptionAsText)) {
            String endEllipsis = "...";
            String htmlText = entity.descriptionAsText.trim();
            int textCount = 140;
            
            switch (context.getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
            	textCount = 40;
            	break;
            case DisplayMetrics.DENSITY_MEDIUM:
            	textCount = 70;
            	break;
            case DisplayMetrics.DENSITY_HIGH:
            	textCount = 140;
            	break;
            }
            
            String content = "<br>" + dateText + "\r\n</br><br>" + (TextUtils.isEmpty(entity.author) ? "" : (context.getResources().getString(R.string.author) + entity.author)) + "\r\n</br>";
            String text = TextUtils.isEmpty(htmlText) ? "" : TextUtils.substring(htmlText, 0, htmlText.length() > textCount ? textCount : htmlText.length());
            if (htmlText.length() > textCount)
                text += endEllipsis;
            content += "<br>" + text + "</br>";
            
            holder.content.setText(Html.fromHtml(content));
        }
        
        return convertView;
    }

    public class ViewHolder {
        TextView title;
        ImageView image;
        TextView content;
        
        public Object obj;
    }
}
