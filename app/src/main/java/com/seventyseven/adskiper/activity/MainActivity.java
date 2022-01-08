package com.seventyseven.adskiper.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.seventyseven.adskiper.R;
import com.seventyseven.adskiper.service.SkipService;
import com.suke.widget.SwitchButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Intent intent;
    private TextView ranktv;
    private TextView serviceState;
    private com.suke.widget.SwitchButton switchButton;
    private GestureDetector gue;//手势识别器


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gue = new GestureDetector(this,new MygestureListener());

        intent = new Intent(MainActivity.this, SkipService.class);
        startService(intent);
        switchButton = (com.suke.widget.SwitchButton)findViewById(R.id.switch_button);
        ranktv = findViewById(R.id.rank_tv);
        serviceState = findViewById(R.id.service_state);

        ranktv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RankActivity.class);
                startActivity(intent);
            }
        });
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked){
                    if (!isAccessibilityOpen(MainActivity.this, SkipService.class.getCanonicalName())) {
                        Toast.makeText(getApplicationContext(), "即将跳转至辅助功能", Toast.LENGTH_SHORT).show();
                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivity(intent);
                            }
                        };
                        //延迟跳转打开无障碍设置
                        handler.postDelayed(runnable,1500);
                    } else {
                        //启动服务
                        intent = new Intent(MainActivity.this, SkipService.class);
                        startService(intent);
                        Toast.makeText(getApplicationContext(), "服务已开启", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    //关闭服务
                    stopService(intent);
                    Toast.makeText(getApplicationContext(), "服务已关闭", Toast.LENGTH_SHORT).show();
                    switchButton.setChecked(false);
                    serviceState.setText("服务已关闭");
                }
            }
        });
    }

    //检查辅助功能是否开启
    private boolean isAccessibilityOpen(Context mContext, String serviceName) {
        int accessibilityEnabled = 0;
        // 对应的服务
        final String service = getPackageName() + "/" + serviceName;
        Log.d(TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.d(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            serviceState.setText("服务已开启");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.d(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "***ACCESSIBILITY IS DISABLED***");
            serviceState.setText("服务已关闭");
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isAccessibilityOpen(MainActivity.this, SkipService.class.getCanonicalName())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switchButton.setChecked(false);
                }
            });
        }
    }

    class MygestureListener extends GestureDetector.SimpleOnGestureListener{

        //onFling方法的第一个参数是 手指按下的位置， 第二个参数是 手指松开的位置，第三个参数是手指的速度
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float startX = e1.getX();//通过e1.getX（）获得手指按下位置的横坐标
            float endX = e2.getX();//通过e2.getX（）获得手指松开位置的横坐标
            float startY = e1.getY();//通过e1.getY（）获得手指按下位置的纵坐标
            float endY = e2.getY();//通过e2.getY（）获得手指松开的纵坐标
            if ((startX - endX) > 50 && Math.abs(startY - endY) < 200) {
                //(startX - endX) > 50 是手指从按下到松开的横坐标距离大于50
                // Math.abs(startY - endY) < 200 是手指从按下到松开的纵坐标的差的绝对值

                //在这里通过Intent实现界面转跳
                Intent intent = new Intent(MainActivity.this,RankActivity.class);
                startActivity(intent);
            }

            if (Math.abs(startY - endY) > 100) {
                //在这里通过Intent实现界面转跳
            }
            //返回值是重点：如果返回值是true则动作可以执行，如果是flase动作将无法执行
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gue.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}