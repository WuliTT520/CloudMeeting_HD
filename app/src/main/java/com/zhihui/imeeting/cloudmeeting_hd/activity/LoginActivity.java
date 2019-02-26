package com.zhihui.imeeting.cloudmeeting_hd.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhihui.imeeting.cloudmeeting_hd.R;
import com.zhihui.imeeting.cloudmeeting_hd.controller.MyURL;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends Activity {
    private static final String TAG="LoginActivity";
    private ImageView logo;
    private EditText username;
    private EditText password;
    private Button login;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private Handler handler;
    private Message msg;

    String sessionId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        setListener();
    }
    public void init(){
        sp=this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        logo=findViewById(R.id.logo);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        Drawable pic1=getResources().getDrawable(R.drawable.username);
        Drawable pic2=getResources().getDrawable(R.drawable.password);
        pic1.setBounds(0,0,48,48);
        pic2.setBounds(0,0,48,48);
        username.setCompoundDrawables(pic1,null,null,null);
        password.setCompoundDrawables(pic2,null,null,null);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 200:
                        editor = sp.edit();
                        editor.putString("sessionID",sessionId);
                        editor.putBoolean("isLogin",true);
                        editor.commit();
                        Intent intent=new Intent(LoginActivity.this,ChooseRoomActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case 500:
                        Toast.makeText(LoginActivity.this,"用户名或密码错误",Toast.LENGTH_LONG).show();
                        break;
                    case 404:
                        Toast.makeText(LoginActivity.this,"网络错误",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };


        if (sp.getBoolean("isLogin",false)){
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    public void setListener(){
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,FaceIDActivity.class);
                intent.putExtra("meetingId",60);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final OkHttpClient client = new OkHttpClient();
                String user=username.getText().toString();
                String pwd=password.getText().toString();
                if (pwd.length()==0||user.length()==0){
                    Toast.makeText(LoginActivity.this,"请输入信息",Toast.LENGTH_LONG).show();
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
                            Log.w(TAG,result);
                            JSONObject data = new JSONObject(result);
                            boolean flag = data.getBoolean("status");
                            if (flag){
                                sessionId=response.header("Set-Cookie");
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
        });
    }

}
