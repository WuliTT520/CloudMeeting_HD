package com.zhihui.imeeting.cloudmeeting_hd.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhihui.imeeting.cloudmeeting_hd.R;
import com.zhihui.imeeting.cloudmeeting_hd.adapter.RoomListAdapter;
import com.zhihui.imeeting.cloudmeeting_hd.controller.MyURL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChooseRoomActivity extends Activity {

    private static final String TAG="ChooseRoomActivity";
    private RecyclerView room_list;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Handler handler;
    private Message msg;

    private ArrayList<Integer> room_id;
    private ArrayList<String> room_name;
    private ArrayList<String> room_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);
        init();
        getInfo();
    }
    public void init(){
        room_id=new ArrayList<>();
        room_name=new ArrayList<>();
        room_address=new ArrayList<>();
        room_list=findViewById(R.id.room_list);
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 404:
                        Toast.makeText(ChooseRoomActivity.this,"网络错误",Toast.LENGTH_LONG).show();
                        break;
                    case 500:
                        Toast.makeText(ChooseRoomActivity.this,"数据错误",Toast.LENGTH_LONG).show();
                        break;
                    case 200:
                        room_list.setLayoutManager(new LinearLayoutManager(ChooseRoomActivity.this));
                        final RoomListAdapter adapter=new RoomListAdapter(ChooseRoomActivity.this);
                        adapter.setRoom_id(room_id);
                        adapter.setRoom_name(room_name);
                        adapter.setRoom_address(room_address);
                        adapter.setOnItemClickLitener(new RoomListAdapter.OnItemClickLitener() {
                            @Override
                            public void onItemClick(View view, int position) {
//                                Toast.makeText(ChooseRoomActivity.this,room_id.get(position)+"",Toast.LENGTH_LONG).show();
                                editor = sp.edit();
                                editor.putInt("roomID",room_id.get(position));
                                editor.putString("roomName",room_name.get(position));
                                editor.commit();

                                Intent intent=new Intent(ChooseRoomActivity.this,MainActivity.class);

                                startActivity(intent);
                                finish();
                            }
                        });
                        room_list.setAdapter(adapter);
                        break;
                }
            }
        };
    }
    public void getInfo(){
        MyURL url=new MyURL();
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .addHeader("cookie", sp.getString("sessionID", ""))
                .url(url.selectAll())
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
                    Log.w(TAG,result);
                    JSONObject data = new JSONObject(result);
                    boolean flag = data.getBoolean("status");
                    if (flag){
                        JSONArray info=data.getJSONArray("data").getJSONArray(0);
                        for(int i=0;i<info.length();i++){
//                            Log.w(TAG,"id:"+info.getJSONObject(i).getInt("id")+"name:"+info.getJSONObject(i).getString("name")+"place:"+info.getJSONObject(i).getString("place"));
                            if(info.getJSONObject(i).getInt("availStatus")==1){
                                room_id.add(info.getJSONObject(i).getInt("id"));
                                room_name.add(info.getJSONObject(i).getString("name"));
                                room_address.add(info.getJSONObject(i).getString("place"));
                            }
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
