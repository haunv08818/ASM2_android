package com.example.demofinal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.demofinal.databinding.FragmentMessageBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;


public class MessageFragment extends Fragment {

    private static final String TAG = MessageFragment.class.getName();
    private final Calendar CAL = Calendar.getInstance();
    private final Random rd = new Random();
    private FragmentMessageBinding binding;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        binding = FragmentMessageBinding.bind(view);
        initViews();
        return view;
    }

    private void initViews() {
        binding.btSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setupAlarm();
                }
            }
        });
        binding.tvDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initDate();
            }
        });
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{
                    Manifest.permission.SEND_SMS
            }, 101);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupAlarm(){
        if (binding.etInputMessage.getText().toString().isEmpty()
                || binding.etInputPhoneNumber.getText().toString().isEmpty()) {
            Toast.makeText(mContext, "Please input + message first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.tvDateTime.getText().toString().isEmpty()){
            Toast.makeText(mContext, "Please set time first!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Khoi tao doi tuong PendingService lam nhiem vu thuc thi tac vu ngam
        ComponentName serviceComponent = new ComponentName(mContext, PendingService.class);
        //Tao mot lenh thuc thi
        JobInfo.Builder builder = new JobInfo.Builder(rd.nextInt(Integer.MAX_VALUE), serviceComponent);
        //Quy dinh thoi gian tre
        builder.setMinimumLatency(1000);
        //Quy dinh thoi gian tre toi da
        builder.setOverrideDeadline(2 * 1000);
        builder.setRequiresCharging(false);

        //Tao doi tuong gui thong so
        PersistableBundle bundle = new PersistableBundle();
        //Loai tac vu nhan tin
        bundle.putString("TYPE", PendingService.TYPE_SMS);
        //Thong tin nguoi gui
        bundle.putString("SENDER", binding.etInputPhoneNumber.getText().toString());
        //Thong tin tin nhan di
        bundle.putString("MSG", binding.etInputMessage.getText().toString());
        //Thoi gian gui
        bundle.putLong("TIME", (long) binding.tvDateTime.getTag());
        builder.setExtras(bundle);

        //Khoi tao JobScheduler
        JobScheduler jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
        Toast.makeText(mContext, "A message will be sent sometime", Toast.LENGTH_SHORT).show();
    }

    private void initDate(){
        new DatePickerDialog(mContext, (view, year, month, dayOfMonth) -> {
            CAL.set(Calendar.YEAR, year);
            CAL.set(Calendar.MONTH, month);
            CAL.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            initTime();
        }, CAL.get(Calendar.HOUR_OF_DAY), CAL.get(Calendar.MONTH), CAL.get(Calendar.YEAR)).show();
    }
    private void initTime(){
        new TimePickerDialog(mContext, (view, hourOfDay, minute) -> {
            CAL.set(Calendar.HOUR_OF_DAY, hourOfDay);
            CAL.set(Calendar.MINUTE, minute);
            updateLabel();
        }, CAL.get(Calendar.HOUR_OF_DAY), CAL.get(Calendar.HOUR_OF_DAY), true).show();
    }
    private void updateLabel(){
        Log.i(TAG, "updateLaabel...");
        String myFormat = "MM/dd/yy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.tvDateTime.setText(sdf.format(CAL.getTime()));
        binding.tvDateTime.setTag(CAL.getTimeInMillis());
    }
}