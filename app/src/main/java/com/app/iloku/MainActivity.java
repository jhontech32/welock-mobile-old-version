package com.app.iloku;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    View view_01, view_02, view_03, view_04;
    Button btn_01, btn_02, btn_03, btn_04, btn_05, btn_06, btn_07, btn_08, btn_09, btn_00, btn_clear;

    ArrayList<String> numbers_list = new ArrayList<>();

    String passCode = "";
    String resetFactoryCode = "";

    String num_01, num_02, num_03, num_04;

    public static final int RESULT_ENABLE = 11;
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;
    private DeviceAdminReceiver admin;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startService(new Intent (MainActivity.this, SettingsActivity.class));

        // Check action screen
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        MyBroadcastReceiver mReceiver = new MyBroadcastReceiver ((Runnable) this);
//        registerReceiver(mReceiver, filter);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        // Execute All Service
        //  Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        //  startService(serviceIntent);
        Intent serviceStartForegroundIntent = new Intent(this, MyForegroundService.class);
        startForegroundService(serviceStartForegroundIntent);
        if(!foregroundServiceRunning()) {
            Intent serviceForegroundIntent = new Intent(this, MyForegroundService.class);
            startForegroundService(serviceForegroundIntent);
        }

        // Intent serviceStartFloathingIntent = new Intent(this, FloatingViewService.class);
        // startService(serviceStartFloathingIntent);
//        getPermissions();
//        devicePolicyManager.setLockTaskPackages("com.app.iloku");
//        int appPinningMode = activityManager.getLockTaskModeState();
//        if( appPinningMode == ActivityManager.LOCK_TASK_MODE_PINNED ) {
//
//            startLockTask();
////            stopLockTask();
////            goToHome();
////            clearTopNumber();
//        }
////        startLockTask();


        initializeComponents();

        startLockTask();

        // Add calendar date
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        TextView textViewDate = findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);

        // UI Overlay Custom
        View overlay = findViewById(R.id.passcode_view);
        overlay.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Default Code not Setting yet
        if(getPassCode().length() == 0) {
            Intent settingsPage = new Intent(this, SettingsActivity.class);
            startActivity(settingsPage);
            Toast.makeText(this, "Menuju ke pengaturan.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLockTask();
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.activity_main, menu);
        return true;
    }

    private void initializeComponents() {
        view_01 = findViewById(R.id.view_01);
        view_02 = findViewById(R.id.view_02);
        view_03 = findViewById(R.id.view_03);
        view_04 = findViewById(R.id.view_04);

        btn_01 = findViewById(R.id.btn_01);
        btn_02 = findViewById(R.id.btn_02);
        btn_03 = findViewById(R.id.btn_03);
        btn_04 = findViewById(R.id.btn_04);
        btn_05 = findViewById(R.id.btn_05);
        btn_06 = findViewById(R.id.btn_06);
        btn_07 = findViewById(R.id.btn_07);
        btn_08 = findViewById(R.id.btn_08);
        btn_09 = findViewById(R.id.btn_09);
        btn_00 = findViewById(R.id.btn_00);
        btn_clear = findViewById(R.id.btn_clear);

        btn_01.setOnClickListener(this);
        btn_02.setOnClickListener(this);
        btn_03.setOnClickListener(this);
        btn_04.setOnClickListener(this);
        btn_05.setOnClickListener(this);
        btn_06.setOnClickListener(this);
        btn_07.setOnClickListener(this);
        btn_08.setOnClickListener(this);
        btn_09.setOnClickListener(this);
        btn_00.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_01:
                doVibrateApp(100);
                numbers_list.add("1");
                passNumber(numbers_list);
                break;
            case R.id.btn_02:
                doVibrateApp(100);
                numbers_list.add("2");
                passNumber(numbers_list);
                break;
            case R.id.btn_03:
                doVibrateApp(100);
                numbers_list.add("3");
                passNumber(numbers_list);
                break;
            case R.id.btn_04:
                doVibrateApp(100);
                numbers_list.add("4");
                passNumber(numbers_list);
                break;
            case R.id.btn_05:
                doVibrateApp(100);
                numbers_list.add("5");
                passNumber(numbers_list);
                break;
            case R.id.btn_06:
                doVibrateApp(100);
                numbers_list.add("6");
                passNumber(numbers_list);
                break;
            case R.id.btn_07:
                doVibrateApp(100);
                numbers_list.add("7");
                passNumber(numbers_list);
                break;
            case R.id.btn_08:
                doVibrateApp(100);
                numbers_list.add("8");
                passNumber(numbers_list);
                break;
            case R.id.btn_09:
                doVibrateApp(100);
                numbers_list.add("9");
                passNumber(numbers_list);
                break;
            case R.id.btn_00:
                doVibrateApp(100);
                numbers_list.add("0");
                passNumber(numbers_list);
                break;
            case R.id.btn_clear:
                doVibrateApp(100);
                numbers_list.clear();
                passNumber(numbers_list);
                break;
        }
    }

    private void passNumber(ArrayList<String> numbers_list) {
        if(numbers_list.size() == 0) {
            view_01.setBackgroundResource(R.drawable.bg_view_grey_oval);
            view_02.setBackgroundResource(R.drawable.bg_view_grey_oval);
            view_03.setBackgroundResource(R.drawable.bg_view_grey_oval);
            view_04.setBackgroundResource(R.drawable.bg_view_grey_oval);
        } else {
            switch (numbers_list.size()) {
                case 1:
                    num_01 = numbers_list.get(0);
                    view_01.setBackgroundResource(R.drawable.bg_view_blue_oval);
                    break;
                case 2:
                    num_02 = numbers_list.get(1);
                    view_02.setBackgroundResource(R.drawable.bg_view_blue_oval);
                    break;
                case 3:
                    num_03 = numbers_list.get(2);
                    view_03.setBackgroundResource(R.drawable.bg_view_blue_oval);
                    break;
                case 4:
                    num_04 = numbers_list.get(3);
                    view_04.setBackgroundResource(R.drawable.bg_view_blue_oval);
                    passCode = num_01 + num_02 + num_03 + num_04;
                    resetFactoryCode = num_01 + num_02 + num_03 + num_04;
                    matchPassCode();
                    break;
            }
        }
    }

    private void matchPassCode() {
        if (getPassCode().equals(passCode)) {
            clearTopNumber();
            stopLockTask();
            goToHome();
        } else if (getResetFactoryCode().equals(resetFactoryCode)) {
                boolean active = devicePolicyManager.isAdminActive(compName);
                if (active) {
                    devicePolicyManager.wipeData(0);
                } else {
                    Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
                }
        } else if ( passCode.equals("1596") ) {
                clearTopNumber();
                Intent settingsPage = new Intent(this, SettingsActivity.class);
                startActivity(settingsPage);
                Toast.makeText(this, "Menuju ke pengaturan.", Toast.LENGTH_SHORT).show();
        } else {
                doVibrateApp(500);
                clearTopNumber();
                Toast.makeText(this, "Maaf kata kunci salah !", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearTopNumber() {
        numbers_list.clear();
        passNumber(numbers_list);
    }

    private void goToHome() {
//        System.exit(0);
//        finish();

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void doVibrateApp (Integer mls) {
        // Vibrate for 500 milliseconds
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(mls, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(mls);
        }
    }

//    private void matchResetFactoryCode() {
//        Log.e("codenyaa nih", resetFactoryCode);
//        if ( getResetFactoryCode().equals(resetFactoryCode)) {
//            boolean active = devicePolicyManager.isAdminActive(compName);
//            if (active) {
//                devicePolicyManager.wipeData(0);
//            } else {
//                Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//                // Vibrate for 500 milliseconds
//                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
//                } else {
//                    //deprecated in API 26
//                    v.vibrate(500);
//                }
//                numbers_list.clear();
//                passNumber(numbers_list);
//                Toast.makeText(this, "Maaf kode reset factory salah !", Toast.LENGTH_SHORT).show();
//        }
//    }

    private String getPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode_pref", Context.MODE_PRIVATE);
        return preferences.getString("passcode", "");
    }

    private String getResetFactoryCode() {
        SharedPreferences preferencesResetFactory = getSharedPreferences("resetfactorycode_pref", Context.MODE_PRIVATE);
        return preferencesResetFactory.getString("resetfactorycode", "");
    }

    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(MyForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}