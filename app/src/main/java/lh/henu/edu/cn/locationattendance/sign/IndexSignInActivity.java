package lh.henu.edu.cn.locationattendance.sign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.tauth.Tencent;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.fragments.GroupFragments;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class IndexSignInActivity extends AppCompatActivity implements View.OnClickListener{

    private Tencent mTencent;
    private ImageView backImageView;//返回图标
    private TextView groupNameTextView;//群名
    private Button startSignInButton;//开始签到按钮
    private Button viewResultButton;//查看成员签到结果
    private Button personResultButton;//个人签到结果
    private Button sendToComputerButton;//发送结果到电脑
    private int groupIndex = 0;//分组序号
    private int childIndex = 0;//组内item序号
    private QQConnection conn;
    private LocationAttendanceApp locationAttendanceApp;
    private GroupData groupData;
    public final static String GROUPDATAJSON = "lh.henu.edu.cn.locationattendance.sign.IndexSignInActivity.group.data.json";
    public final static String GROUPID = "lh.henu.edu.cn.locationattendance.sign.IndexSignInActivity.group.id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_sign_in);
        groupIndex = getIntent().getIntExtra(GroupFragments.GROUPINDEX,-1);
        childIndex = getIntent().getIntExtra(GroupFragments.CHILDINDEX,-1);
        locationAttendanceApp = (LocationAttendanceApp)getApplication();
        groupData = locationAttendanceApp.getChild().get(groupIndex).get(childIndex);

        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.addOnMessageListener(signMessageListener);
        }
        initView();
    }

    private QQConnection.onMessageListener signMessageListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT.equals(msg.type)){
                        if(!"".equals(msg.content)){
                            SignInDataList signInDataList = new Gson().fromJson(msg.content,SignInDataList.class);
                            XLSHelper.writeToFile(signInDataList,groupData.groupId+"",IndexSignInActivity.this);
                            //发送到电脑
                        }else{
                            //发送到电脑
                        }
                    }
                }
            });
        }
    };

    //初始化界面
    private void initView(){
        backImageView = (ImageView)findViewById(R.id.index_sign_in_back_imageview);
        backImageView.setOnClickListener(this);
        startSignInButton = (Button)findViewById(R.id.index_start_sign_in_start_button);
        startSignInButton.setOnClickListener(this);
        viewResultButton = (Button)findViewById(R.id.index_view_result_sign_in_start_button);
        viewResultButton.setOnClickListener(this);
        groupNameTextView = (TextView)findViewById(R.id.index_sign_in_group_name_textview);
        groupNameTextView.setText(groupData.groupName);
        personResultButton = (Button)findViewById(R.id.index_sign_in_person_sign_in_result_button);
        personResultButton.setOnClickListener(this);
        sendToComputerButton = (Button)findViewById(R.id.index_sign_in_sned_result_to_computer_button);
        sendToComputerButton.setOnClickListener(this);
        //mTencent = Tencent.createInstance("",this);
        

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.index_sign_in_back_imageview:
                //点击返回图标
                finish();
                break;
            case R.id.index_start_sign_in_start_button:
                //点击开始签到按钮
                Intent startIntent = new Intent(this,StartSignInActivity.class);
                Gson g = new Gson();
                startIntent.putExtra(GROUPDATAJSON,g.toJson(groupData));
                startActivity(startIntent);
                break;

            case R.id.index_view_result_sign_in_start_button:
                //点击查看群成员按钮
                Intent viewIntent = new Intent(this,BaiduGroupPersonSignInResultActivity.class);
                viewIntent.putExtra(IndexSignInActivity.GROUPID,groupData.groupId+"");

                startActivity(viewIntent);
                break;
            case R.id.index_sign_in_person_sign_in_result_button:
                //查看个人签到记录
                Intent personIntent = new Intent(this,BaiduPersonSignInResultActivity.class);
                personIntent.putExtra(GROUPID,groupData.groupId+"");
                startActivity(personIntent);
                break;

            case R.id.index_sign_in_sned_result_to_computer_button:
                //发送签到结果到电脑
                AMessage getResults = new AMessage();
                getResults.type = AMessageType.TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT;
                getResults.content = groupData.groupId+"";
                conn.sendMessage(getResults,false,null);
                break;
            default:
                break;
        }
    }
}
