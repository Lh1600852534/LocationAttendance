package lh.henu.edu.cn.locationattendance.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView groupInfoTextView;//群名
    private ImageView groupInfoIamgeView;//群头像
    private TextView groupRealName;//群主名字
    private ImageView groupInfoBackImageView;//返回
    private Button groupInfoJoinButton;//申请加入按钮
    private String groupString[];
    private String groupInfoString;
    private QQConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        //获取连接
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.bindContext(this);
            conn.addOnMessageListener(groupInfoListener);
        }
        //获得群信息
        //content为groupName+群主名字
        if((groupInfoString = getIntent().getStringExtra(JoinGroupActivity.GROUP_INFO_LABLE))!=null){
            groupString = groupInfoString.split("#");
        }
        initView();
    }

    private QQConnection.onMessageListener groupInfoListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_REQUEST_JOIN_GROUP_TO.equals(msg.type)){
                        if("-1".equals(msg.content)){
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GroupInfoActivity.this,"你已经是群成员",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
        }
    };


    //初始化界面
    public void initView(){
        groupInfoTextView = (TextView)findViewById(R.id.group_info_group_name_textview);
        groupInfoIamgeView = (ImageView)findViewById(R.id.group_info_group_image_textview);
        groupInfoBackImageView = (ImageView)findViewById(R.id.join_group_back_imageview);
        //添加返回事件
        groupInfoBackImageView.setOnClickListener(this);
        groupInfoJoinButton = (Button)findViewById(R.id.group_info_join_button);
        //显示群名
        groupInfoTextView.setText("群名："+groupString[0]);
        //加载图片
        //Glide.with(this).load(groupData.imageUrl).into(groupInfoIamgeView);
        groupInfoIamgeView.setImageResource(R.drawable.ic_toolbar_face);
        //添加加入Button点击事件
        groupInfoJoinButton.setOnClickListener(this);
        groupRealName = (TextView)findViewById(R.id.group_info_group_real_name_textview);
        groupRealName.setText("群主："+groupString[1]);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.join_group_back_imageview:
                finish();
                break;
            case R.id.group_info_join_button:
                //发送申请信息groupid
                AMessage msg = new AMessage();
                msg.type = AMessageType.TYPE_REQUEST_JOIN_GROUP_TO;
                msg.content = groupString[2];
                conn.sendMessage(msg,true,GroupInfoActivity.this);
                break;
                default:
                    break;
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
