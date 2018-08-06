package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * Created by bowen on 2017/11/17.
 */

public class MakeGroupActivity extends AppCompatActivity implements View.OnClickListener{

    public static String MAKE_GROUP_GROUP_NUMBER_LABEL = "lh.henu.edu.cn.locationattendance.activity.MakeGroupActivity.goup.number";
    private TextInputLayout groupNameLayout;
    private TextInputEditText groupNameEdit;
    private Button submitButton;
    private Button cancelButton;
    private QQConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);
        initView();
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.bindContext(this);
            conn.addOnMessageListener(makeGroupOnMessageListener);
        }

    }
    //初始化界面
    private void initView(){
        groupNameEdit = (TextInputEditText)findViewById(R.id.makeGroupName);
        groupNameLayout = (TextInputLayout)findViewById(R.id.makeGroupNmaeLayout);
        submitButton = (Button)findViewById(R.id.make_group_submit);
        submitButton.setOnClickListener(this);
        cancelButton = (Button)findViewById(R.id.make_group_cancle);
        cancelButton.setOnClickListener(this);
        groupNameEdit.addTextChangedListener(new TextWatcher() {
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
    //实现接口
    private onMessageListener makeGroupOnMessageListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_REUQEST_MAKE_GROUP.equals(msg.type)){
                        if(AMessageType.CONTENT_REQUEST_MAKE_GROUP_FAIL.equals(msg.content)){
                            //创建群失败
                            Toast.makeText(MakeGroupActivity.this,"创建失败，再来一次",Toast.LENGTH_SHORT).show();
                        }else{
                            //创建成功,服务器返回创建的群账号,打开修改群资料页面
                            Intent changedGroupInfoIntent = new Intent(MakeGroupActivity.this, ChangedGroupInfoActivity.class);
                            //带着群账号
                            changedGroupInfoIntent.putExtra(MakeGroupActivity.MAKE_GROUP_GROUP_NUMBER_LABEL,msg.content);
                            MakeGroupActivity.this.startActivity(changedGroupInfoIntent);
                            finish();
                        }
                    }
                }
            });
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.make_group_submit:
                final String s = groupNameEdit.getText().toString();
                if(!"".equals(s.toString())&&!s.toString().contains("#")){
                    //封装创建群的消息群主账号+群名
                    AMessage makeGroup = new AMessage();
                    //类型为创建群
                    makeGroup.type = AMessageType.TYPE_REUQEST_MAKE_GROUP;
                    makeGroup.content = ((LocationAttendanceApp)(MakeGroupActivity.this.getApplication())).getUserName()+"#"+s;
                    ((LocationAttendanceApp)(MakeGroupActivity.this.getApplication())).getConn().sendMessage(makeGroup,false,null);

                }
                break;
            case R.id.make_group_cancle:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conn!=null){
            conn.removeOnMessageListener(makeGroupOnMessageListener);
            conn.unBindContext();
        }
    }
}
