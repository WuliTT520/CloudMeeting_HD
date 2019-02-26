package com.zhihui.imeeting.cloudmeeting_hd.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.VersionInfo;
import com.zhihui.imeeting.cloudmeeting_hd.R;
import com.zhihui.imeeting.cloudmeeting_hd.common.Constants;
import com.zhihui.imeeting.cloudmeeting_hd.controller.MyURL;
import com.zhihui.imeeting.cloudmeeting_hd.helper.TCP;
import com.zhihui.imeeting.cloudmeeting_hd.util.camera.CameraHelper;
import com.zhihui.imeeting.cloudmeeting_hd.util.camera.CameraListener;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceIDActivity extends Activity {

    FaceEngine faceEngine = null;
    static int errorCode = -1;

    private static final String TAG = "FaceIDActivity";
    private CameraHelper cameraHelper;
    private Camera.Size previewSize;
    private int processMask = FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS;
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private TextureView previewView ;//相机预览显示控件

    private int meetingId;
    private Handler handler;
    private Message msg;
    AlertDialog.Builder builder;
    /**
     * 所需的所有权限信息
     */
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_id);

        meetingId=getIntent().getIntExtra("meetingId",-1);
        Toast.makeText(FaceIDActivity.this,"会议id为"+meetingId,Toast.LENGTH_LONG).show();

        previewView=findViewById(R.id.face);

        builder = new AlertDialog.Builder(FaceIDActivity.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
//                        builder.setTitle("输入群组名称");
        View dialogview = LayoutInflater.from(FaceIDActivity.this).inflate(R.layout.face_tip, null);
        builder.setView(dialogview);
        final AlertDialog dialog = builder.create();

        final TextView tip=dialogview.findViewById(R.id.tip_card);

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 200:
//                        Log.w(TAG,"运行到这了");
//                        Toast.makeText(FaceIDActivity.this,msg.getData().getString("message"),Toast.LENGTH_LONG).show();

                        tip.setText(msg.getData().getString("message"));
                        dialog.show();
                        TCP tcp=new TCP();
                        try {
                            tcp.post();
                            Toast.makeText(FaceIDActivity.this,"运行到这",Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                cameraHelper.start();
                            }
                        },2000);

                        break;
                    case 404:
                        Toast.makeText(FaceIDActivity.this,"网络错误",Toast.LENGTH_LONG).show();
                        break;
                    case 500:
                        Toast.makeText(FaceIDActivity.this,"数据错误",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        if(!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }

    /**
     * 检查是否授权
     * @param neededPermissions
     * @return
     */
    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
            } else {
                Log.i(TAG, "未授权权限");
            }
        }
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        //激活引擎
        int activeCode = faceEngine.active(this, Constants.ArcFace_APP_ID, Constants.ArcFace_SDK_KEY);

        /**
         * faceEngine.init()初始化引擎
         * @param context上下文对象
         * @param 视频模式检测
         * @param 人脸检测方向为多方向检测
         * @param 人脸相对于所在图片的长边的占比 [2, 16]
         * @param 引擎最多能检测出的人脸数 [1, 50]
         * @param 引擎功能:人脸检测、人脸识别、年龄检测、人脸三维角度检测、性别检测、活体检测
         */
        int errorCode = faceEngine.init(this.getApplicationContext(), FaceEngine.ASF_DETECT_MODE_VIDEO,
                FaceEngine.ASF_OP_0_HIGHER_EXT,
                16, 1,
                processMask);
        //获取版本信息
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        if (errorCode != ErrorInfo.MOK) {
            Log.i(TAG, "初始化引擎失败");
        } else {
            //deBug信息
            Log.i(TAG, "初始化引擎成功!  errorCode: " + errorCode + "  引擎版本号:" + versionInfo);
        }
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (errorCode == ErrorInfo.MOK) {
            errorCode = faceEngine.unInit();
            Log.i(TAG, "销毁引擎!  errorCode: " + errorCode);
        }
    }



    private byte[] faceFeatureData; //存储人脸特征值数据


    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isMirror);
                previewSize = camera.getParameters().getPreviewSize();
            }

            @Override
            public void onPreview(byte[] nv21, Camera camera) {
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                Log.i("code", "code:"+code);
                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    FaceFeature faceFeatures = new FaceFeature();
                    //从图片解析出人脸特征数据
                    int extractFaceFeatureCodes = faceEngine.extractFaceFeature(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeatures);
                    if(extractFaceFeatureCodes == ErrorInfo.MOK) {
                        faceFeatureData = faceFeatures.getFeatureData();
//                        System.out.print("特征值:");
//                        System.out.println(bytesToHex(faceFeatureData));
                        cameraHelper.stop();
                        qiandao(bytesToHex(faceFeatureData));

                        return;
                    }
                }else {
                    return;
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };
        cameraHelper = new CameraHelper.Builder()
                .metrics(metrics)
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static File getFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        File file = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            int x = 0;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void qiandao(String faceDetail){
//        Log.w(TAG,"签到函数");
        final OkHttpClient client = new OkHttpClient();
        RequestBody form=new FormBody.Builder()
                .add("meetingId",meetingId+"")
                .add("faceDetail",faceDetail)
                .build();
        final Request request=new Request.Builder()
                .url(new MyURL().compare())
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
                try{
                    String result = response.body().string();
                    Log.w(TAG,result);
                    JSONObject data = new JSONObject(result);
                    boolean flag = data.getBoolean("status");
                    if (flag){
                        msg=Message.obtain();
                        msg.what=200;
                        Bundle bundle = new Bundle();
                        bundle.putString("message",data.getString("message"));
                        msg.setData(bundle);
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
