package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.SMSSDK;
import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;

public class SendPhoneActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView ToVerify;
    private ImageView btnBack;
    private EditText sendPhone;
    private String code;
    private QQConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_phone);
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.bindContext(this);
        }
        initView();
    }

    /**
     * author: 李豪
     * create: 2017/9/24  11:56
     * description:初始化View
     */
    private void initView(){
        ToVerify=(ImageView)findViewById(R.id.send_to_verify);
        btnBack=(ImageView)findViewById(R.id.send_to_back);
        sendPhone=(EditText)findViewById(R.id.send_num);

        ToVerify.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        code=getIntent().getStringExtra("code");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_to_verify:
                sendPhone();
                break;
            case R.id.send_to_back:
                finish();
                break;
        }
    }

    /**
     * author: 李豪
     * create: 2017/10/11  15:08
     * description:发送短信，获取验证码
     */
    private  void sendPhone(){
        if(isMobileNo()){
            SMSSDK.getVerificationCode("86",sendPhone.getText().toString());
            Intent intent=new Intent(SendPhoneActivity.this,VerifyCodeActivity.class);
            intent.putExtra("code",code);
            intent.putExtra("phone",sendPhone.getText().toString());
            startActivity(intent);
        }else {
            Toast.makeText(SendPhoneActivity.this,"输入手机号错误，请重新输入",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * author: 李豪
     * create: 2017/10/11  15:10
     * description:判断输入是否为手机号
     */
    private boolean isMobileNo(){

        String pattern = "^1[3|4|5|7|8][0-9]\\d{4,8}$";
        String str = "";
        str=sendPhone.getText().toString();

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            conn.unBindContext();
        }

    }
}
