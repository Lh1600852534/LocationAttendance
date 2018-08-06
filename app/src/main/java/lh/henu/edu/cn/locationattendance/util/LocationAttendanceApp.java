package lh.henu.edu.cn.locationattendance.util;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.NetReceiver;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.sign.XLSHelper;

/**
 * Created by bowen on 2017/10/25.
 */

public class LocationAttendanceApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        //监听网络
        initNetReceiver();
        //百度地图初始化（必须的）
        SDKInitializer.initialize(this);
        //初始化数据库
        DBAdapter.setContext(this);
        DBAdapter.getDBAdapater();
        //建立存储文件目录
        XLSHelper.mkDirs();

    }

    //监听网络
    private void initNetReceiver(){
        if(conn==null){
            conn = new QQConnection();
            this.setConn(conn);
        }
        NetReceiver netReceiver = new NetReceiver(conn);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver,filter);
    }

    public NewsFragment.NewsRecyclerViewAdapter getNewsRecyclerViewAdapter() {
        return newsRecyclerViewAdapter;
    }

    public void setNewsRecyclerViewAdapter(NewsFragment.NewsRecyclerViewAdapter newsRecyclerViewAdapter) {
        this.newsRecyclerViewAdapter = newsRecyclerViewAdapter;
    }
    //消息recyclerview的适配器
    private NewsFragment.NewsRecyclerViewAdapter newsRecyclerViewAdapter;

    //账号
    private String userName = "";
    //与服务器的连接
    private QQConnection conn;

    //群列表
    private List<List<GroupData>> child;

    public void setChild(List<List<GroupData>> child) {
        this.child = child;
    }

    public List<List<GroupData>> getChild() {

        return child;
    }



    //登陆时服务器返回的群信息
    private String groupListString = "";

    //消息列表
    private List<AMessage> newsList = new ArrayList<>();
    //获取消息列表
    public List<AMessage> getNewsList() {
        return newsList;
    }
    //设置消息列表
    public void setNewsList(List<AMessage> newsList) {
        this.newsList = newsList;
    }

    public String getGroupListString() {
        return groupListString;
    }


    public void setGroupListString(String groupListString) {

        this.groupListString = groupListString;
    }

    public QQConnection getConn() {
        return conn;
    }

    public void setConn(QQConnection conn) {
        this.conn = conn;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
