package com.zhihui.imeeting.cloudmeeting_hd.myView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zhihui.imeeting.cloudmeeting_hd.adapter.RoomListAdapter;
import com.zhihui.imeeting.cloudmeeting_hd.util.MeetingInfo;
import com.zhihui.imeeting.cloudmeeting_hd.util.Rectangle;

import org.jetbrains.annotations.Nullable;

public class MyView extends View {

    private Paint mPaint;
    private OnItemClickLitener mOnItemClickLitener;

    private String[] times={"08:00","08:15","08:30","08:45",
                            "09:00","09:15","09:30","09:45",
                            "10:00","10:15","10:30","10:45",
                            "11:00","11:15","11:30","11:45",
                            "12:00","12:15","12:30","12:45",
                            "13:00","13:15","13:30","13:45",
                            "14:00","14:15","14:30","14:45",
                            "15:00","15:15","15:30","15:45",
                            "16:00","16:15","16:30","16:45",
                            "17:00","17:15","17:30","17:45",
                            "18:00"};


    private MeetingInfo[] meetingInfos={new MeetingInfo(1,"哈哈哈","10:00","12:00","已结束")};

    private Rectangle[] rectangles;

    public MyView(Context context) {
        super(context);
    }
    public MyView(Context context, @Nullable AttributeSet attributeSet){
        super(context,attributeSet);

    }

    public MeetingInfo[] getMeetingInfos() {
        return meetingInfos;
    }

    public void setMeetingInfos(MeetingInfo[] meetingInfos) {
        this.meetingInfos = meetingInfos;
    }

    public interface OnItemClickLitener{
        void onItemClick(int i);
    }
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);

        int height = measureHeight(heightMeasureSpec);

        setMeasuredDimension(width, times.length*100+100);
    }


    //根据xml的设定获取宽度

    private int measureWidth(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);

        int specSize = MeasureSpec.getSize(measureSpec);

        //wrap_content

        if (specMode == MeasureSpec.AT_MOST){

        }

        else if (specMode == MeasureSpec.EXACTLY){

        }


        return specSize;

    }

    //根据xml的设定获取高度

    private int measureHeight(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);

        int specSize = MeasureSpec.getSize(measureSpec);

        //wrap_content

        if (specMode == MeasureSpec.AT_MOST){

        }

        //fill_parent或者精确值

        else if (specMode == MeasureSpec.EXACTLY){

        }

        return specSize;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                for(int i=0;i<meetingInfos.length;i++){
                    if (rectangles[i].isInside(event.getX(),event.getY())){
                        Log.w("点击事件","点击的是"+i+"会议id是"+meetingInfos[i].getMeetingId());
                        if (mOnItemClickLitener != null) {
                            mOnItemClickLitener.onItemClick(i);
                        }
                    }
                }

                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        drawPoint(canvas);
        drawText(canvas);
//        drawRectangle(canvas);
        drawInfo(canvas);
    }

    /*绘制一条直线*/
    public void drawLine(Canvas canvas){
        mPaint=new Paint();
        //设置画板颜色
        canvas.drawColor(Color.TRANSPARENT);
        //设置画笔颜色
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        //绘制直线
        canvas.drawLine(200, 50, 200, times.length*100+50, mPaint);

        canvas.save();
    }

    public void drawPoint(Canvas canvas){
        mPaint=new Paint();
        //设置画板颜色
        canvas.drawColor(Color.TRANSPARENT);
        //设置画笔颜色
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.FILL);
        //绘制直线
        for(int i=0;i<times.length;i++){
            canvas.drawCircle(200, 100+i*100,5,  mPaint);
        }
//        canvas.drawCircle(200, 100,4,  mPaint);
        canvas.save();
    }

    public void drawText(Canvas canvas){
        mPaint=new Paint();
        //设置画板颜色
        canvas.drawColor(Color.TRANSPARENT);
        //设置画笔颜色
        mPaint.setColor(Color.GRAY);
        mPaint.setTextSize(30);
        //绘制时间
        for(int i=0;i<times.length;i++){
            canvas.drawText(times[i], 100,100+i*100+10,  mPaint);
        }
        canvas.save();
    }
    public void drawInfo(Canvas canvas){
        rectangles=new Rectangle[meetingInfos.length];
        for(int i=0;i<meetingInfos.length;i++){
            mPaint=new Paint();
//            if (meetingInfos[i].getState().equals("进行中")){
//                mPaint.setColor(Color.argb(70,237,90,101));
//            }else {
//                mPaint.setColor(Color.argb(70,55,55,55));
//            }
            mPaint.setColor(Color.argb(70,55,55,55));
            canvas.drawRect(250,100+getIndex(times,meetingInfos[i].getBegin())*100,
                    900,100+getIndex(times,meetingInfos[i].getOver())*100,mPaint);

            rectangles[i]=new Rectangle(250,100+getIndex(times,meetingInfos[i].getBegin())*100,
                    900,100+getIndex(times,meetingInfos[i].getOver())*100);

            mPaint.setColor(Color.rgb(230,230,230));
            mPaint.setTextSize(30);
            canvas.drawText(meetingInfos[i].getMeetingName(),300,160+getIndex(times,meetingInfos[i].getBegin())*100,mPaint);
            mPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(meetingInfos[i].getState(),850,160+getIndex(times,meetingInfos[i].getBegin())*100,mPaint);
        }
    }

    //查找该时间在时间表中的位置
    public static int getIndex(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] .equals(value) ) {
                return i;
            }
        }
        return -1;//如果未找到返回-1
    }

//    @Override
//    public void invalidate() {
////        super.invalidate();
//        this.onDraw();
//    }
}
