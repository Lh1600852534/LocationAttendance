package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.MD5;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView registerBack;
    private Button registerCancel;
    private Button registerSubmit;
    private TextInputEditText passWord;
    private TextInputLayout passWordLayout;
    private TextInputEditText repeatPassWord;
    private TextInputLayout repeatPassWordLayout;
    private TextInputEditText realName;
    private TextInputLayout realNameLayout;
    private QQConnection conn;
    private String username;//手机号即账号
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null)
        {
            conn.addOnMessageListener(registerListener);
            conn.bindContext(this);
        }

        initView();
    }

    private void initView(){
        registerBack=(ImageView)findViewById(R.id.register_back);
        registerCancel=(Button)findViewById(R.id.register_cancel);
        registerSubmit=(Button)findViewById(R.id.register_submit);

        registerBack.setOnClickListener(this);
        registerCancel.setOnClickListener(this);
        registerSubmit.setOnClickListener(this);

        passWordLayout = (TextInputLayout)findViewById(R.id.passWordLayout);
        passWord = (TextInputEditText) findViewById(R.id.passWord);
        //当文本改变时，判断是否输入合法（不为空且不含#）
        passWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passWordLayout.setErrorEnabled(true);
                //输入为空
                if("".equals(s.toString())){
                    passWordLayout.setError("输入不能为空");
                    return;
                }
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    passWordLayout.setError("包含非法字符'#'");
                    return;
                }
                //密码不在5-16
                if(!(s.toString().length()>=5&&s.toString().length()<=16)){
                    passWordLayout.setError("密码长度为5-16位");
                    return;
                }
                //输入合法，将错误信息关闭
                if(!s.toString().contains("#")&&!"".equals(s.toString())&&(s.toString().length()>=5&&s.toString().length()<=16)){
                    passWordLayout.setErrorEnabled(false);
                    return;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        repeatPassWordLayout = (TextInputLayout) findViewById(R.id.repeatPassWordLayout);
        repeatPassWord = (TextInputEditText) findViewById(R.id.repeatPassWord);
        repeatPassWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                repeatPassWordLayout.setErrorEnabled(false);
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    repeatPassWordLayout.setError("包含非法字符'#'");
                    return;
                }
                //输入为空
                if("".equals(s.toString())){
                    repeatPassWordLayout.setError("输入不能为空");
                    return;
                }
                //密码不在5-16
                if(!(s.toString().length()>=5&&s.toString().length()<=16)){
                    repeatPassWordLayout.setError("密码长度为5-16位");
                    return;
                }
                //密码不同
                if(!passWord.getText().toString().equals(repeatPassWord.getText().toString())){
                    repeatPassWordLayout.setError("两次密码输入不同");
                    return;
                }
                //输入合法，将错误信息关闭
                if((s.toString().length()>=5&&s.toString().length()<=16)&&!s.toString().contains("#")&&!"".equals(s.toString())&&passWord.getText().toString().equals(repeatPassWord.getText().toString())){
                    repeatPassWordLayout.setErrorEnabled(false);
                    return;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        realNameLayout = (TextInputLayout)findViewById(R.id.realNameLayout);
        realName = (TextInputEditText)findViewById(R.id.realName);
        realName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                realNameLayout.setErrorEnabled(true);
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    realNameLayout.setError("包含非法字符'#'");
                    return;
                }
                //输入为空
                if("".equals(s.toString())){
                    realNameLayout.setError("输入不能为空");
                    return;
                }
                //输入合法，将错误信息关闭
                if(!s.toString().contains("#")&&!"".equals(s.toString())){
                    realNameLayout.setErrorEnabled(false);
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
            case R.id.register_back:
                //打开登陆界面
                Intent intent1=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent1);
                finish();
            case R.id.register_cancel:
                //打开登陆界面
                Intent intent2=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.register_submit:
                //获取密码
                //第一次输入
                String s = passWord.getText().toString();
                //第二次输入
                String ss = repeatPassWord.getText().toString();
                //获取手机号
                username = getIntent().getStringExtra("phone");

                //判断信息填写是否有误
                if((ss.toString().length()>=5&&ss.toString().length()<=16)&&!ss.toString().contains("#")&&!"".equals(ss.toString())&&ss.toString().equals(s.toString()))
                {

                    final String sRealName = realName.getText().toString();//昵称
                    final String sPassword = passWord.getText().toString();//密码
                    final String sRepeatPassWord = repeatPassWord.getText().toString();
                    try {
                        //封装给服务器的信息
                        AMessage msg = new AMessage();
                        //注册类型的信息
                        msg.type = AMessageType.TYPE_REGISTER;
                        //注册内容为：username+"#"+password+"#"+"realname"+"#"+imgid
                        msg.content = username + "#" + MD5.toMd5String(sPassword) + "#" + sRealName;
                        //给服务器发送消息
                        conn.sendMessage(msg,false,null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    //实现onMessageListener.onReceive接口
    private onMessageListener registerListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    //处理注册时，服务器返回的信息
                    if(AMessageType.TYPE_REGISTER.equals(msg.type))
                    {
                        if(AMessageType.CONTENT_REGISTER_SUCCESS.equals(msg.content))
                        {
                            //注册成功，提示一下
                            Toast.makeText(RegisterActivity.this, "注册成功！",Toast.LENGTH_SHORT).show();
                            //打开登陆页面
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        }else{
                            //注册失败，提示一下
                            Toast.makeText(RegisterActivity.this,"注册失败！",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    };

    //释放资源

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            //移出监听
            conn.removeOnMessageListener(registerListener);
            conn.unBindContext();
        }
    }
}
