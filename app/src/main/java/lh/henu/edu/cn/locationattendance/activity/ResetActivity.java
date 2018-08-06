package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.MD5;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * 重置密码（实现）
 */
public class ResetActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView resetBack;
    private Button resetCancel;
    private Button resetSubmit;
    private TextInputEditText resetPassWord;
    private TextInputLayout resetPassWordLayout;
    private TextInputEditText resetRepeatPassWord;
    private TextInputLayout resetRepeatPassWordLayout;
    private QQConnection conn;
    private String host = "192.168.1.6";
    private int port = 8090;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        //获取手机号
        //phone = getIntent().getStringExtra("phone");
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null)
        {
            conn.addOnMessageListener(resetOnMessageListener);
            conn.bindContext(this);
        }
        initView();
    }

    private void initView(){
        resetBack=(ImageView)findViewById(R.id.reset_back);
        resetCancel=(Button)findViewById(R.id.reset_cancel);
        resetSubmit=(Button)findViewById(R.id.reset_submit);


        resetCancel.setOnClickListener(this);
        resetBack.setOnClickListener(this);
        resetSubmit.setOnClickListener(this);


        resetPassWord = (TextInputEditText) findViewById(R.id.resetPassWord);
        resetPassWordLayout = (TextInputLayout) findViewById(R.id.resetPassWordLayout);
        resetPassWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //输入为空
                if("".equals(s.toString())){
                    resetPassWordLayout.setError("输入不能为空");
                    return;
                }
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    resetPassWordLayout.setError("包含非法字符'#'");
                    return;
                }
                //密码不在5-16
                if(!(s.toString().length()>=5&&s.toString().length()<=16)){
                    resetPassWordLayout.setError("密码长度为5-16位");
                    return;
                }

                //输入合法，将错误信息关闭
                if(!s.toString().contains("#")&&
                        !"".equals(s.toString())&&
                        (s.toString().length()>=5&&s.toString().length()<=16)){
                    resetPassWordLayout.setErrorEnabled(false);
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!resetPassWord.getText().toString().equals(resetRepeatPassWord.getText().toString())){
                    resetRepeatPassWordLayout.setError("两次密码不一样");
                    return;
                }else{
                    resetRepeatPassWordLayout.setErrorEnabled(false);
                }
            }
        });

        resetRepeatPassWord = (TextInputEditText) findViewById(R.id.resetRepeatPassWord);
        resetRepeatPassWordLayout = (TextInputLayout) findViewById(R.id.resetRepeatPassWordLayout);
        resetRepeatPassWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    resetRepeatPassWordLayout.setError("包含非法字符'#'");
                    return;
                }
                //输入为空
                if("".equals(s.toString())){
                    resetRepeatPassWordLayout.setError("输入不能为空");
                    return;
                }
                //密码不在5-16
                if(!(s.toString().length()>=5&&s.toString().length()<=16)){
                    resetRepeatPassWordLayout.setError("密码长度为5-16位");
                    return;
                }
                //密码不同
                if(!resetPassWord.getText().toString().equals(resetRepeatPassWord.getText().toString())){
                    resetRepeatPassWordLayout.setError("两次密码输入不同");
                    return;
                }
                //输入合法，将错误信息关闭
                if((s.toString().length()>=5&&s.toString().length()<=16)&&
                        !s.toString().contains("#")&&!"".equals(s.toString())&&
                        resetRepeatPassWord.getText().toString().equals(resetPassWord.getText().toString())){
                    resetRepeatPassWordLayout.setErrorEnabled(false);
                    return;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reset_back:
                Intent intent1=new Intent(ResetActivity.this,LoginActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.reset_cancel:
                Intent intent2=new Intent(ResetActivity.this,LoginActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.reset_submit:
                phone = getIntent().getStringExtra("phone");
                //获得重置的第一次密码
                String sPassWord = resetPassWord.getText().toString();
                //获得重置的第二次密码，若第二次合法，那第一次一定合法
                String ssPassWord = resetRepeatPassWord.getText().toString();
                //输入合法
                if((ssPassWord.toString().length()>=5&&ssPassWord.toString().length()<=16)&&!ssPassWord.toString().contains("#")&&!"".equals(ssPassWord.toString())&&ssPassWord.equals(sPassWord)){
                    final AMessage message =new AMessage();
                    //消息为重置密码类型
                    message.type = AMessageType.TYPE_RESET_PASSWORD;

                    try {
                        //拼接发送给服务器的消息
                        message.content = phone +"#"+ MD5.toMd5String(ssPassWord);
                        conn.sendMessage(message,false,null);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                break;
        }
    }
    //实现监听接口
    private onMessageListener resetOnMessageListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            //判断服务器的消息类型
            if(AMessageType.TYPE_RESET_PASSWORD.equals(msg.type)){
                //若重置密码成功，提醒一下
                if(AMessageType.CONTENT_RESET_PASSWORD_SUCCESS.equals(msg.content)){
                    Toast.makeText(ResetActivity.this,"重置密码成功，去登录吧",Toast.LENGTH_SHORT).show();
                    //打开登陆界面
                    Intent loginIntent = new Intent(ResetActivity.this,LoginActivity.class);
                    startActivity(loginIntent);
                    //关闭本页面
                    finish();
                }else{
                    //重置密码失败，提醒一下
                    Toast.makeText(ResetActivity.this,"重置密码失败",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    //移除接口
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            conn.removeOnMessageListener(resetOnMessageListener);
            conn.unBindContext();
        }

    }
}
