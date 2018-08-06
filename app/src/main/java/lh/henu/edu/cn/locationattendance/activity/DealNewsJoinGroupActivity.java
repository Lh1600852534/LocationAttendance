package lh.henu.edu.cn.locationattendance.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;

public class DealNewsJoinGroupActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView backImageView;//返回键
    private Button agreeButton;//同意按钮
    private Button denyButton;//拒绝按钮
    private TextView contentTextView;//消息内容
    private ImageView headImageView;//请求人的头像
    private AMessage msg;
    private QQConnection conn;//与服务器的链接
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_news_join_group);
        Gson g = new Gson();
        msg = g.fromJson(getIntent().getStringExtra(NewsFragment.MSGCONTENT),AMessage.class);//获取消息
        conn = ((LocationAttendanceApp)getApplication()).getConn();//获取连接
        initView();
    }
    //初始化界面
    private void initView(){
        backImageView = (ImageView)findViewById(R.id.activity_deal_news_join_group_back_imageview);
        agreeButton = (Button)findViewById(R.id.activity_deal_news_join_group_agree_button);
        denyButton = (Button)findViewById(R.id.activity_deal_news_join_group_deny_button);
        headImageView = (ImageView)findViewById(R.id.activity_deal_news_join_head_imageview);
        contentTextView = (TextView)findViewById(R.id.activity_deal_news_join_content_textview);
        //设置头像
        headImageView.setImageResource(R.drawable.ic_toolbar_face);
        //设置消息内容
        //content为username+"#"+groupNumber+"#"+"realName"+"#"+groupname
        String params[] = msg.content.split("#");
        contentTextView.setText(params[2]+"请求加入"+params[3]);
        backImageView.setOnClickListener(this);
        agreeButton.setOnClickListener(this);
        denyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //msg.content为username+groupnumber+realname+groupname
        String params[] = msg.content.split("#");
        switch (v.getId()){
            case R.id.activity_deal_news_join_group_back_imageview:
                //返回，销毁界面
                finish();
                break;
            case R.id.activity_deal_news_join_group_agree_button:
                //同意加群
                /**
                 *账号+群号+1
                 */

                AMessage agreeMsg = new AMessage();
                agreeMsg.type = AMessageType.TYPE_REQUEST_JOIN_GROUP_DEAL;
                agreeMsg.content = params[0]+"#"+params[1]+"#"+1;
                conn.sendMessage(agreeMsg,true,this);

                break;
            case R.id.activity_deal_news_join_group_deny_button:
                //拒绝加群
                /**
                 * 请求人的账号+要加入的群号+0
                 */
                AMessage denyMsg = new AMessage();
                denyMsg.type = AMessageType.TYPE_REQUEST_JOIN_GROUP_DEAL;
                denyMsg.content = params[0]+"#"+params[1]+"#"+0;
                conn.sendMessage(denyMsg,true,this);
                break;
            default:
                break;
        }
    }
}
