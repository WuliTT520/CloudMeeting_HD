package com.zhihui.imeeting.cloudmeeting_hd.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhouwei.library.CustomPopWindow;
import com.zhihui.imeeting.cloudmeeting_hd.R;
import com.zhihui.imeeting.cloudmeeting_hd.controller.MyURL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity {

    private SharedPreferences sp;
    private int roomId;
    private String roomName;
    private Handler handler;
    private Message msg;

    private Calendar cal;
    private String current_time_year;
    private String current_time_month;
    private String current_time_day;
    private String current_time_hour;
    private String current_time_minute;
    private String current_time_sec;

    private int meetingId;
    private String topic;
    private String begin;
    private String over;
    private String status;

    Timer timer;

    TextView room_name_tv;
    TextView time_tv;
    TextView date_tv;
    TextView isworking;
    TextView meeting_name_tv;
    TextView meeting_time_tv;
    TextView button;
    LinearLayout bg;
    ImageView set;
    ImageView more;
    ImageView qrcode;


    CustomPopWindow mCustomPopWindow;
    String sessionId;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setListener();
    }
    public void init(){
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        room_name_tv=findViewById(R.id.room_name);
        time_tv=findViewById(R.id.time_tv);
        date_tv=findViewById(R.id.date_tv);
        isworking=findViewById(R.id.isworking);
        meeting_name_tv=findViewById(R.id.meeting_name);
        meeting_time_tv=findViewById(R.id.meeting_time);
        button=findViewById(R.id.button);
        bg=findViewById(R.id.bg);
        set=findViewById(R.id.set);
        more=findViewById(R.id.more);
        qrcode=findViewById(R.id.rqcode);

        roomId=sp.getInt("roomID",-1);
        roomName=sp.getString("roomName","会议室");
        room_name_tv.setText(roomName);
//        Toast.makeText(MainActivity.this,roomId+"",Toast.LENGTH_LONG).show();

        cal = Calendar.getInstance();
        current_time_year = String.valueOf(cal.get(Calendar.YEAR));
        current_time_month = String.format("%02d",cal.get(Calendar.MONTH)+1);
        current_time_day = String.format("%02d",cal.get(Calendar.DATE));

        if (cal.get(Calendar.AM_PM) == 0)

            current_time_hour = String.format("%02d",cal.get(Calendar.HOUR));
        else
            current_time_hour = String.format("%02d",cal.get(Calendar.HOUR)+12);
        current_time_minute = String.format("%02d",cal.get(Calendar.MINUTE));

        time_tv.setText(current_time_hour+":"+current_time_minute);
        date_tv.setText(current_time_year+"年"+current_time_month+"月"+current_time_day+"日");


        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 404:
                        Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                        break;
                    case 500:
                        Toast.makeText(MainActivity.this,"数据错误",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
//                        Toast.makeText(MainActivity.this,current_time_sec,Toast.LENGTH_SHORT).show();
                        time_tv.setText(current_time_hour+":"+current_time_minute);
                        date_tv.setText(current_time_year+"年"+current_time_month+"月"+current_time_day+"日");
                        break;
                    case 200:
                        if (status.equals("未开始")){
                            isworking.setText("下一场会议");
                            isworking.setTextColor(MainActivity.this.getResources().getColor(R.color.free));
                            meeting_name_tv.setText(topic);
                            meeting_time_tv.setText(begin.substring(10,16)+"~"+over.substring(10,16));
                            button.setText("等待会议开始");
                            button.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.free));
                            bg.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.free_bg));
                        }else {
                            isworking.setText("会议进行中");
                            isworking.setTextColor(MainActivity.this.getResources().getColor(R.color.work));
                            meeting_name_tv.setText(topic);
                            meeting_time_tv.setText(begin.substring(10,16)+"~"+over.substring(10,16));
                            button.setText("会议签到");
                            button.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.work));
                            bg.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.working_bg));
                        }
                        break;
                    case 201:
                        isworking.setText("当前没有会议信息");
                        isworking.setTextColor(MainActivity.this.getResources().getColor(R.color.free));
                        meeting_name_tv.setText("");
                        meeting_time_tv.setText("");
                        button.setText("");
                        button.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.touming));
                        bg.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.free_bg));
                        break;
                    case 202:
                        editor = sp.edit();
                        editor.putString("sessionID",sessionId);
                        editor.commit();
                        Intent intent=new Intent(MainActivity.this,ChooseRoomActivity.class);
                        startActivity(intent);
                        finish();

                        break;
                }
            }
        };

        getInfo();
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cal = Calendar.getInstance();
                current_time_year = String.valueOf(cal.get(Calendar.YEAR));
                current_time_month = String.format("%02d",cal.get(Calendar.MONTH)+1);
                current_time_day = String.format("%02d",cal.get(Calendar.DATE));

                if (cal.get(Calendar.AM_PM) == 0)

                    current_time_hour = String.format("%02d",cal.get(Calendar.HOUR));
                else
                    current_time_hour = String.format("%02d",cal.get(Calendar.HOUR)+12);
                current_time_minute = String.format("%02d",cal.get(Calendar.MINUTE));

                current_time_sec =String.valueOf(cal.get(Calendar.SECOND));

                msg=Message.obtain();
                msg.what=1;
                handler.sendMessage(msg);

                //如果为整分钟，则向服务器请求更新数据
                if (cal.get(Calendar.SECOND)==0){
                    getInfo();
                }


            }
        },1000,1000);
    }

    public void getInfo(){
        MyURL url=new MyURL();
        final OkHttpClient client = new OkHttpClient();
        RequestBody form=new FormBody.Builder()
                .add("reserverDate",current_time_year+"-"+current_time_month+"-"+current_time_day)
                .add("roomId",roomId+"")
                .build();
        final Request request = new Request.Builder()
                .addHeader("cookie", sp.getString("sessionID", ""))
                .url(url.oneRoomReserver())
                .post(form)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                msg=Message.obtain();
                msg.what=404;
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
//                                Log.w(TAG,result);
                    JSONObject data = new JSONObject(result);
                    boolean flag = data.getBoolean("status");
                    if (flag){
                        JSONArray info=data.getJSONArray("data");
                        if (info.length()>0){
                            for(int i=0;i<info.length();i++){
                                if (info.getJSONObject(i).getString("status").equals("已结束")){
                                    continue;
                                }else {
                                    meetingId=info.getJSONObject(i).getInt("id");
                                    topic=info.getJSONObject(i).getString("topic");
                                    begin=info.getJSONObject(i).getString("begin");
                                    over=info.getJSONObject(i).getString("over");
                                    status=info.getJSONObject(i).getString("status");
                                    msg=Message.obtain();
                                    msg.what=200;
                                    handler.sendMessage(msg);
                                    return;
                                }
                            }
//                            meetingId=info.getJSONObject(0).getInt("id");
//                            topic=info.getJSONObject(0).getString("topic");
//                            begin=info.getJSONObject(0).getString("begin");
//                            over=info.getJSONObject(0).getString("over");
//                            status=info.getJSONObject(0).getString("status");
                            msg=Message.obtain();
                            msg.what=201;
                            handler.sendMessage(msg);
                        }else {
                            //今天以后都没有会议
                            msg=Message.obtain();
                            msg.what=201;
                            handler.sendMessage(msg);
                        }
                    }else {
                        msg=Message.obtain();
                        msg.what=500;
                        handler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setListener(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button.getText().toString().equals("会议签到")){
                    Intent intent=new Intent(MainActivity.this,FaceIDActivity.class);
                    intent.putExtra("meetingId",meetingId);
                    startActivity(intent);
                }
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"设置",Toast.LENGTH_LONG).show();
                View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popwindow,null);
                //处理popWindow 显示内容
                handleLogic(contentView);
                //创建并显示popWindow
                mCustomPopWindow= new CustomPopWindow.PopupWindowBuilder(MainActivity.this)
                        .setView(contentView)
                        .create()
                        .showAsDropDown(set,-60,-40);

            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"详细信息",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(MainActivity.this,InfoActivity.class);
                startActivity(intent);
            }
        });
        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"显示二维码",Toast.LENGTH_LONG).show();
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                View dialogview = LayoutInflater.from(MainActivity.this).inflate(R.layout.show_qrcode, null);
                builder.setView(dialogview);
                AlertDialog dialog=builder.create();
                dialog.show();
                final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = 620;
                params.height = 620;
                dialog.getWindow().setAttributes(params);

//                dialog.show();
            }
        });
    }

    private void handleLogic(View contentView) {

        //设置弹出View中的点击事件
        contentView.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this,"重设会议室",Toast.LENGTH_LONG).show();
                mCustomPopWindow.dissmiss();
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
                View dialogview = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_dialog, null);


                builder.setView(dialogview);
                final AlertDialog dialog=builder.create();
                final EditText username=dialogview.findViewById(R.id.username);
                final EditText password=dialogview.findViewById(R.id.password);

                dialogview.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final OkHttpClient client = new OkHttpClient();
                        String user=username.getText().toString();
                        String pwd=password.getText().toString();
                        if (pwd.length()==0||user.length()==0){
                            Toast.makeText(MainActivity.this,"请输入信息",Toast.LENGTH_LONG).show();
                            return;
                        }
                        RequestBody form=new FormBody.Builder()
                                .add("username",user)
                                .add("password",pwd)
                                .build();
                        final Request request=new Request.Builder()
                                .url(new MyURL().mangerLogin())
                                .post(form)
                                .build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                msg=Message.obtain();
                                msg.what=404;
                                handler.sendMessage(msg);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    String result = response.body().string();
//                                    Log.w(TAG,result);
                                    JSONObject data = new JSONObject(result);
                                    boolean flag = data.getBoolean("status");
                                    if (flag){
                                        sessionId=response.header("Set-Cookie");
                                        dialog.dismiss();
                                        msg=Message.obtain();
                                        msg.what=202;
                                        handler.sendMessage(msg);
                                    }else {
                                        msg=Message.obtain();
                                        msg.what=500;
                                        handler.sendMessage(msg);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                dialogview.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }
}
