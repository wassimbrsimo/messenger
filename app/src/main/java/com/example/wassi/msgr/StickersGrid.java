package com.example.wassi.msgr;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class StickersGrid extends BaseAdapter {
    private Context mContext;

    // Keep all Images in array
    public Integer[] mThumbIds = {
            R.drawable.bubble_rectangle,R.drawable.first,R.drawable.pic,R.drawable.notif,
            R.drawable.bubble_rectangle,R.drawable.first,R.drawable.pic,R.drawable.notif,
            R.drawable.bubble_rectangle,R.drawable.first,R.drawable.pic,R.drawable.notif,
            R.drawable.bubble_rectangle,R.drawable.first,R.drawable.pic,R.drawable.notif,

    };

    // Constructor
    public StickersGrid(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position) {
        return mThumbIds[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mThumbIds[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
        return imageView;
    }
}
