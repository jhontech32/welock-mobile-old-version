package com.app.iloku;

import static android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY;

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
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Calendar;

public class WidgetService extends Service {
    int LAYOUT_FLAGS;
    View mFloatingView;
    WindowManager windowManager;
    ImageView imageClose;
    TextView tvWidget;
    float height, width;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private DevicePolicyManager devicePolicyManager;
    private ActivityManager activityManager;
    private ComponentName compName;

    @Override
    public void onCreate() {
        super.onCreate();

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Build version checker
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            LAYOUT_FLAGS = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAGS = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Inflate widget layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_widget, null);
        WindowManager.LayoutParams layoutParams  = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAGS,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // Initialize Position
        layoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 100;

        // Layout params for close button
        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(140,
                140,
                LAYOUT_FLAGS,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        imageParams.gravity = Gravity.BOTTOM|Gravity.CENTER;
        imageParams.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        imageClose = new ImageView(this);
        imageClose.setImageResource(R.drawable.close_whitee_foreground);
        imageClose.setVisibility(View.INVISIBLE);
        windowManager.addView(imageClose, imageParams);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        tvWidget = (TextView) mFloatingView.findViewById(R.id.text_widget);

        // Show & update current time in textview
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvWidget.setText("\uD83D\uDD12");
                handler.postDelayed(this, 1000);
            }
        }, 10);

        // Drag movement for widget
        tvWidget.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;
            long startClickTime;

            int MAX_CLICK_DURATION = 200;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        imageClose.setVisibility(View.VISIBLE);

                        initialX = layoutParams.x;
                        initialY = layoutParams.y;

                        // Touch positions
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();

                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        imageClose.setVisibility(View.GONE);

                        layoutParams.x = initialX + (int)(initialTouchX - motionEvent.getRawX());
                        layoutParams.y = initialY + (int)(motionEvent.getRawY() - initialTouchY);

                        if ( clickDuration < MAX_CLICK_DURATION ) {
                            boolean active = devicePolicyManager.isAdminActive(compName);
                            if (active) {
                                new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                startActivity(getPackageManager().getLaunchIntentForPackage("com.app.iloku"));
                                            }
                                        },
                                        1000);
                                new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                devicePolicyManager.lockNow();
                                            }
                                        },
                                        3000);

                             } else {
                                callToast("You need to enable the Admin Device Features");
                            }
                        } else {
                            // Remove widget
                            if ( layoutParams.y > (height * 0.6) ){
                                stopSelf();
                            }
                        }

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate X + Y coordination of view
                        layoutParams.x = initialX + (int)(initialTouchX - motionEvent.getRawX());
                        layoutParams.y = initialY + (int)(motionEvent.getRawY() - initialTouchY);

                        // Update layout width
                        windowManager.updateViewLayout(mFloatingView, layoutParams);
                        if (layoutParams.y > (height * 0.6)) {
                            imageClose.setImageResource(R.drawable.close_whitee_foreground);
                        } else {
                            imageClose.setImageResource(R.drawable.close_whitee_foreground);
                        }
                        return true;
                }

                return false;
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            windowManager.removeView(mFloatingView);
        }

        if (imageClose != null) {
            windowManager.removeView(imageClose);
        }
    }

    private void callToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
