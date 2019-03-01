package com.zhihui.imeeting.cloudmeeting_hd.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhihui.imeeting.cloudmeeting_hd.R;
import com.zhihui.imeeting.cloudmeeting_hd.controller.MyURL;
import com.zhihui.imeeting.cloudmeeting_hd.myView.MyView;
import com.zhihui.imeeting.cloudmeeting_hd.util.MeetingInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoActivity extends Activity {
    private ImageView back;
    private MyView myView;
    private LinearLayout bg;
    private TextView room_name;
    private TextView tip;
    private ScrollView info_view;
    private TextView name_tv;
    private TextView content_tv;
    private TextView begin_tv;
    private TextView over_tv;
    private TextView prepareTime_tv;
    private TextView peopleName_tv;
    private TextView phone_tv;
    private TextView state_tv;


    private SharedPreferences sp;
    private Handler handler;
    private Message msg;

    private String time;
    private int roomId;
    private String roomName;
    private boolean iswork;

    private MeetingInfo[] meetingInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        init();
        getInfo();
        setListener();
    }

    public void init(){
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        time=getIntent().getStringExtra("time");
        iswork=getIntent().getBooleanExtra("iswork",false);
        roomId=sp.getInt("roomID",-1);
        roomName=sp.getString("roomName","会议室");

        back=findViewById(R.id.back);
        myView=findViewById(R.id.myview);
        room_name=findViewById(R.id.room_name);
        room_name.setText(roomName);
        bg=findViewById(R.id.bg);
        tip=findViewById(R.id.tip);
        info_view=findViewById(R.id.info);
        name_tv=findViewById(R.id.name_tv);
        content_tv=findViewById(R.id.content_tv);
        begin_tv=findViewById(R.id.begin_tv);
        over_tv=findViewById(R.id.over_tv);
        prepareTime_tv=findViewById(R.id.prepareTime_tv);
        peopleName_tv=findViewById(R.id.peopleName_tv);
        phone_tv=findViewById(R.id.phone_tv);
        state_tv=findViewById(R.id.state_tv);

        info_view.setVisibility(View.GONE);
        if (iswork){
//            Toast.makeText(InfoActivity.this,"会议室工作",Toast.LENGTH_LONG).show();
            bg.setBackground(InfoActivity.this.getResources().getDrawable(R.drawable.working_bg));
        }else {
//            Toast.makeText(InfoActivity.this,"会议室空闲",Toast.LENGTH_LONG).show();
            bg.setBackground(InfoActivity.this.getResources().getDrawable(R.drawable.free_bg));
        }

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
                switch (msg.what){
                    case 200:
//                        Toast.makeText(InfoActivity.this,meetingInfos[0].getBegin(),Toast.LENGTH_LONG).show();
//                        Log.w("时间",meetingInfos[0].getBegin());
                        myView.setMeetingInfos(meetingInfos);
                        myView.invalidate();
//                        myView.forceLayout();
//
//                        myView.requestLayout();

//                        Toast.makeText(InfoActivity.this,meetingInfos[0].getBegin(),Toast.LENGTH_LONG).show();
                        break;
                    case 404:
                        Toast.makeText(InfoActivity.this,"网络错误",Toast.LENGTH_LONG).show();
                        break;
                    case 500:
                        Toast.makeText(InfoActivity.this,"数据错误",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

    }
    public void setListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        myView.setOnItemClickLitener(new MyView.OnItemClickLitener() {
            @Override
            public void onItemClick(int i) {
//                Toast.makeText(InfoActivity.this,meetingInfos[i].getMeetingId()+"",Toast.LENGTH_LONG).show();
                tip.setVisibility(View.GONE);
                name_tv.setText(meetingInfos[i].getMeetingName());

                content_tv.setText(meetingInfos[i].getContent());
                begin_tv.setText(meetingInfos[i].getBegin());
                over_tv.setText(meetingInfos[i].getOver());
                prepareTime_tv.setText(meetingInfos[i].getPrepareTime()+"分钟");
                peopleName_tv.setText(meetingInfos[i].getPeopleName());
                phone_tv.setText(meetingInfos[i].getPhone());

                state_tv.setText(meetingInfos[i].getState());
                if (meetingInfos[i].getState().equals("进行中")){
                    state_tv.setTextColor(getResources().getColor(R.color.work));
                }else if (meetingInfos[i].getState().equals("未开始")){
                    state_tv.setTextColor(getResources().getColor(R.color.free));
                }
                info_view.setVisibility(View.VISIBLE);

            }
        });
    }
    public void getInfo(){
        MyURL url=new MyURL();
        final OkHttpClient client = new OkHttpClient();
        RequestBody form=new FormBody.Builder()
                .add("reserverDate",time)
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
                        meetingInfos=new MeetingInfo[info.length()];
                        for(int i=0;i<info.length();i++){
                            meetingInfos[i]=new MeetingInfo(info.getJSONObject(i).getInt("id"),
                                    info.getJSONObject(i).getString("topic"),
                                    info.getJSONObject(i).getString("begin").substring(11,16),
                                    info.getJSONObject(i).getString("over").substring(11,16),
                                    info.getJSONObject(i).getString("status"));
                            meetingInfos[i].setContent(info.getJSONObject(i).getString("content"));
                            meetingInfos[i].setPeopleName(info.getJSONObject(i).getString("peopleName"));
                            meetingInfos[i].setPhone(info.getJSONObject(i).getString("phone"));
                            meetingInfos[i].setPrepareTime(info.getJSONObject(i).getInt("prepareTime"));
                        }
                        msg=Message.obtain();
                        msg.what=200;
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
}
