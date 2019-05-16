package com.example.wassi.msgr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static com.example.wassi.msgr.Messages.getRoundedCornerBitmap;


public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Messages> messagesList;
    public class MyMsgViewHolder extends RecyclerView.ViewHolder {
        public TextView message, date, sender;
        public ImageView user_picture;

        public MyMsgViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.msg);
            date = (TextView) view.findViewById(R.id.date);
            sender = (TextView) view.findViewById(R.id.sender);
            user_picture = (ImageView) view.findViewById(R.id.avatar);
        }
    }

    public class MyImgViewHolder extends RecyclerView.ViewHolder {
        public TextView date, sender, info;
        public ImageView img, user_picture;

        public MyImgViewHolder(View view) {
            super(view);
            info = (TextView) view.findViewById(R.id.imginfo);
            img = (ImageView) view.findViewById(R.id.img);
            date = (TextView) view.findViewById(R.id.date);
            sender = (TextView) view.findViewById(R.id.sender);
            user_picture = (ImageView) view.findViewById(R.id.avatar);
        }
    }

    public MessagesAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @Override
    public int getItemViewType(int position) {
        Messages message = messagesList.get(position);
        if (message.isImage())
            return 1;
        else return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == 0) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_row, parent, false);
            return new MyMsgViewHolder(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.img_row, parent, false);
            return new MyImgViewHolder(itemView);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Messages message = messagesList.get(position);
        switch (holder.getItemViewType()) {
            case 0:
                MyMsgViewHolder msgholder = (MyMsgViewHolder) holder;
                msgholder.sender.setText(message.getSender());
                msgholder.message.setText(message.getMessage());
                msgholder.date.setText(message.getDate());
                msgholder.user_picture.setImageBitmap(message.getAvatar());
                break;

            case 1:
                MyImgViewHolder imgholder = (MyImgViewHolder) holder;
                imgholder.sender.setText(message.getSender());
                imgholder.img.setImageBitmap(message.getImg());
                imgholder.date.setText(message.getDate());
                int size;
                Bitmap img = message.getImg();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    size = img.getAllocationByteCount();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    size = img.getByteCount();
                } else {
                    size = img.getRowBytes() * img.getHeight();
                }
                imgholder.info.setText(img.getHeight() + " x " + img.getWidth() + " : " + img.getDensity() + "Bytes");
                imgholder.user_picture.setImageBitmap(message.getAvatar());

                break;
        }
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}