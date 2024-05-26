package com.example.agrospherecontrolcenter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.example.agrospherecontrolcenter.databinding.ActivityAddNewDeviceBinding;

import java.util.ArrayList;
import java.util.List;

public class AddNewDeviceActivity extends AppCompatActivity {
    private DeviceDatabase repository;
    private final static String EXTRA_ID = "id";
    private final static String EXTRA_NAME="name";
    private final static String EXTRA_TYPE="type";
    private final static String EXTRA_PIN="pin";
    private String name;
    private String type;
    private String pin;
    private String id;
    private Handler handler;
    private ActivityAddNewDeviceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNewDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handler = new Handler(Looper.getMainLooper());
        repository = DeviceDatabase.newInstance(getApplication());

        name = getIntent().getStringExtra(EXTRA_NAME);
        type = getIntent().getStringExtra(EXTRA_TYPE);
        pin = getIntent().getStringExtra(EXTRA_PIN);
        id = getIntent().getStringExtra(EXTRA_ID);

        binding.editTextName.setText(name);
        binding.editTextType.setText(type);
        binding.editTextPin.setText(pin);

        binding.btnSaveDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editTextName.getText().toString();
                String type = binding.editTextType.getText().toString();
                String pin = binding.editTextPin.getText().toString();

                Device device = new Device(name,type,pin);
                if (id.equals("0")) {
                    Log.d("LOG","Create Product"+id+" "+device);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            repository.deviceDao().add(device);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                    }).start();
                } else {
                    Log.d("LOG","UPDATE Product"+id +" "+ device);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            repository.deviceDao().update(device.getName(),
                                    device.getType(),
                                    device.getPin(),
                                    Integer.parseInt(id));
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

    public static Intent newIntent(Context context,String id, String name, String type, String pin) {
        Intent intent = new Intent(context,AddNewDeviceActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_NAME,name);
        intent.putExtra(EXTRA_TYPE,type);
        intent.putExtra(EXTRA_PIN,pin);
        return intent;
    }
}