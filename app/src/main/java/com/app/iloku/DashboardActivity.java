package com.app.iloku;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;

    Button disable, enable, stopTask, addWidget, saveBtn;
    public static final int RESULT_ENABLE = 11;
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;
    private EditText editText;

    // Initialize Variable
    EditText etCodeKey;
    String passCode = "";

    EditText etFactoryCode;
    String resetFactoryCode = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        getPermissions();

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        initializeComponents();

//        // UI Overlay Custom
//        View overlay = findViewById(R.id.dashboard_view);
//        overlay.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        enable.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    private void initializeComponents() {
        enable = findViewById(R.id.enableBtn);
        disable = findViewById(R.id.disableBtn);
        stopTask = findViewById(R.id.stopTaskBtn);
        addWidget = findViewById(R.id.button_widget);

        etCodeKey = findViewById(R.id.etCodeKey);
        etFactoryCode = findViewById(R.id.etFactoryCode);
        saveBtn = findViewById(R.id.saveBtn);

        enable.setOnClickListener(this);
        disable.setOnClickListener(this);
        stopTask.setOnClickListener(this);
        addWidget.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        etCodeKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // When value is not equal to empty and contain numeric value
//                    String codeKey = getIntent().getExtras().getString("codeKey");
                String codeKey = etCodeKey.getText().toString();
                passCode = codeKey;
                Log.e("value code", codeKey);
//                etCodeKey.setText("codeKey", codeKey);

//                    Log.e("value nya", charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etFactoryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // When value is not equal to empty and contain numeric value
//                    String codeKey = getIntent().getExtras().getString("codeKey");
                String codeKey = etFactoryCode.getText().toString();
                resetFactoryCode = codeKey;
                Log.e("value code", codeKey);
//                etCodeKey.setText("codeKey", codeKey);
//                    Log.e("value nya", charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enableBtn:
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                startActivityForResult(intent, RESULT_ENABLE);
                break;
            case R.id.disableBtn:
                devicePolicyManager.removeActiveAdmin(compName);
                disable.setVisibility(View.GONE);
                enable.setVisibility(View.VISIBLE);
                break;
            case R.id.stopTaskBtn:
                stopLockTask();
                break;
            case R.id.button_widget:
                if (Settings.canDrawOverlays(DashboardActivity.this)) {
                    Intent intentWidget = new Intent(DashboardActivity.this,
                            WidgetService.class);
                    startService(intentWidget);
                    finish();
                } else {
                    getPermissions();
                }
                break;
            case R.id.saveBtn:
                savePassCode(passCode);
                saveFactoryCode(resetFactoryCode);
                onBackPressed();
                Toast.makeText(this, "Berhasil membuat keycode screen.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(new Intent(this, MainActivity.class));
    }

//    private void restartApp() {
//        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
//        int mPendingIntentId = 101;
//        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//        System.exit(0);
//    }

    private void savePassCode(String passCode) {
        onRestart();
        SharedPreferences preferences = getSharedPreferences("passcode_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.e("Value passcode <<", passCode);
        editor.putString("passcode", passCode);
        editor.apply();
    }

    private void saveFactoryCode(String resetFactoryCode) {
        onRestart();
        SharedPreferences preferences = getSharedPreferences("resetfactorycode_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.e("Value reset factory <<", resetFactoryCode);
        editor.putString("resetfactorycode", resetFactoryCode);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(DashboardActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DashboardActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}