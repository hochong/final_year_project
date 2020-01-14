package com.example.fyp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

public class ConnectRobot_BluetoothListAdapter extends RecyclerView.Adapter<ConnectRobot_BluetoothListAdapter.MyViewHolder> {
    private final String[] mRobot_Bluetooth_List;
    private LayoutInflater mInflater;
    private final BluetoothConnectionRobotApp bcra;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        final ConnectRobot_BluetoothListAdapter mAdapter;
        private final BluetoothConnectionRobotApp bcra;
        public MyViewHolder(View v, ConnectRobot_BluetoothListAdapter adapter, BluetoothConnectionRobotApp bcra) {
            super(v);
            textView = v.findViewById(R.id.word);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
            this.bcra = bcra;
        }

        @Override
        public void onClick(View v) {
            int mPosition = getLayoutPosition();

            Log.i(TAG, "Connect to robot activity - bluetooth list adapter - " + this.mAdapter.mRobot_Bluetooth_List[mPosition] + " is clicked");
            this.bcra.connectToRobotSocket(mPosition);
            Intent i = new Intent();
            i.putExtra("bluetoothconnection", this.bcra);

        }
    }

    // constructor
    public ConnectRobot_BluetoothListAdapter(Context context, String[] mylist, BluetoothConnectionRobotApp bcra) {
        mInflater = LayoutInflater.from(context);
       this.mRobot_Bluetooth_List = mylist;
       this.bcra = bcra;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ConnectRobot_BluetoothListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View mItemView = mInflater.inflate(R.layout.bluetooth_recyclerview_item, parent, false);
        return new MyViewHolder(mItemView, this, this.bcra);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

       holder.textView.setText(mRobot_Bluetooth_List[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mRobot_Bluetooth_List.length;
    }
}
