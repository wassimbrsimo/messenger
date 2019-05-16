package com.example.wassi.msgr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import android.widget.ImageView;

public class Messages {
    private String sender;
    private String message;
    private String date;
    private Boolean isImg;
    private String avatar;

    public void setImg(Boolean img) {
        isImg = img;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private Color color;

    public Bitmap getAvatar() {
        if(avatar!=null) {
            Bitmap av=StringToBitmap(avatar);
            av=Bitmap.createScaledBitmap(av, av.getWidth()/4, av.getHeight()/4, false);
            return getRoundedCornerBitmap(av, av.getWidth() / 2);
        }else return null;
    }


    public Bitmap getImg(){
        Bitmap bmp =StringToBitmap(message);
        bmp=Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false);
            return  getRoundedCornerBitmap(bmp,1);
    }
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public Boolean isImage() {
        return isImg;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(Boolean image) {
        isImg = image;
    }
    public Messages(String s,String m,String d,boolean isImage,String avatar){
        this.sender=s;
        this.message=m;
        this.date=d;
        this.isImg=isImage;
        this.avatar=avatar;
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    public Bitmap StringToBitmap(String img){
        byte[] b = Base64.decode(img,Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
        return  getRoundedCornerBitmap(bmp,bmp.getHeight()/10);

    }
}
