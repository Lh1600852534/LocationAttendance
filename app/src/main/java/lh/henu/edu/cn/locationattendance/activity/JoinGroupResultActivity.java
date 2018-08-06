package lh.henu.edu.cn.locationattendance.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;

public class JoinGroupResultActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView joinGroupResultBackImageView;//返回键
    private ImageView joinGroupResultHeadImageView;//群头像
    private TextView joinGroupResultContentTextView;//请求结果
    private TextView joinGroupResultGroupNameTextView;//群名
    private AMessage msg;//消息,content为群名+0/1
    private QQConnection conn;
    String params[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_result);
        Gson g = new Gson();
        msg = g.fromJson(getIntent().getStringExtra(NewsFragment.MSGCONTENT),AMessage.class);
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        params = msg.content.split("#");
        initView();
    }

    private void initView(){
        joinGroupResultBackImageView = (ImageView)findViewById(R.id.join_group_result_back_imageview);
        joinGroupResultBackImageView.setOnClickListener(this);
        joinGroupResultHeadImageView = (ImageView)findViewById(R.id.join_group_result_head_imageview);
        joinGroupResultContentTextView = (TextView)findViewById(R.id.join_group_result_textview);
        joinGroupResultGroupNameTextView = (TextView)findViewById(R.id.join_group_result_group_name_textview);

        //设置群头像
        joinGroupResultHeadImageView.setImageResource(R.drawable.ic_toolbar_face);
        //设置群名
        joinGroupResultGroupNameTextView.setText(params[0]);
        //设置处理结果
        if("1".equals(params[1])){
            joinGroupResultContentTextView.setText("你已经是群成员了");
        }else{
            joinGroupResultContentTextView.setText("你的请求被拒绝了");
        }




    }


    @Override
    public void onClick(View v) {
        finish();
    }
}
