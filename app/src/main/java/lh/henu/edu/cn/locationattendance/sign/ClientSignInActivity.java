package lh.henu.edu.cn.locationattendance.sign;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.google.gson.Gson;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.ApplyPermission;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class ClientSignInActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView backImageView;//返回图标
    private ImageView groupImageView;//群头像
    private TextView groupNameTextView;//群名
    private TextView startNameTextView;//发起人
    private Button signButton;//签到按钮
    private AMessage msg;//content为群名+发起人（即群主）+groupId
    private QQConnection conn;
    private boolean isSignIn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_sign_in);
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.addOnMessageListener(clientSignInListener);
        }
        if(getIntent().getStringExtra(NewsFragment.MSGCONTENT)!=null){
            Gson g = new Gson();
            msg = g.fromJson(getIntent().getStringExtra(NewsFragment.MSGCONTENT),AMessage.class);
        }
        initView();
    }
    //实现服务器返回消息处理
    private QQConnection.onMessageListener clientSignInListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_SIGN_IN_RESULT.equals(msg.type)){
                        if(AMessageType.CONTENT_START_SIGN_IN_FAIL.equals(msg.content)){
                            Toast.makeText(ClientSignInActivity.this,"签到失败",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(ClientSignInActivity.this,"签到成功",Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            });

        }
    };
    //位置监听
    private BDAbstractLocationListener bdAbstractLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(!isSignIn){
                AMessage signInMsg = new AMessage();
                signInMsg.type = AMessageType.TYPE_SIGN_IN_RESULT;
                //服务器通知消息的content为群号+发起时间+群主经度+群主纬度+终止时间+群主账号+群昵称+群主名字

                String notifParams[] = msg.content.split("#");
                long signTime = System.currentTimeMillis();
                if(signTime > Long.parseLong(notifParams[4])){
                    //签到时间已过
                    Toast.makeText(ClientSignInActivity.this,"签到时间已结束",Toast.LENGTH_SHORT).show();
                }else{
                    //发给服务器的content为群号+发起时间+群主账号+群主经度+群主纬度+自己经度+自己纬度
                    signInMsg.content = notifParams[0]+"#"+notifParams[1]+"#"+notifParams[5]+"#"+notifParams[2]+"#"+notifParams[3]+"#"+location.getLongitude()+"#"+location.getLatitude();
                    conn.sendMessage(signInMsg,true,ClientSignInActivity.this);
                }

                isSignIn = true;
            }else{
                Toast.makeText(ClientSignInActivity.this,"你已处理了这条消息",Toast.LENGTH_SHORT).show();
            }
        }
    };


    //初始化界面
    private void initView(){
        backImageView = (ImageView)findViewById(R.id.client_sign_in_back_imageview);
        backImageView.setOnClickListener(this);
        groupImageView = (ImageView)findViewById(R.id.client_sign_in_group_image_imageview);
        //设置头像
        signButton = (Button)findViewById(R.id.client_sign_in_sign_button);
        signButton.setOnClickListener(this);

        String params[] = msg.content.split("#");
        groupNameTextView = (TextView)findViewById(R.id.client_sign_in_group_name_textview);
        groupNameTextView.setText("群名："+params[6]);
        startNameTextView = (TextView)findViewById(R.id.client_sign_in_start_name_textview);
        startNameTextView.setText("发起人："+params[7]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.client_sign_in_back_imageview:
                finish();
                break;
            case R.id.client_sign_in_sign_button:
                long locationMinTime = 200;
                float locationMinDistance = 0;
                //获得locationClient
                LocationClient locationClient = new LocationClient(this);
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    //设置GPS获得位置
                    locationClient.registerLocationListener(bdAbstractLocationListener);
                    locationClient.start();
                }else{
                    //没有权限就申请
                    ApplyPermission.applyPermissions(this);
                }
                break;


            default:
                break;
        }
    }
}
