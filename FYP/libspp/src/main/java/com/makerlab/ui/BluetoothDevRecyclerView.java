package com.makerlab.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makerlab.protocol.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BluetoothDevRecyclerView {

    public static class Adapter extends RecyclerView.Adapter<Adapter.BluetoothDeviceViewHolder> {
        private List<BluetoothDevice> mBluetoothDeviceList;
        private Map<String, BluetoothDevice> mlookUpMap;
        private LayoutInflater mInflater;
        private BluetoothDevListActivity activity;

        public Adapter(Context activity) {
            //
            mlookUpMap = new HashMap<>();
            mBluetoothDeviceList = new LinkedList<BluetoothDevice>();
            mInflater = LayoutInflater.from(activity);
            this.activity = (BluetoothDevListActivity) activity;
        }

        @NonNull
        @Override
        public BluetoothDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //mItemView is constraint layout
            View mItemView = mInflater.inflate(R.layout.devicelist_item, parent, false);

            //Log.e("BluetoothDevListAdapter",mItemView.getContext().toString());
            //Log.e("BluetoothDevListAdapter", "onCreateViewHolder");
            return new BluetoothDeviceViewHolder(mItemView);
        }

        @Override
        public void onBindViewHolder(@NonNull BluetoothDeviceViewHolder deviceViewHolder, int position) {
            BluetoothDevice device = mBluetoothDeviceList.get(position);
            deviceViewHolder.deviceName.setText(device.getName());
            deviceViewHolder.deviceAddr.setText(device.getAddress());
            deviceViewHolder.deviceType.setText("Classic");
            deviceViewHolder.deviceCount.setText(String.valueOf(position + 1));
        }

        public void clearBluetoothDeviceList() {
            mBluetoothDeviceList.clear();
            mlookUpMap.clear();
            notifyDataSetChanged();
        }

        synchronized public int addBluetoothDevice(BluetoothDevice remoteDevice) {
            int size = mBluetoothDeviceList.size();
            String name = remoteDevice.getName();
            if (name == null || name.trim().length() == 0) return size;
            if (mlookUpMap.get(remoteDevice.getAddress()) == null) {
                mlookUpMap.put(remoteDevice.getAddress(), remoteDevice);
                mBluetoothDeviceList.add(remoteDevice);
                size = mBluetoothDeviceList.size();
                notifyItemInserted(size);
            }
            return size;
        }

        public BluetoothDevice getBluetoothDevice(String address) {
            return mlookUpMap.get(address);
        }

        public List<BluetoothDevice> getmBluetoothDeviceList() {
            return mBluetoothDeviceList;
        }

        // number of BluetoothDeviceViewHolder respective to data size
        @Override
        public int getItemCount() {
            return mBluetoothDeviceList.size();
        }

        //
        public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName = null;
            TextView deviceAddr = null;
            TextView deviceCount = null;
            TextView deviceType = null;
            //int position;

            public BluetoothDeviceViewHolder(View containerView) {
                super(containerView);
                deviceName = containerView.findViewById(R.id.deviceName);
                deviceAddr = containerView.findViewById(R.id.deviceAddr);
                deviceCount = containerView.findViewById(R.id.count);
                deviceType = containerView.findViewById(R.id.deviceType);
                containerView.setOnClickListener(activity);
                //position = getLayoutPosition();
                // deviceCount.setText(position);
            }
        }
    }
}
