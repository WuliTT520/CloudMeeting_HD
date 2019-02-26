package com.zhihui.imeeting.cloudmeeting_hd.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhihui.imeeting.cloudmeeting_hd.R;

import java.util.ArrayList;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Integer> room_id;
    private ArrayList<String> room_name;
    private ArrayList<String> room_address;

    private OnItemClickLitener mOnItemClickLitener;


    public void setContext(Context context) {
        this.context = context;
    }

    public void setRoom_id(ArrayList<Integer> room_id) {
        this.room_id = room_id;
    }

    public void setRoom_name(ArrayList<String> room_name) {
        this.room_name = room_name;
    }

    public void setRoom_address(ArrayList<String> room_address) {
        this.room_address = room_address;
    }

    public interface OnItemClickLitener{
        void onItemClick(View view, int position);
    }
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public RoomListAdapter(Context context) {
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        MyViewHolder viewHolder=new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.room_list_item, viewGroup,false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder,final int i) {
        if(i==0){
            myViewHolder.order_tv.setText("序号");
            myViewHolder.name_tv.setText("名称");
            myViewHolder.address_tv.setText("地址");
        }else {
            myViewHolder.order_tv.setText((i)+"");
            myViewHolder.name_tv.setText(room_name.get(i-1));
            myViewHolder.address_tv.setText(room_address.get(i-1));
            if (mOnItemClickLitener != null) {
                myViewHolder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickLitener.onItemClick(view, i-1);
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return room_id.size()+1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout root;
        TextView order_tv;
        TextView name_tv;
        TextView address_tv;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            root=itemView.findViewById(R.id.root);
            order_tv=itemView.findViewById(R.id.order);
            name_tv=itemView.findViewById(R.id.name);
            address_tv=itemView.findViewById(R.id.address);
        }
    }
}
