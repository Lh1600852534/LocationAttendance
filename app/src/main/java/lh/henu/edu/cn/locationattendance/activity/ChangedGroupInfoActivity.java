package lh.henu.edu.cn.locationattendance.activity;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class ChangedGroupInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private Button submitButton;
    private ImageView backImageView;
    private TextView groupNumberTextView;
    private TextInputLayout groupNameLayout;
    private TextInputEditText groupNameText;
    private String[] groupString;//群号，群名
    public static String CHANGED_GROUP_INFO_GROUP_NAME_LABEL = "lh.henu.edu.cn.locationattendance.activity.ChangedGroupInfoActivity.group.name";
    private QQConnection conn;//与服务器的连接
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changed_group_info);
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        groupString = getIntent().getStringExtra(MakeGroupActivity.MAKE_GROUP_GROUP_NUMBER_LABEL).split("#");
        initView();
        if(conn!=null){
            conn.bindContext(this);
            //添加监听器
            conn.addOnMessageListener(changedGroupInfoMessageListener);


        }
    }

    private void initView(){
        submitButton = (Button)findViewById(R.id.changed_group_info_submit_button);
        submitButton.setOnClickListener(this);
        backImageView = (ImageView)findViewById(R.id.changed_group_info_back_imageview);
        backImageView.setOnClickListener(this);
        groupNumberTextView = (TextView)findViewById(R.id.changed_group_info_group_number_textview);
        groupNumberTextView.setText(groupString[0]);
        groupNameLayout = (TextInputLayout)findViewById(R.id.changed_group_info_group_name_layout);
        groupNameText = (TextInputEditText)findViewById(R.id.changed_group_info_group_name_text);
        groupNameText.setText(groupString[1]);
        groupNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if("".equals(s.toString())){
                    groupNameLayout.setError("群名不能为空");
                    return;
                }

                if(s.toString().contains("#")){
                    groupNameLayout.setError("群名不能包含'#'");
                    return;
                }

                if(!"".equals(s.toString())&&!s.toString().contains("#")){
                    groupNameLayout.setErrorEnabled(false);
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
            case R.id.changed_group_info_submit_button:
                //向服务器发送修改群信息的请求,content为群号+群名
                AMessage msg = new AMessage();
                msg.type = AMessageType.TYPE_REQUEST_GROUP_INFO_CHANGE;
                msg.content = groupString[0]+"#"+groupNameText.getText().toString();
                conn.sendMessage(msg,false,null);
                break;
            //点击返回键返回
            case R.id.changed_group_info_back_imageview:
                finish();
                break;
        }
    }
    //实现接口
    private onMessageListener changedGroupInfoMessageListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    //服务器返回的请求群信息,服务器返回的content是groupData的json字符串

                    if(AMessageType.TYPE_REQUEST_GROUP_INFO_CHANGE.equals(msg.type)){
                        if(AMessageType.CONTENT_REQUEST_GROUP_INFO_CHANGE_FAIL.equals(msg.content)){
                            //修改群信息失败
                            Toast.makeText(ChangedGroupInfoActivity.this,"修改失败，网络或服务器故障",Toast.LENGTH_SHORT).show();
                        }else{
                            //修改群信息成功
                            Toast.makeText(ChangedGroupInfoActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
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
            conn.removeOnMessageListener(changedGroupInfoMessageListener);
            conn.unBindContext();
        }

    }
}
