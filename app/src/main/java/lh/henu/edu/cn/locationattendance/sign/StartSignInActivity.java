package lh.henu.edu.cn.locationattendance.sign;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.google.gson.Gson;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.ApplyPermission;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;

/**
 * 签到activity
 * 点击开始签到按钮后，动态显示剩余时间（默认是10分钟），动态显示签到人数
 */
public class StartSignInActivity extends AppCompatActivity implements View.OnClickListener{

    private long total;//总共时间
    private long count = 0;//用来记时
    private long personNum = 0;//用来记签到人数
    private Button startButton;//签到开始按钮
    private boolean isStart = false;//是否开始了签到，用来判断是否可以选择签到时间
    private TextView leftTimeTextView;//剩余时间
    private TextView personNumTextView;//签到人数
    private Handler handler;//用来更新界面
    private TextView groupNameTextView;//群名TextView
    private ImageView backImageView;//返回图标
    private GroupData groupData;//群信息
    private LocationAttendanceApp locationAttendanceApp;
    private QQConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);



        Gson g = new Gson();
        groupData = g.fromJson(getIntent().getStringExtra(IndexSignInActivity.GROUPDATAJSON),GroupData.class);
        locationAttendanceApp = (LocationAttendanceApp)getApplication();
        conn = locationAttendanceApp.getConn();
        if(conn!=null){
            conn.addOnMessageListener(startSignInListener);
        }

        initHandler();
        initView();
    }

    private onMessageListener startSignInListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_START_SIGN_IN.equals(msg.type)){
                        if(AMessageType.CONTENT_START_SIGN_IN_FAIL.equals(msg.content)){
                            //发起签到失败
                            handler.sendEmptyMessage(0x003);
                        }else{
                            //发起签到成功
                            isStart = true;
                            leftTime();

                        }
                    }
                    if(AMessageType.TYPE_UPDATE_SIGN_IN_PERSON_NUM.equals(msg.type)){
                        //更新人数请求
                        Log.i("ttss", "run: "+"群员发送签到消息");
                        handler.sendEmptyMessage(0x002);
                    }
                }
            });
        }
    };

    private void leftTime(){
        //开启线程更新时间，数total个数
        //total为秒数
        ThreadUtils.runInSubThread(new Runnable() {
            @Override
            public void run() {
                while (isStart){
                    handler.sendEmptyMessage(0x001);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //初始化handler
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0x001:
                        //用来倒计时
                        startButton.setEnabled(false);
                        startButton.setBackgroundColor(getResources().getColor(R.color.gray));
                        count++;
                        if((total-count)==0){
                            isStart = false;
                            personNum = 0;
                            startButton.setEnabled(true);
                            startButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        }else{
                            int minute = (int)(total-count)/60;
                            int mill = (int)(total-count)%60;
                            leftTimeTextView.setText(minute+" "+"分"+" "+mill+" "+"秒");
                        }
                        break;
                    case 0x002:
                        //用来更新签到人数
                        personNum++;
                        personNumTextView.setText("签到人数："+personNum);
                        break;
                    case 0x003:
                        //用来提示是否发起签到
                        Toast.makeText(getBaseContext(),"发起签到失败",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
    }
    private void initView(){
        personNumTextView = (TextView)findViewById(R.id.sign_in_person_number_textview);
        startButton = (Button)findViewById(R.id.sign_in_start_button);
        startButton.setOnClickListener(this);
        leftTimeTextView = (TextView)findViewById(R.id.sign_in_left_time_textview);
        leftTimeTextView.setOnClickListener(this);
        groupNameTextView = (TextView)findViewById(R.id.sign_in_group_name_textview);
        groupNameTextView.setText(groupData.groupName);
        backImageView = (ImageView)findViewById(R.id.sign_in_back_imageview);
        backImageView.setOnClickListener(this);
        leftTimeTextView.setOnClickListener(this);
    }

    private BDAbstractLocationListener bdLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(!isStart){
                //服务器判断是否可以发起签到(是否是群主，是否正在签到)，若成功发起签到，服务器让该群的其他成员不能发起签到
                AMessage startMessage = new AMessage();
                startMessage.type = AMessageType.TYPE_START_SIGN_IN;
                long startTime = System.currentTimeMillis();
                double longitude = location.getLongitude();//经度
                double latitude = location.getLatitude();//纬度
                //content为群号+发起时间+群主经度+群主纬度+终止时间
                startMessage.content = groupData.groupId+"#"+startTime+"#"+longitude+"#"+latitude+"#"+(startTime+(total*1000));
                conn.sendMessage(startMessage,false,null);
                isStart = true;
            }
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_start_button:
                if(!isStart){
                    //点击开始按钮，计数置为0，签到人数置为0，isStart置为true，通知服务器签到开始，并动态显示剩余时间何签到人数，并让开始签到按钮不能按

                    String[] s = leftTimeTextView.getText().toString().split(" |分|秒");
                    total = Long.parseLong(s[0])*60+Long.parseLong(s[3]);
                    personNum = 0;
                    count = 0;

                    long locationMinTime = 1000;
                    float locationMinDistance = 0;
                    //获得locationClient
                    LocationClient locationClient = new LocationClient(this);
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                        //设置GPS获得位置
                        locationClient.registerLocationListener(bdLocationListener);
                        locationClient.start();
                    }else{
                        //没有权限就申请
                        ApplyPermission.applyPermissions(this);
                    }
                }
                break;
            case R.id.sign_in_back_imageview:
                finish();
                break;
            case R.id.sign_in_left_time_textview:
                //没有开始签到，选择剩余时间
                if(!isStart){
                    final String time[] = {"10","20","30","40","50","60"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("选择时间");
                    builder.setItems(time, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leftTimeTextView.setText(time[which]+" "+"分"+" "+"00"+" "+"秒");
                        }
                    });
                    (builder.create()).show();
                }
                break;
        }
    }
}
