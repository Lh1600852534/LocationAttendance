package lh.henu.edu.cn.locationattendance.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.activity.LoginActivity;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.sign.ReceiveMessageService;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * Created by bowen on 2017/11/12.
 */

public class ChangeSettingsFragment extends Fragment implements View.OnClickListener {
    private QQConnection conn;
    private View changeSettingsFragmentView;
    private TextInputEditText realNameText;
    private TextInputLayout realNameLayout;
    private ImageButton imageButton;
    private Button submitButton;
    private Button exitButton;//退出系统按钮
    //fragment初始化视图

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        changeSettingsFragmentView = inflater.inflate(R.layout.activity_change_settings,container,false);
        initView();
        //获得连接
        conn = ((LocationAttendanceApp) (getActivity().getApplication())).getConn();
        if(conn!=null){
            //添加监听
            conn.addOnMessageListener(changeSettingsOnMessageListener);
            conn.bindContext(getContext());
        }
        //获得账号
        String username = ((LocationAttendanceApp)(getActivity().getApplication())).getUserName();
        //请求获得用户信息的消息
        AMessage requestUserInfoMsg = new AMessage();
        requestUserInfoMsg.type = AMessageType.TYPE_REQUEST_USER_INFO;
        requestUserInfoMsg.content = username;
        //将消息发送给服务器
        conn.sendMessage(requestUserInfoMsg,false,null);
        return changeSettingsFragmentView;
    }

    //设置控件
    private void initView(){
        realNameText = (TextInputEditText)changeSettingsFragmentView.findViewById(R.id.change_realName_text);
        realNameLayout = (TextInputLayout)changeSettingsFragmentView.findViewById(R.id.change_realName_layout);
        realNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //包含非法字符
                if(s.toString().contains("#"))
                {
                    realNameLayout.setError("包含非法字符'#'");
                    return;
                }
                //输入为空
                if("".equals(s.toString())){
                    realNameLayout.setError("输入不能为空");
                    return;
                }
                //输入合法，将错误信息关闭
                if(!s.toString().contains("#")&&!"".equals(s.toString())){
                    realNameLayout.setErrorEnabled(false);
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        submitButton = (Button)changeSettingsFragmentView.findViewById(R.id.change_submit);
        submitButton.setOnClickListener(this);

        exitButton = (Button)changeSettingsFragmentView.findViewById(R.id.change_settings_exit_button);
        exitButton.setOnClickListener(this);

    }

    //实现接口
    private QQConnection.onMessageListener changeSettingsOnMessageListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            //启动线程，接收消息，更新界面
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    //服务器发来的消息类型是请求获得用户信息
                    if(AMessageType.TYPE_REQUEST_USER_INFO.equals(msg.type)){
                        if(AMessageType.CONTENT_REQUEST_USER_INFO_FAIL.equals(msg.content)){

                        }else{
                            //请求用户信息成功(图片还未实现)
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    realNameText.setText(msg.content);
                                }
                            });

                            //imageButton.setImageResource(R.drawable.ic_toolbar_face);
                        }
                    }
                    //服务器发来的消息类型是请求修改用户信息
                    if(AMessageType.TYPE_REQUEST_USER_INFO_CHANGE.equals(msg.type)){
                        if(AMessageType.CONTENT_REQUEST_USER_INFO_CHANGE_SUCCESS.equals(msg.content)){
                            //修改信息成功
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"修改成功",Toast.LENGTH_SHORT).show();

                                }
                            });

                        }else{
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"修改失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
        }
    };
    //移除接口
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(conn!=null){
            conn.removeOnMessageListener(changeSettingsOnMessageListener);
            conn.unBindContext();
        }

    }

    //按钮点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_submit:
                //启动线程发送消息
                //发送给服务器的消息username+#+realName+#+imageUrl（图片未实现）
                String username = ((LocationAttendanceApp)(getActivity().getApplication())).getUserName();
                String realname = realNameText.getText().toString();
                //发送给服务器的消息，请求修改信息
                AMessage requestUserInfoChange = new AMessage();
                requestUserInfoChange.type = AMessageType.TYPE_REQUEST_USER_INFO_CHANGE;
                requestUserInfoChange.content = username+"#"+realname;
                //发送消息
                conn.sendMessage(requestUserInfoChange,false,null);
                break;
            case R.id.change_settings_exit_button:
                //停止接收消息服务
                AMessage stopMessage = new AMessage();
                stopMessage.type = AMessageType.TYPE_OUT;
                conn.sendMessage(stopMessage,false,null);
                //开启登陆界面
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
                //关闭接收消息服务
                Intent stopIntent = new Intent(getActivity(), ReceiveMessageService.class);
                getActivity().stopService(stopIntent);
                //关闭
                getActivity().finish();
                break;
            default:
                break;
        }

    }
}
