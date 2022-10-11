package com.app.iloku;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;

    ImageView backNavBtn;
    TextView adminName, adminInfo, Manufacture, Brand;
    Button disable, enable, addWidget, stopTask, showScreenCode, showResetCode, permissionWidget, permissionTask, permissionScreen, permissionReset, saveScreenBtn, saveResetBtn;
    BottomSheetDialog dialogScreen, dialogReset;

    public static final int RESULT_ENABLE = 11;
    public static final int RESULT_ENABLE_APPS = 5469;
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;
    private EditText editText;

    // Initialize Variable Form
    EditText etCodeKey;
    String passCode = "";

    EditText etFactoryCode;
    String resetFactoryCode = "";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dashboard);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        initializeComponents();

        createScreenDialog();
        createResetDialog();

        getBuildInfo();

    }

    @Override
    protected void onResume() {
        super.onResume();
        buttonCondition();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void buttonCondition() {
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        boolean isDrawOverOtherAppsAvailable = Settings.canDrawOverlays(this);

        disable.setVisibility(isActive ? View.GONE : View.VISIBLE);
        enable.setVisibility(isActive ? View.VISIBLE : View.GONE);

        addWidget.setVisibility(isDrawOverOtherAppsAvailable ? View.VISIBLE : View.GONE);
        permissionWidget.setVisibility(isDrawOverOtherAppsAvailable ? View.GONE : View.VISIBLE);

        stopTask.setVisibility(isActive ? View.VISIBLE : View.GONE);
        permissionTask.setVisibility(isActive ? View.GONE : View.VISIBLE);

        showScreenCode.setVisibility(isActive ? View.VISIBLE : View.GONE);
        permissionScreen.setVisibility(isActive ? View.GONE : View.VISIBLE);

        showResetCode.setVisibility(isActive ? View.VISIBLE : View.GONE);
        permissionReset.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    private void initializeComponents() {
        enable = findViewById(R.id.enableBtn);
        disable = findViewById(R.id.disableBtn);
        addWidget = findViewById(R.id.button_widget);
        stopTask = findViewById(R.id.stopTaskBtn);
        showScreenCode = findViewById(R.id.showScreenCode);
        showResetCode = findViewById(R.id.showResetCode);
        permissionWidget = findViewById(R.id.permissionWidgetBtn);
        permissionTask = findViewById(R.id.permissionTaskBtn);
        permissionScreen = findViewById(R.id.permissionScreenBtn);
        permissionReset = findViewById(R.id.permissionResetBtn);
        backNavBtn = findViewById(R.id.backNavBtn);

        // Dialog bottomsheet
        dialogScreen = new BottomSheetDialog(this);
        dialogReset = new BottomSheetDialog(this);

        // Inflate View
        createScreenDialog();
//        createResetDialog();

        enable.setOnClickListener(this);
        disable.setOnClickListener(this);
        addWidget.setOnClickListener(this);
        stopTask.setOnClickListener(this);
        showScreenCode.setOnClickListener(this);
        showResetCode.setOnClickListener(this);
        permissionWidget.setOnClickListener(this);
        permissionTask.setOnClickListener(this);
        permissionScreen.setOnClickListener(this);
        permissionReset.setOnClickListener(this);
        backNavBtn.setOnClickListener(this);

        saveScreenBtn = findViewById(R.id.saveScreenCodeBtn);
        etCodeKey = findViewById(R.id.etCodeKey);

        saveResetBtn = findViewById(R.id.saveResetCodeBtn);
        etFactoryCode = findViewById(R.id.etFactoryCode);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enableBtn:
                hasDisableBtn();
                break;
            case R.id.disableBtn:
                hasEnableBtn();
                break;
            case R.id.stopTaskBtn:
                stopLockTask();
                break;
            case R.id.button_widget:
                hasOpenWidget();
                break;
            case R.id.permissionWidgetBtn:
                openPermissionApp();
                break;
            case R.id.showScreenCode:
                 hasOpenScreenCode();
                break;
            case R.id.showResetCode:
                hasOpenResetCode();
                break;
            case R.id.backNavBtn:
                onBackPressed();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void getBuildInfo() {
        adminName = findViewById(R.id.adminName);
        adminName.setText(Build.MANUFACTURER + " " + Build.BRAND);

        adminInfo = findViewById(R.id.adminInfo);
        adminInfo.setText("SDK " + Build.VERSION.SDK + " | " + Build.MODEL);
    }

    private void createScreenDialog() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(
                R.layout.bottom_dialog_code_screen,
                null,
                false
        );

        saveScreenBtn = view.findViewById(R.id.saveScreenCodeBtn);
        etCodeKey = view.findViewById(R.id.etCodeKey);

        saveScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passCode = etCodeKey.getText().toString();
                savePassCode(passCode);
                dialogScreen.dismiss();
            }
        });

        dialogScreen.setContentView(view);
    }

    private void createResetDialog() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(
                R.layout.bottom_dialog_code_reset,
                null,
                false
        );

        saveResetBtn = view.findViewById(R.id.saveResetCodeBtn);
        etFactoryCode = view.findViewById(R.id.etFactoryCode);

        saveResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetFactoryCode = etFactoryCode.getText().toString();
                saveFactoryCode(resetFactoryCode);
                dialogReset.dismiss();
            }
        });

        dialogReset.setContentView(view);
    }

    private void hasEnableBtn() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    private void hasDisableBtn() {
        devicePolicyManager.removeActiveAdmin(compName);
        disable.setVisibility(View.VISIBLE);
        enable.setVisibility(View.GONE);

//        addWidget.setVisibility(View.GONE);
//        permissionWidget.setVisibility(View.VISIBLE);

        stopTask.setVisibility(View.GONE);
        permissionTask.setVisibility(View.VISIBLE);

        showScreenCode.setVisibility(View.GONE);
        permissionScreen.setVisibility(View.VISIBLE);

        showResetCode.setVisibility(View.GONE);
        permissionReset.setVisibility(View.VISIBLE);
    }

    private void hasOpenScreenCode() {
        dialogScreen.show();
        dialogScreen.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void hasOpenResetCode() {
        dialogReset.show();
        dialogReset.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void savePassCode(String passCode) {
        SharedPreferences preferences = getSharedPreferences("passcode_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.e("Value passcode <<", passCode);
        editor.putString("passcode", passCode);
        editor.apply();
    }

    private void saveFactoryCode(String resetFactoryCode) {
        SharedPreferences preferences = getSharedPreferences("resetfactorycode_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Log.e("Value reset factory <<", resetFactoryCode);
        editor.putString("resetfactorycode", resetFactoryCode);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void hasOpenWidget() {
                Intent intentWidget = new Intent(SettingsActivity.this,
                        WidgetService.class);
                startService(intentWidget);
                onBackPressed();
                Toast.makeText(SettingsActivity.this, "Smart Lock diaktifkan.", Toast.LENGTH_SHORT).show();
    }

    private void openPermissionApp() {
        stopLockTask();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(new Intent(this, MainActivity.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE :
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(SettingsActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void restartApp() {
        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 101;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intentMain, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
