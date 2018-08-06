package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class JoinGroupActivity extends AppCompatActivity {

    public static String GROUP_INFO_LABLE = "lh.henu.edu.cn.locationattendance.activity.JoinGroupActivity.GroupInfo";
    private EditText searchEditText;
    private LinearLayout searchLinearLayout;
    private TextView searchTextView;
    private TextView noFoundTextView;
    private ImageView joinGroupBack;
    private QQConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        initView();
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.bindContext(this);
            conn.addOnMessageListener(joinGroupOnMessageListener);
        }
    }

    //初始化视图
    private void initView(){
        searchEditText = (EditText) findViewById(R.id.search_edittext);
        searchLinearLayout = (LinearLayout)findViewById(R.id.search_linearlayout);
        searchTextView = (TextView)findViewById(R.id.search_textview);
        joinGroupBack = (ImageView)findViewById(R.id.join_group_back);
        noFoundTextView = (TextView)findViewById(R.id.no_found_textview);
        //点击返回按钮
        joinGroupBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //当输入信息时，使searchLinearLayout可见，并对searchTextView设置
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noFoundTextView.setVisibility(View.INVISIBLE);
                if(!"".equals(s.toString())){
                    //当输入不为空使searchLinearLayout可见
                    searchLinearLayout.setVisibility(View.VISIBLE);
                    //设置searchTextView
                    searchTextView.setText(s.toString());
                }else{
                    //当输入为空，使searchLinearLayout不可见
                    searchLinearLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //点击搜索，将群号发送给服务器
        searchLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = searchEditText.getText().toString();
                if(!"".equals(s)){
                    final AMessage message = new AMessage();
                    //搜索类型
                    message.type = AMessageType.TYPE_SEARCH;
                    //搜索内容
                    message.content = s;
                    //启动线程，发送消息
                    conn.sendMessage(message,false,null);
                }
            }
        });
    }
    //服务器发来消息,如果找到了群，就把群信息发过来，否者就把content="0",如果是群成员或群主就把content置为-1
    private onMessageListener joinGroupOnMessageListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_SEARCH.equals(msg.type)){
                        switch (msg.content){
                            case "0":
                                //搜索失败
                                noFoundTextView.setVisibility(View.VISIBLE);
                                Toast.makeText(JoinGroupActivity.this,"该群不存在",Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                //搜索成功
                                //content为groupName+群主昵称
                                Intent groupInfoIntent = new Intent(JoinGroupActivity.this,GroupInfoActivity.class);
                                groupInfoIntent.putExtra(JoinGroupActivity.GROUP_INFO_LABLE,msg.content);
                                startActivity(groupInfoIntent);
                                break;
                        }
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            conn.removeOnMessageListener(joinGroupOnMessageListener);
            conn.unBindContext();
        }

    }
}
