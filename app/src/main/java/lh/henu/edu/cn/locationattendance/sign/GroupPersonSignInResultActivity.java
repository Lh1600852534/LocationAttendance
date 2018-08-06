package lh.henu.edu.cn.locationattendance.sign;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * 该activity显示群成员
 */
public class GroupPersonSignInResultActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView groupPersonBackImageView;//返回图标
    private RecyclerView groupPersonListRecyclerView;//成员列表
    private GroupPersonSignInResultAdapter recyclerAdapter;//recycler的adapter
    private Spinner timeSpinner;//发起时间
    private QQConnection conn;
    private SignInDataList signInDataList;//发起签到数据list
    private List<SignInData> recyclerSignInDataList;//recycler的list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_person_sign_in_result);
        //把群号发给服务器，向服务器要该群的所有签到记录
        conn = ((LocationAttendanceApp)getApplication()).getConn();
        if(conn!=null){
            conn.addOnMessageListener(signInResultListener);
            AMessage getSignInResult = new AMessage();
            getSignInResult.type = AMessageType.TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT;
            getSignInResult.content = getIntent().getStringExtra(IndexSignInActivity.GROUPID);
            conn.sendMessage(getSignInResult,false,null);
        }
        initView();
    }
    //接收服务器签到数据,如果有签到记录，就把content置为SignInDataList的json字符串
    private onMessageListener signInResultListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInUiThread(new Runnable() {
                @Override
                public void run() {
                     if(AMessageType.TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT.equals(msg.type)){
                         Gson g = new Gson();
                         signInDataList = g.fromJson(msg.content,SignInDataList.class);
                     }
                }
            });
        }
    };

    //初始化界面
    private void initView(){
        groupPersonBackImageView = (ImageView)findViewById(R.id.group_person_sign_in_result_back_imageview);
        groupPersonBackImageView.setOnClickListener(this);
        groupPersonListRecyclerView = (RecyclerView)findViewById(R.id.group_person_sign_in_result_recyclerview);
        groupPersonListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //初始化spinner
        timeSpinner = (Spinner)findViewById(R.id.group_person_sign_in_result_time_spinner);
        if(getMillsTime(signInDataList.signInDataList).size()==0){
            ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,getStringTime(getMillsTime(signInDataList.signInDataList)));
            timeSpinner.setAdapter(arrayAdapter);
            timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    recyclerSignInDataList = findByTime(signInDataList.signInDataList,getMillsTime(signInDataList.signInDataList).get(position)+"");
                    recyclerAdapter = new GroupPersonSignInResultAdapter(recyclerSignInDataList);
                    groupPersonListRecyclerView.setAdapter(recyclerAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }else{
            ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"没有发起签到活动"});
            timeSpinner.setAdapter(arrayAdapter);

        }
        timeSpinner.setSelection(0);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.group_person_sign_in_result_back_imageview:
                finish();
                break;
            default:
                break;
        }
    }
    //继承viewholder
    private class GroupPersonSignInResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SignInData data;
        private ImageView headImage;//头像
        private TextView realNameTextView;//昵称
        private TextView resultTextView;//签到结果
        public GroupPersonSignInResultViewHolder(View itemView) {
            super(itemView);
            headImage = (ImageView)itemView.findViewById(R.id.recyclerview_group_person_sign_in_result_item_head_image_imageview);
            realNameTextView = (TextView)itemView.findViewById(R.id.recyclerview_group_person_sign_in_result_item_real_name_textview);
            resultTextView = (TextView)itemView.findViewById(R.id.recyclerview_group_person_sign_in_result_item_sign_in_result_textview);
            itemView.setOnClickListener(this);
        }
        //绑定视图
        public void onBindView(SignInData data){
            this.data = data;
            realNameTextView.setText(data.receiver);
            resultTextView.setText(data.result);
        }

        @Override
        public void onClick(View v) {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sb.append("发起时间:"+dateFormat.format(new Date(Long.parseLong(data.time)))+"\n");
            sb.append("发起人:"+data.orijinator+"\n");
            sb.append("签到人:"+data.receiver+"\n");
            sb.append("纬度:"+data.rLatitude+"\n");
            sb.append("经度:"+data.rLongitude+"\n");
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupPersonSignInResultActivity.this);
            builder.setTitle("签到详情")
                    .setMessage(sb.toString())
                    .setPositiveButton("确定",null);
            builder.create().show();

        }
    }

    private class GroupPersonSignInResultAdapter extends RecyclerView.Adapter<GroupPersonSignInResultViewHolder>{

        private List<SignInData> adapterSignInDataList;
        public GroupPersonSignInResultAdapter(List<SignInData> list){
            adapterSignInDataList = list;
        }
        @Override
        public GroupPersonSignInResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view  = LayoutInflater.from(GroupPersonSignInResultActivity.this).inflate(R.layout.recyclerview_group_person_sign_in_result_item,parent,false);
            return new GroupPersonSignInResultViewHolder(view);

        }

        @Override
        public void onBindViewHolder(GroupPersonSignInResultViewHolder holder, int position) {
            holder.onBindView(adapterSignInDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return adapterSignInDataList.size();
        }
    }
    //获得发起签到时间（毫秒数）
    public static List<Long> getMillsTime(List<SignInData> dataList){
        List<Long> list = new ArrayList<>();
        //将所有发起签到时间取出来
        for(int i=0;i<dataList.size();i++){
            if(!list.contains(Long.parseLong(dataList.get(i).time))){
                list.add(Long.parseLong(dataList.get(i).time));
            }
        }
        Collections.sort(list, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return (int)(o2-o1);
            }
        });
        return list;
    }

    //获得发起签到时间（日期格式）
    public static List<String> getStringTime(List<Long> millsTime){
        List<String> list = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for(int i=0;i<millsTime.size();i++){
            list.add(dateFormat.format(new Date(millsTime.get(i))));
        }
        return list;
    }

    //获得特定发起签到时间的签到结果time为毫秒数
    public static List<SignInData> findByTime(List<SignInData> dataList,String time){
        List<SignInData> list = new ArrayList<>();
        for(int i=0;i<dataList.size();i++){
            if(dataList.get(i).time.equals(time)){
                list.add(dataList.get(i));
            }
        }

        return list;
    }

}
