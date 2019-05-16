package com.example.wassi.msgr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private ImageButton btnCapturePicture,colorbtn,btnStickers;


    /*
                                * TODO : -Taking a pic,request pic,color changer ,Realtime draw board,Seen ,Connected notification
                                **/

    public Socket mSocket;
    public static List<Messages> msgList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MessagesAdapter mAdapter;
    private String txtUname = null;
    private SharedPreferences.Editor editor = null;
    private View Ostatus,Istatus;
    private TextView type;
    private boolean forground=false;
    private int c1,c2;
    private SeekBar slider;
    private GradientDrawable bubble = null;



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
        stopService(myService);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        editor = settings.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        txtUname = settings.getString("Username", "");
        if (txtUname.equalsIgnoreCase("") || txtUname.equals(null)) {
            TextView f = findViewById(R.id.textView5);
            f.setVisibility(View.VISIBLE);
            ImageView b = findViewById(R.id.boy);
            ImageView g = findViewById(R.id.gurl);
            b.setVisibility(View.VISIBLE);
            g.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SecondAccount();
                }
            });
            g.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirstAccount();
                }
            });

        } else {
            final TextView reciever = findViewById(R.id.reciever);
            reciever.setText(txtUname);
            TextView f = findViewById(R.id.textView5);
            f.setVisibility(View.GONE);
            ImageView b = findViewById(R.id.boy);
            ImageView g = findViewById(R.id.gurl);
            b.setVisibility(View.GONE);
            g.setVisibility(View.GONE);

            ChatApplication app = new ChatApplication();
            mSocket = app.getSocket();
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on("Connected", onConnected);
            mSocket.on("Disconnected", onDisconnected);
            mSocket.on("Initialize", onInitialize);
            mSocket.on("newMessage", onRecieve);
            mSocket.on("Typing", onTyping);
            mSocket.on("notTyping", onNotTyping);
            mSocket.on("Color", onColorize);
            mSocket.connect();

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mAdapter = new MessagesAdapter(msgList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
            type = findViewById(R.id.type);
            Ostatus = findViewById(R.id.offline);
            Istatus = findViewById(R.id.online);
            slider = findViewById(R.id.colorslide);
            colorbtn = findViewById(R.id.colorbtn);
            btnCapturePicture = (ImageButton) findViewById(R.id.cambutton);
            btnStickers= (ImageButton) findViewById(R.id.stickerbutton);




            c1 = Color.parseColor("#481891");
            c2 = Color.parseColor("#6030ff");
            float init[] = new float[3];
            Color.colorToHSV(c1, init);
            slider.setProgress((int) init[0]);
            bubble = (GradientDrawable) this.getResources().getDrawable(R.drawable.bubble_rectangle);

            initListeners();
        }
    }

    public void initListeners(){
        colorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorbtn.setVisibility(View.GONE);
                slider.setVisibility(View.VISIBLE);
            }
        });

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int a;
            boolean t;

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                t = true;
                mAdapter.notifyItemRangeChanged(msgList.size() - 7, 7);
                mAdapter.notifyDataSetChanged();
                mSocket.emit("color", a);
                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (t) {
                            colorbtn.setVisibility(View.VISIBLE);
                            slider.setVisibility(View.GONE);
                            t = false;
                        }
                    }
                }, 1500);


            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                t = false;
                a = arg1;
                float c10[] = new float[3];
                float c20[] = new float[3];
                Color.colorToHSV(c1, c10);
                Color.colorToHSV(c2, c20);
                c10[0] = arg1;
                c20[0] = arg1;
                c1 = Color.HSVToColor(c10);
                c2 = Color.HSVToColor(c20);
                bubble.setColors(new int[]{c1, c2});
                findViewById(R.id.Toolbar).setBackground(bubble);
                findViewById(R.id.Toolbar).postInvalidate();
                findViewById(R.id.layout_chatbox).setBackground(bubble);
                findViewById(R.id.layout_chatbox).postInvalidate();


            }
        });

        EditText t = findViewById(R.id.textmsg);
        t.addTextChangedListener(new TextWatcher() {
            private int trigger = 0;
            private boolean isTyping;

            @Override
            public void afterTextChanged(Editable mEdit) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                trigger++;
                if (trigger <= 1) {
                    mSocket.emit("Typing");
                    Log.e("TYPE", "TYPING ......................");
                }
                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        trigger--;
                        if (trigger <= 1) {
                            mSocket.emit("notTyping");
                            Log.e("TYPE", "STOP ......................");
                        }
                    }
                }, 1500);

            }

        });


        btnCapturePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnStickers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StickersDialog sd = new StickersDialog(MainActivity.this);
                sd.show();
            }
        });
    }




@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
@Override
public void onResume(){
    super.onResume();
   // Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
   // stopService(myService);
    forground=true;

}
    @Override
    public void onPause() {
        super.onPause();
        forground=false;
       // Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
        //startService(myService);
        //recyclerView.removeAllViews();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
      finish();
      stopService(myService);
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            sendImagesData(BitToString(imageBitmap));
        } else if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                //initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("CONNECTION ","CONNECTION SUCCESS");
            mSocket.emit("In",txtUname);
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("CONNECTION ", "CONNECTION ERROR");

                }
            });
        }
    };
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
                    stopService(myService);

                }
            });
        }
    };
    public Emitter.Listener onDisconnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String u= (String)args[0];
                    if(!u.equals(txtUname)) {
                        Ostatus.setVisibility(View.VISIBLE);
                        Istatus.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    public Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String u= (String)args[0];
                        if(!u.equals(txtUname)){
                            Istatus.setVisibility(View.VISIBLE);
                            Ostatus.setVisibility(View.GONE);
                        }
                }
            });
        }
    };
    public Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("TYPE ", " entrain d'ecrire .......... +");
                     type.setVisibility(View.VISIBLE);
                }
            });
        }
    };
    public Emitter.Listener onNotTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("TYPE ", " STOP  .......... +");
                     type.setVisibility(View.INVISIBLE);

                }
            });
        }
    };
    public Emitter.Listener onColorize = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                int arg1=(int)args[0];
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    float c10[]=new float[3];
                    float c20[]=new float[3];
                    Color.colorToHSV(c1,c10);
                    Color.colorToHSV(c2,c20);
                    c10[0]=arg1;
                    c20[0]=arg1;
                    c1=Color.HSVToColor(c10);
                    c2=Color.HSVToColor(c20);
                    bubble.setColors(new int[]{c1,c2});
                    findViewById(R.id.Toolbar).setBackground(bubble);
                    findViewById(R.id.Toolbar).postInvalidate();
                    findViewById(R.id.layout_chatbox).setBackground(bubble);
                    findViewById(R.id.layout_chatbox).postInvalidate();
                    mAdapter.notifyItemRangeChanged(msgList.size()-7,7);
                    mAdapter.notifyDataSetChanged();
                    slider.setProgress(arg1);

                }
            });
        }
    };
    public Emitter.Listener onInitialize = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String avatar = null;
                    JSONArray data = (JSONArray) args[0];
                    JSONObject m = null;
                   /* String status =(String)args[0];
                    View off=findViewById(R.id.offline);
                    TextView last=findViewById(R.id.last_active);
                    View on=findViewById(R.id.online);
                    if(status=="true"){off.setVisibility(View.GONE);on.setVisibility(View.VISIBLE);}
                    else {off.setVisibility(View.GONE);on.setVisibility(View.VISIBLE);
                            last.setText(status);}*/

                        for (int i = 0; i < data.length(); i++) {
                            try {
                                m = data.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (m.getString("user").equals("")) {
                                    avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.first));
                                } else {
                                    avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.second));

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                msgList.add(new Messages(m.getString("user"), m.getString("message"), m.getString("date"),m.getBoolean("isImg"), avatar));
                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                            mAdapter.notifyDataSetChanged();

                        }
                        recyclerView.scrollToPosition(msgList.size()-1);

                }
            });
        }
    };
    public Emitter.Listener onRecieve = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String user, msg, date;
                    Boolean isImg;
                    try {

                        user = data.getString("user");
                        msg = data.getString("message");
                        date = data.getString("date");
                        isImg = data.getBoolean("isImg");
                    } catch (JSONException e) {
                        return;
                    }
                    String avatar;
                    if (user.equals("h")) {
                        avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.first));
                    } else {
                        avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.second));

                    }
                    Messages M = new Messages(user, msg, date, isImg, avatar);
                   if(!forground) {
                       Intent myService = new Intent(MainActivity.this, FloatingViewService.class);
                       startService(myService);
                       if (isImg)
                           showNotification(M.getSender(), "Sent you an image ..");
                       else
                           showNotification(M.getSender(), M.getMessage());
                   } msgList.add(M);
                       mAdapter.notifyDataSetChanged();
                       recyclerView.smoothScrollToPosition(msgList.size() - 1);

                }
            });
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //___________________________________________________________________________________________________
    private String grabTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public Bitmap StringToBitmap(String img){
        byte[] b = Base64.decode(img,Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
        return bmp;

    }
    private String BitToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private void sendImagesData(String image) {
        String avatar;
        if (txtUname.equals("")) {
            avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.first));
        } else {
            avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.second));

        }
        Messages msg = new Messages(txtUname, image, grabTime(), true, avatar);
        msgList.add(msg);
        mAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(msgList.size() - 1);
        mSocket.emit("newMessage", txtUname, image, grabTime(), true);
    }

    private void sendMessagesData(String message) {
        if (!message.equalsIgnoreCase("")) {
            String avatar;
            if (txtUname.equals("")) {
                avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.first));
            } else {
                avatar = BitToString(BitmapFactory.decodeResource(getResources(), R.drawable.second));

            }
            Messages msg = new Messages(txtUname, message, grabTime(), false, avatar);
            msgList.add(msg);
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(msgList.size() - 1);
            mSocket.emit("newMessage", txtUname, message, grabTime(), false);
        }
    }

    public void onClickBtn(View v) {
        TextView t = findViewById(R.id.textmsg);

        sendMessagesData(t.getText().toString());
        t.setText("");
    }

    public void FirstAccount() {
        txtUname = "";
        editor.putString("Username", txtUname);
        editor.apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void SecondAccount() {
        txtUname = "Wassim";
        editor.putString("Username", txtUname);
        editor.apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void showNotification(String Title, String Text) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(Title)
              //  .setColor(Title.equals("")? Color.MAGENTA:Color.BLUE)
                .setSmallIcon(R.drawable.notif)
                .setContentTitle(Title)
                .setContentText(Text)
                .setContentIntent(pi)
                .setSound(alarmSound)
                .setAutoCancel(true).setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

}