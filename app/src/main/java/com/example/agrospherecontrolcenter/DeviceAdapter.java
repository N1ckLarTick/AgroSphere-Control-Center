package com.example.agrospherecontrolcenter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.agrospherecontrolcenter.databinding.DeviceItemBinding;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<Device> devices = new ArrayList<>();
    public void setDevices(List<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    public List<Device> getDevices() {
        return devices;
    }

    interface OnItemDeviceClickListener{
        public void onDeviceClick(int position);
    }

    private OnItemDeviceClickListener onItemDeviceClickListener;

    public void setOnItemDeviceClickListener(OnItemDeviceClickListener onItemDeviceClickListener) {
        this.onItemDeviceClickListener = onItemDeviceClickListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item,parent,false);
        return new DeviceViewHolder(DeviceItemBinding.bind(view));
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.binding.deviceName.setText(String.valueOf(device.getName()));
        holder.binding.deviceType.setText(String.valueOf(device.getType()));
        holder.binding.devicePin.setText(String.valueOf(device.getPin()));
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemDeviceClickListener.onDeviceClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        DeviceItemBinding binding;
        public DeviceViewHolder(DeviceItemBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}