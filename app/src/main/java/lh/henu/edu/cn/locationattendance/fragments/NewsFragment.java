package lh.henu.edu.cn.locationattendance.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.activity.DealNewsJoinGroupActivity;
import lh.henu.edu.cn.locationattendance.activity.JoinGroupResultActivity;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.sign.ClientSignInActivity;
import lh.henu.edu.cn.locationattendance.util.DBAdapter;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * Created by bowen on 2017/11/12.
 */

/**
 * 消息fragment用来显示签到消息，请求加入群消息。
 */
public class NewsFragment extends Fragment{
    public List<AMessage> newsList;//测试消息
    private QQConnection conn;//与服务器的连接
    private View view;
    private RecyclerView newsRecyclerView;//消息列表
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;//recyclerview的适配器
    public static final String MSGCONTENT = "lh.henu.edu.cn.locationattendance.fragments.NewsFragment.msg.content";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_news,container,false);

        conn = ((LocationAttendanceApp)(getActivity().getApplication())).getConn();
        if(conn!=null){
            AMessage msg = new AMessage();
            msg.type = AMessageType.TYPE_REQUEST_MESSAGE;
            conn.sendMessage(msg,false,null);
            conn.bindContext(getContext());
        }


        newsList = DBAdapter.getDBAdapater().findAllMessage(((LocationAttendanceApp)(getActivity().getApplication())).getUserName());
        newsRecyclerView = (RecyclerView)view.findViewById(R.id.news_listview);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter();
        newsRecyclerView.setAdapter(newsRecyclerViewAdapter);
        ((LocationAttendanceApp)(getActivity().getApplication())).setNewsRecyclerViewAdapter(newsRecyclerViewAdapter);
        ((LocationAttendanceApp)(getActivity().getApplication())).setNewsList(newsList);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class NewsRecyclerViewHodler extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView headImageView;//消息头像
        public TextView titleTextView;//消息标题（群名，通知信息）
        public TextView contentTextView;//消息内容
        private AMessage msg;
        private Button deleteButton;//删除按钮
        private List<AMessage> msgList = ((LocationAttendanceApp)(getActivity().getApplication())).getNewsList();
        public NewsRecyclerViewHodler(View viewHolder){
            super(viewHolder);
            headImageView = (ImageView)viewHolder.findViewById(R.id.item_recycler_view_news_fragment_head_imageview);
            titleTextView = (TextView)viewHolder.findViewById(R.id.item_recycler_view_news_fragment_title_textview);
            contentTextView = (TextView)viewHolder.findViewById(R.id.item_recycler_view_news_fragment_content_textview);
            deleteButton = (Button)viewHolder.findViewById(R.id.item_recycler_view_news_fragment_delete_button);
            deleteButton.setOnClickListener(this);
            headImageView.setOnClickListener(this);
            titleTextView.setOnClickListener(this);
            contentTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.item_recycler_view_news_fragment_content_textview:
                case R.id.item_recycler_view_news_fragment_head_imageview:
                case R.id.item_recycler_view_news_fragment_title_textview:
                    switch (msg.type){
                        case AMessageType.TYPE_REQUEST_SIGN_IN:
                            //要求签到
                            Intent requestIntent = new Intent(getActivity(), ClientSignInActivity.class);
                            requestIntent.putExtra(NewsFragment.MSGCONTENT,msg.toJson());
                            getActivity().startActivity(requestIntent);
                            break;
                        case AMessageType.TYPE_REQUEST_JOIN_GROUP_TO:
                            //请求加入别的群的结果
                            Intent joinGroupResultIntent = new Intent(getActivity(), JoinGroupResultActivity.class);
                            joinGroupResultIntent.putExtra(NewsFragment.MSGCONTENT,msg.toJson());
                            getActivity().startActivity(joinGroupResultIntent);
                            break;
                        case AMessageType.TYPE_REQUEST_JOIN_GROUP_DEAL:
                            //处理加入群的信息
                            Intent dealNewsIntent = new Intent(getActivity(), DealNewsJoinGroupActivity.class);
                            dealNewsIntent.putExtra(NewsFragment.MSGCONTENT,msg.toJson());
                            getActivity().startActivity(dealNewsIntent);
                            break;

                        default:
                            break;
                    }
                    break;
                case R.id.item_recycler_view_news_fragment_delete_button:
                    DBAdapter.getDBAdapater().delete(msg);
                    newsList.remove(msg);
                    if(newsRecyclerViewAdapter!=null){
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;

            }
        }
        //绑定视图
        public void onBindView(AMessage message){
            msg = message;
            switch (msg.type){
                case AMessageType.TYPE_REQUEST_SIGN_IN:
                    //要求签到
                    //content为群号+发起时间+群主经度+群主纬度+终止时间+群主账号+群昵称+群主昵称
                    String[] paramsSignIn = msg.content.split("#");
                    //消息头像
                    headImageView.setImageResource(R.drawable.ic_toolbar_face);
                    //消息标题
                    titleTextView.setText(paramsSignIn[6]);
                    //消息内容
                    contentTextView.setText(paramsSignIn[7]+"发起签到");
                    break;
                case AMessageType.TYPE_REQUEST_JOIN_GROUP_TO:
                    //请求加入别的群的结果
                    //content为groupname+0/1
                    String[] paramsTo = msg.content.split("#");
                    //消息头像
                    headImageView.setImageResource(R.drawable.ic_toolbar_face);
                    //消息标题
                    titleTextView.setText("群通知");
                    //消息内容
                    contentTextView.setText(paramsTo[0]+"申请结果");
                    break;
                case AMessageType.TYPE_REQUEST_JOIN_GROUP_DEAL:
                    //处理加入群信息
                    //content为username+"#"+groupNumber+"#"+"realName"+"#"+groupname
                    String[] paramsDeal = msg.content.split("#");
                    //消息头像
                    headImageView.setImageResource(R.drawable.ic_toolbar_face);
                    //消息标题
                    titleTextView.setText("群通知");
                    //消息内容
                    contentTextView.setText(paramsDeal[2]+"请求加入"+paramsDeal[3]);
                    break;
            }
        }
    }

    public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewHodler>{



        @Override
        public NewsRecyclerViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(NewsFragment.this.getContext()).inflate(R.layout.item_recycler_view_news_fragment,parent,false);
            return new NewsRecyclerViewHodler(v);
        }

        @Override
        public void onBindViewHolder(NewsRecyclerViewHodler holder, int position) {
            holder.onBindView(newsList.get(position));
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((LocationAttendanceApp)(getActivity().getApplication())).setNewsRecyclerViewAdapter(null);
        ((LocationAttendanceApp)(getActivity().getApplication())).setNewsList(null);
    }
}
