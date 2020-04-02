package com.example.fyp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


import static android.content.ContentValues.TAG;

public class fyp001_ConnectRobot_BluetoothListAdapter extends RecyclerView.Adapter<fyp001_ConnectRobot_BluetoothListAdapter.MyViewHolder> {
    private final ArrayList<String> mRobot_Bluetooth_List;
    private LayoutInflater mInflater;
    private final fyp001_BluetoothConnectionRobotApp bcra;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        final fyp001_ConnectRobot_BluetoothListAdapter mAdapter;

        public MyViewHolder(View v, fyp001_ConnectRobot_BluetoothListAdapter adapter) {
            super(v);
            textView = v.findViewById(R.id.word);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();

            Log.i(TAG, "Connect to robot activity - bluetooth list adapter - " + this.mAdapter.mRobot_Bluetooth_List.get(mPosition) + " is clicked");
            fyp001_ConnectRobot.connectToRobotSocket(mPosition);
        }

    }

    // constructor
    public fyp001_ConnectRobot_BluetoothListAdapter(Context context, ArrayList<String> mylist, fyp001_BluetoothConnectionRobotApp bcra) {
        mInflater = LayoutInflater.from(context);
       this.mRobot_Bluetooth_List = mylist;
       this.bcra = bcra;
    }

    public void updateData(ArrayList<String> list) {
        mRobot_Bluetooth_List.clear();
        mRobot_Bluetooth_List.addAll(list);
        notifyDataSetChanged();
    }
    // Create new views (invoked by the layout manager)
    @Override
    public fyp001_ConnectRobot_BluetoothListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View mItemView = mInflater.inflate(R.layout.fyp001_bluetooth_recyclerview_item, parent, false);
        return new MyViewHolder(mItemView, this);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
       if (mRobot_Bluetooth_List != null){
            holder.textView.setText(mRobot_Bluetooth_List.get(position));
       }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mRobot_Bluetooth_List == null){
            return 0;
        }
        return mRobot_Bluetooth_List.size();
    }
}
