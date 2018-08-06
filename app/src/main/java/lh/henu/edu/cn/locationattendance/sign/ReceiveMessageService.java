package lh.henu.edu.cn.locationattendance.sign;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Adapter;

import java.util.List;

import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.NetReceiver;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.net.QQConnection.onMessageListener;
import lh.henu.edu.cn.locationattendance.util.DBAdapter;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class ReceiveMessageService extends Service {

    private LocationAttendanceApp locationAttendanceApp;
    private NetReceiver netReceiver;
    private QQConnection conn;
    private List<AMessage> newsList;
    private NewsFragment.NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    @Override
    public void onCreate() {
        super.onCreate();
        locationAttendanceApp  = (LocationAttendanceApp) getApplicationContext();
        if(locationAttendanceApp.getConn()!=null){
            conn = locationAttendanceApp.getConn();
            locationAttendanceApp.getConn().addOnMessageListener(receiveListener);
        }

    }



    public ReceiveMessageService() {


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private onMessageListener receiveListener = new onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    newsList = locationAttendanceApp.getNewsList();
                    newsRecyclerViewAdapter = locationAttendanceApp.getNewsRecyclerViewAdapter();
                    synchronized (DBAdapter.getDBAdapater()){
                        if(AMessageType.TYPE_REQUEST_JOIN_GROUP_TO.equals(msg.type)){
                            //加别人群的结果
                            if(!"-1".equals(msg.content)){
                                DBAdapter.getDBAdapater().save(msg);
                                if(newsList!=null&&newsRecyclerViewAdapter!=null){
                                    newsList.add(msg);
                                    ThreadUtils.runInUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            newsRecyclerViewAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                        if(AMessageType.TYPE_REQUEST_JOIN_GROUP_DEAL.equals(msg.type)){
                            //处理别人请求
                            if(!DBAdapter.getDBAdapater().exist(msg)){
                                DBAdapter.getDBAdapater().save(msg);
                                if(newsList!=null&&newsRecyclerViewAdapter!=null){
                                    newsList.add(msg);
                                    ThreadUtils.runInUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            newsRecyclerViewAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                            }

                        }
                        if(AMessageType.TYPE_REQUEST_SIGN_IN.equals(msg.type)){
                            //要求签到
                            DBAdapter.getDBAdapater().save(msg);
                            if(newsList!=null&&newsRecyclerViewAdapter!=null){
                                newsList.add(msg);
                                ThreadUtils.runInUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        newsRecyclerViewAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    };


}
