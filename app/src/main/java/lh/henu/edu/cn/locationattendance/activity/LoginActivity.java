package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.NetReceiver;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.sign.ReceiveMessageService;
import lh.henu.edu.cn.locationattendance.util.ApplyPermission;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.MD5;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btnLogin;
    private TextView btnRegister;
    private TextView btnReset;
    private EditText userName;
    private EditText userPassword;
    private QQConnection conn;
    EventHandler eventHandler;
    private LocalBroadcastManager localBroadcastManager;
    private NetReceiver netReceiver;
    private Handler loginHandler;
    public final static String MSGCONTENT = "lh.henu.edu.cn.locationattendance.activity.LoginActivity.msg.content";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 初始化shareSDK 需要添加
        MobSDK.init(getApplicationContext(),"21380b843a2b0","f8b81745732e6ed758dc15ddb395aa1b");
        initEventHandler();

        initLoginHandler();

        initBroadcastReceiver();
        initView();

        //网络连接
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn==null)
        {
            conn = new QQConnection();
            Log.i("ttss", "onCreate: "+"登录");
            conn.connect();
            conn.addOnMessageListener(loginListener);
            ((LocationAttendanceApp)getApplication()).setConn(conn);

        }else{
            conn.addOnMessageListener(loginListener);

        }
        //绑定用来提示网络问题
        conn.bindContext(this);
        //获取权限
        ApplyPermission.applyPermissions(LoginActivity.this);

    }

    private void initLoginHandler(){
        loginHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0x001:
                        //登陆失败，提示一下
                        Toast.makeText(LoginActivity.this, "密码错误或账号不存在！", Toast.LENGTH_SHORT).show();
                        break;
                    case 0x002:
                        //登录成功，保存用户的账号
                        String sUserName = userName.getText().toString();
                        String sPassWord = userPassword.getText().toString();
                        conn.setUserName(sUserName);
                        try {
                            conn.setPassWord(MD5.toMd5String(sPassWord));
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        LocationAttendanceApp locationAttendanceApp1 = (LocationAttendanceApp)getApplication();
                        locationAttendanceApp1.setUserName(sUserName);
                        conn.runHeart();
                        break;
                    case 0x003:
                        //保存服务器返回的登陆信息
                        LocationAttendanceApp locationAttendanceApp = (LocationAttendanceApp)getApplication();
                        locationAttendanceApp.setGroupListString(msg.getData().getString(MSGCONTENT));
                        //打开主页
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                }
            }
        };
    }



    private void initBroadcastReceiver(){
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    //实现listener接口
    private onMessageListener loginListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    //服务器返回登陆结果
                    if(AMessageType.TYPE_LOGIN.equals(msg.type)){
                        if(AMessageType.CONTENT_LOGIN_FAIL.equals(msg.content))
                        {
                            loginHandler.sendEmptyMessage(0x001);

                        }else{
                            loginHandler.sendEmptyMessage(0x002);
                        }
                    }
                    //列表信息到来
                    if(AMessageType.TYPE_GROUP_LIST.equals(msg.type)){
                        Bundle bundle = new Bundle();
                        bundle.putString(MSGCONTENT,msg.content);
                        Message message = loginHandler.obtainMessage();
                        message.what = 0x003;
                        message.setData(bundle);
                        loginHandler.sendEmptyMessage(0x003);
                    }
                }
            });

        }
    };

    private void initView() {
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister = (TextView) findViewById(R.id.login_register);
        btnReset = (TextView) findViewById(R.id.login_reset);
        userName = (EditText) findViewById(R.id.et_mobile);
        userPassword = (EditText) findViewById(R.id.et_password);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                //取出账号
                String sUserName = userName.getText().toString();
                //取出密码
                String sPassWord = userPassword.getText().toString();
                if("".equals(sUserName)||"".equals(sPassWord)){
                    Toast.makeText(this,"输入不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                //给服务器发送消息
                //封装给服务器的消息
                AMessage msg = new AMessage();
                //设置类型为登陆
                msg.type = AMessageType.TYPE_LOGIN;
                //登陆内容：userName + "#" + password
                try {
                    msg.content = sUserName + "#" + MD5.toMd5String(sPassWord);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                conn.sendMessage(msg,true,this);

                break;
            case R.id.login_register:
                //打开填写手机号界面
                Intent registerIntent = new Intent(LoginActivity.this, SendPhoneActivity.class);
                //"1"代表注册
                registerIntent.putExtra("code","1");
                startActivity(registerIntent);
                break;
            case R.id.login_reset:
                //打开填写手机号界面
                Intent resetIntent = new Intent(LoginActivity.this, SendPhoneActivity.class);
                //"2"代表重置密码
                resetIntent.putExtra("code","2");
                startActivity(resetIntent);
                break;
        }
    }

    /*本handler需要添加 对mod进行消息处理*/
    private void initEventHandler() {

        //创建EventHandler对象
        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {

                if(result== SMSSDK.RESULT_COMPLETE){
                    if(event==SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
                        //校验验证码，返回校验的手机和国家代码

/*                        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                        startActivity(intent);*/
                        Intent intent=new Intent("lh.henu.edu.cn.locationattendance.VERIFY_SUCCESS");
                        localBroadcastManager.sendBroadcast(intent);
                    }else if(event==SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //true为智能验证，false为普通下发短信
                    }else if(event==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }else {
                    }
                }
            }


            @Override
            public void onRegister() {
                super.onRegister();
            }

            @Override
            public void beforeEvent(int i, Object o) {
                super.beforeEvent(i, o);
            }

            @Override
            public void onUnregister() {
                super.onUnregister();
            }
        };

        //注册监听器
        SMSSDK.registerEventHandler(eventHandler);
    }

    //释放资源
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            //移除监听接口
            conn.removeOnMessageListener(loginListener);
            //解除绑定
            conn.unBindContext();
        }
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case ApplyPermission.requestCode:
                //没有权限申请到，就关闭应用
                if(ApplyPermission.permissionList.size()!=0){
                    //finish();
                }
                break;
            default:
                break;
        }
    }
}
