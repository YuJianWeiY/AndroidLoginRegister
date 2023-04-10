package com.example.androidloginregister;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Vibrator vibrator;
    private final long VIBRATION_DURATION = 100; // 震动持续时间100毫秒

    private Handler handler;
    private UserDao userDao;

    private boolean isRememberUserName=false;//是否记住用户名
    private boolean isRememberUserPassword=false;//是否记住密码
    private SharedPreferences sharedPreferences;//声明一个共享参数对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        TextView textview_login=findViewById(R.id.textview_login);//从布局文件中获取textview_login文本视图
        Button button_register_no=findViewById(R.id.button_register_no);//获取跳转到注册页面功能的button按钮的实例
        Button button_login=findViewById(R.id.button_login);//获取button登录按钮实例
        CheckBox checkbox_remember_id=findViewById(R.id.checkbox_remember_id);//获取CheckBox实例
        CheckBox checkbox_remember_password=findViewById(R.id.checkbox_remember_password);//获取CheckBox实例
        EditText editText_username=findViewById(R.id.editText_username);//获取EditText实例
        EditText editText_userpassword=findViewById(R.id.editText_userpassword);//获取EditText实例


        //为每个button按钮控件注册点击监听器
        button_register_no.setOnClickListener(this);
        button_login.setOnClickListener(this);
        //为每个CheckBox控件注册点击监听器
        checkbox_remember_id.setOnClickListener(this);
        checkbox_remember_password.setOnClickListener(this);

        //给checkbox_remember_id设置勾选监听器
        checkbox_remember_id.setOnCheckedChangeListener(((buttonView, isChecked) -> isRememberUserName=isChecked));
        //给checkbox_remember_password设置勾选监听器
        checkbox_remember_password.setOnCheckedChangeListener(((buttonView, isChecked) -> isRememberUserPassword=isChecked));

        sharedPreferences=getSharedPreferences("login",MODE_PRIVATE);//从login.xml获取共享参数实例
        String username=sharedPreferences.getString("username","");//获取共享参数保存的用户名
        String userpassword=sharedPreferences.getString("userpassword","");//获取共享参数保存的密码
        boolean ischeckName=sharedPreferences.getBoolean("ischeckName",false);
        boolean ischeckPassword=sharedPreferences.getBoolean("ischeckPassword",false);
        editText_username.setText(username);//在用户名编辑框中填写上次保存的用户名
        editText_userpassword.setText(userpassword);//在密码编辑框中填写上次保存的密码
        checkbox_remember_id.setChecked(ischeckName);
        checkbox_remember_password.setChecked(ischeckPassword);


        handler=new Handler(getMainLooper());//获取主线程
        userDao=new UserDao();


        //设计动态渐变
        int gradient_startColor= Color.rgb(148,0,211);//定义深紫色为渐变起点
        int gradient_endColor=Color.rgb(255,0,0);//定义深红色为渐变终点
        int[] color_start_end={gradient_startColor,gradient_endColor};
        float[] position={0f,1f};
        //创建一个LinearGradient渐变对象应用于TextView的Paint对象，实现TextView中文字的渐变效果
        LinearGradient shader=new LinearGradient(0,0,textview_login.getTextSize()*textview_login.getText().length(),textview_login.getTextSize(),color_start_end,position, Shader.TileMode.CLAMP);
        textview_login.getPaint().setShader(shader);
        //实例化对象，创建透明动画效果，从0.7f到1.0f渐变  0.0是完全透明，1.0完全不透明
        AlphaAnimation animation=new AlphaAnimation(0.7f,1.0f);
        //设置动画持续时常  单位：毫秒
        animation.setDuration(300);
        //设置重复次数
        animation.setRepeatCount(Animation.INFINITE);
        //设置重复模式
        animation.setRepeatMode(Animation.REVERSE);
        //给TextView文本添加动画效果
        textview_login.startAnimation(animation);


        //保持竖屏
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double angle = Math.atan2(y, x) * 180 / Math.PI;
                if (angle < -45 && angle > -135) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                } else if (angle > 45 && angle < 135) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);


        //点击button按钮缩小，松开恢复和点击震动
        button_register_no.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        //缩小按钮
                        ScaleAnimation shrinkAnimation=new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        shrinkAnimation.setDuration(100);
                        shrinkAnimation.setFillAfter(true);
                        v.startAnimation(shrinkAnimation);
                        break;
                    case MotionEvent.ACTION_UP:
                        //恢复按钮
                        ScaleAnimation restoreAnimation=new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        restoreAnimation.setDuration(100);
                        restoreAnimation.setFillAfter(true);
                        v.startAnimation(restoreAnimation);
                        break;
                }
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时开始震动
                        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case MotionEvent.ACTION_UP:
                        // 松开时停止震动
                        vibrator.cancel();
                        break;
                }
                return false;
            }
        });


        //点击button按钮缩小，松开恢复和点击震动
        button_login.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        //缩小按钮
                        ScaleAnimation shrinkAnimation=new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        shrinkAnimation.setDuration(100);
                        shrinkAnimation.setFillAfter(true);
                        v.startAnimation(shrinkAnimation);
                        break;
                    case MotionEvent.ACTION_UP:
                        //恢复按钮
                        ScaleAnimation restoreAnimation=new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        restoreAnimation.setDuration(100);
                        restoreAnimation.setFillAfter(true);
                        v.startAnimation(restoreAnimation);
                        break;
                }
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时开始震动
                        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case MotionEvent.ACTION_UP:
                        // 松开时停止震动
                        vibrator.cancel();
                        break;
                }
                return false;
            }
        });


        //点击CheckBox震动
        checkbox_remember_id.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时开始震动
                        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case MotionEvent.ACTION_UP:
                        // 松开时停止震动
                        vibrator.cancel();
                        break;
                }
                return false;
            }
        });


        //点击CheckBox震动
        checkbox_remember_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时开始震动
                        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                        break;
                    case MotionEvent.ACTION_UP:
                        // 松开时停止震动
                        vibrator.cancel();
                        break;
                }
                return false;
            }
        });


    }


    public void login()
    {
        EditText editText_username=findViewById(R.id.editText_username);//获取EditText实例
        EditText editText_userpassword=findViewById(R.id.editText_userpassword);//获取EditText实例
        final String username=editText_username.getText().toString().trim();//获取用户输入的用户名
        final String userpassword=editText_userpassword.getText().toString().trim();//获取用户输入的密码
        if(TextUtils.isEmpty(username)&&!TextUtils.isEmpty(userpassword))
        {
            //弹出提醒对话框，提醒用户用户名不能为空
            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("尊敬的用户");
            builder.setMessage("用户名不能为空，请输入！");
            builder.setPositiveButton("好的",null);
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
            //设计AlertDialog提醒对话框大小
            WindowManager.LayoutParams layoutParams=alertDialog.getWindow().getAttributes();
            layoutParams.width=700;
            layoutParams.height=565;
            alertDialog.getWindow().setAttributes(layoutParams);//设置AlertDialog的宽高
            editText_username.requestFocus();
        }else if (TextUtils.isEmpty(userpassword)&&!TextUtils.isEmpty(username))
        {
            //弹出提醒对话框，提醒用户密码不能为空
            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("尊敬的用户");
            builder.setMessage("密码不能为空，请输入！");
            builder.setPositiveButton("好的",null);
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
            //设计AlertDialog提醒对话框大小
            WindowManager.LayoutParams layoutParams=alertDialog.getWindow().getAttributes();
            layoutParams.width=700;
            layoutParams.height=565;
            alertDialog.getWindow().setAttributes(layoutParams);//设置AlertDialog的宽高
            editText_userpassword.requestFocus();
        }else if(TextUtils.isEmpty(username)&&TextUtils.isEmpty(userpassword))
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("尊敬的用户");
            builder.setMessage("请输入用户名和密码！");
            builder.setPositiveButton("好的",null);
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
            //设计AlertDialog提醒对话框大小
            WindowManager.LayoutParams layoutParams=alertDialog.getWindow().getAttributes();
            layoutParams.width=700;
            layoutParams.height=565;
            alertDialog.getWindow().setAttributes(layoutParams);//设置AlertDialog的宽高
            editText_username.requestFocus();
            editText_userpassword.requestFocus();
        }else
        {
            //这里要以线程访问，否则会报错
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final User user_name=userDao.findUserName(username);
                    final User user=userDao.findUser(username,userpassword);
                    //这里使用Handler类中常用的一个方法，post(Runnable r),立即发送Runnable对象。这里使用已经创建的android.os.Handler对象
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(user_name==null)
                            {
                                //创建提醒对话框的建造器
                                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                                //设计对话框标题图标
                                builder.setIcon(R.mipmap.ic_launcher);
                                //设置对话框标题文本
                                builder.setTitle("尊敬的用户");
                                //设置对话框内容文本
                                builder.setMessage("您所输入的账号不存在，请重新输入！");
                                //设置对话框的肯定按钮文本及其点击监听器
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editText_username.setText("");//清空editText_username内容
                                        editText_userpassword.setText("");//清空editText_userpassword内容
                                        SharedPreferences.Editor editor=sharedPreferences.edit();
                                        editor.putBoolean("ischeckName",false);
                                        editor.putString("username","");
                                        editor.putBoolean("ischeckPassword",false);
                                        editor.putString("userpassword","");
                                        editor.commit();
                                        CheckBox checkbox_remember_id=findViewById(R.id.checkbox_remember_id);//获取CheckBox实例
                                        CheckBox checkbox_remember_password=findViewById(R.id.checkbox_remember_password);//获取CheckBox实例
                                        checkbox_remember_id.setChecked(false);
                                        checkbox_remember_password.setChecked(false);
                                    }
                                });
                                AlertDialog alertDialog=builder.create();//根据建造器构建提醒对话框对象
                                alertDialog.show();//显示提醒对话框
                                //设计AlertDialog提醒对话框大小
                                WindowManager.LayoutParams layoutParams=alertDialog.getWindow().getAttributes();
                                layoutParams.width=700;
                                layoutParams.height=565;
                                alertDialog.getWindow().setAttributes(layoutParams);//设置AlertDialog的宽高
                                return;
                            }
                            if(user==null)
                            {
                                //创建提醒对话框的建造器
                                AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                                //设计对话框标题图标
                                builder.setIcon(R.mipmap.ic_launcher);
                                //设置对话框标题文本
                                builder.setTitle("尊敬的用户");
                                //设置对话框内容文本
                                builder.setMessage("您所输入的密码错误，请重新输入！");
                                //设置对话框的肯定按钮文本及其点击监听器
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editText_userpassword.setText("");//清空editText_userpassword内容
                                        SharedPreferences.Editor editor=sharedPreferences.edit();
                                        editor.putBoolean("ischeckName",false);
                                        editor.putString("username","");
                                        editor.putBoolean("ischeckPassword",false);
                                        editor.putString("userpassword","");
                                        editor.commit();
                                        CheckBox checkbox_remember_id=findViewById(R.id.checkbox_remember_id);//获取CheckBox实例
                                        CheckBox checkbox_remember_password=findViewById(R.id.checkbox_remember_password);//获取CheckBox实例
                                        checkbox_remember_id.setChecked(false);
                                        checkbox_remember_password.setChecked(false);
                                    }
                                });
                                AlertDialog alertDialog=builder.create();//根据建造器构建提醒对话框对象
                                alertDialog.show();//显示提醒对话框
                                //设计AlertDialog提醒对话框大小
                                WindowManager.LayoutParams layoutParams=alertDialog.getWindow().getAttributes();
                                layoutParams.width=700;
                                layoutParams.height=565;
                                alertDialog.getWindow().setAttributes(layoutParams);//设置AlertDialog的宽高
                                return;
                            }else
                            {
                                //如果勾选了"记住账号"复选框，就把账号保存到共享参数里
                                if(isRememberUserName)
                                {
                                    SharedPreferences.Editor editor=sharedPreferences.edit();//获取编辑器对象
                                    editor.putBoolean("ischeckName",true);
                                    editor.putString("username",editText_username.getText().toString());//添加名为username的账号
                                    editor.commit();//提交编辑器修改
                                }
                                //如果勾选了“记住密码"复选框,就把密码保存到共享参数里
                                if(isRememberUserPassword)
                                {
                                    SharedPreferences.Editor editor=sharedPreferences.edit();
                                    editor.putBoolean("ischeckPassword",true);
                                    editor.putString("userpassword",editText_userpassword.getText().toString());//添加名为userpassword的密码
                                    editor.commit();
                                }
                                //创建一个意图对象，准备跳转到指定的活动页面
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                //跳转到意图对象指定的活动页面
                                startActivity(intent);
                            }
                        }
                    });
                }
            }).start();
        }
    }


    //设计读取button按钮点击的功能函数onClick()
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button_register_no)
        {
            //创建一个意图对象，准备跳转到指定的活动页面
            Intent intent=new Intent(this,RegisterActivity.class);
            //跳转到意图对象指定的活动页面
            startActivity(intent);
        }
        if(v.getId()==R.id.button_login)
        {
            login();
        }
    }
}