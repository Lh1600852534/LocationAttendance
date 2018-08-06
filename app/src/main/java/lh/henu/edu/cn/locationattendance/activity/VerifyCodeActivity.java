package lh.henu.edu.cn.locationattendance.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.smssdk.SMSSDK;
import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;

public class VerifyCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView verifyCode_next;
    private ImageView verifyCodeBack;
    private TextView verifyCodeAgainSend;
    private TextView verifyCodeAgainPhone;
    private EditText verifyCode_code;
    private String code;
    private String phone;
    private QQConnection conn;
    public int time;
    Thread thread;

    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        initView();
        initBroadcastReceiver();
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.bindContext(this);
        }
    }

    private void initBroadcastReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("lh.henu.edu.cn.locationattendance.VERIFY_SUCCESS");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    private void initView() {
        verifyCode_next = (ImageView) findViewById(R.id.verifyCode_next);
        verifyCodeBack = (ImageView) findViewById(R.id.verifyCode_back);
        verifyCodeAgainSend = (TextView) findViewById(R.id.verifyCode_again_send);
        verifyCodeAgainPhone = (TextView) findViewById(R.id.verifyCode_again_phone);
        verifyCode_code = (EditText) findViewById(R.id.verifyCode_code);

        verifyCode_next.setOnClickListener(this);
        verifyCodeBack.setOnClickListener(this);
        verifyCodeAgainSend.setOnClickListener(this);

        code = getIntent().getStringExtra("code");
        phone = "+86 " + getIntent().getStringExtra("phone");

        verifyCodeAgainPhone.setText(phone);

        changeBtnGetCode();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.verifyCode_next:
                SMSSDK.submitVerificationCode("86", getIntent().getStringExtra("phone"),
                        verifyCode_code.getText().toString().isEmpty() ? "-1" : verifyCode_code.getText().toString());
                break;
            case R.id.verifyCode_back:
                finish();
                break;
            case R.id.verifyCode_again_send:
                //Toast.makeText(VerifyCodeActivity.this, "重新发送", Toast.LENGTH_SHORT).show();
                SMSSDK.getVerificationCode("86",getIntent().getStringExtra("phone"));
                changeBtnGetCode();
                break;
        }
    }

    /**
     * author: 李豪
     * create: 2017/10/11  15:46
     * description:改变按钮样式
     */
    private void changeBtnGetCode() {

        thread = new Thread() {
            @Override
            public void run() {
                time = 60;
/*                tag=true;
                if(tag){*/
                while (time > 1) {
                    time--;
                    //如果活动为空
                    if (VerifyCodeActivity.this == null) {
                        break;
                    }

                    VerifyCodeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //通过线程设置按钮text逐秒递减，并设置不可点击
                            verifyCodeAgainSend.setText("重新发送(" + time + ")");
                            verifyCodeAgainSend.setClickable(false);
                            // verifyCodeAgainSend.setTextColor(0xc3c3c3);
                            verifyCodeAgainSend.setTextColor(Color.GRAY);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                thread.interrupt();
/*                    tag=false;
                }*/

                if (VerifyCodeActivity.this != null) {
                    VerifyCodeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            verifyCodeAgainSend.setText("重新发送");
                            verifyCodeAgainSend.setClickable(true);
                            verifyCodeAgainSend.setTextColor(Color.BLACK);

                        }
                    });
                }
            }

        };
        thread.start();
    }

    /**
     * author: 李豪
     * create: 2017/10/25  20:25
     * description:创建本地广播，短信验证通过时，接收消息
     */
    class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (code.equals("1")) {
                //code为1代表注册
                Intent intent1 = new Intent(VerifyCodeActivity.this, RegisterActivity.class);
                intent1.putExtra("phone",phone.substring(4));
                startActivity(intent1);
            } else {
                //code为2代表重置密码
                Intent intent2 = new Intent(VerifyCodeActivity.this, ResetActivity.class);
                intent2.putExtra("phone",phone.substring(4));
                startActivity(intent2);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            conn.unBindContext();
        }
    }
}
