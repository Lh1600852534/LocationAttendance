package lh.henu.edu.cn.locationattendance.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;

public class NetReceiver extends BroadcastReceiver {
    private QQConnection conn;
    public NetReceiver(){
        super();
    }

    public NetReceiver(QQConnection conn){
        this.conn = conn;
    }
    //网络断开就把socket资源释放掉，网络连接成功就连接服务器
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
       //监听网络连接，wifi和移动数据打开和关闭，以及连接上可用的连接都会监听
        if((ConnectivityManager.TYPE_MOBILE+"").equals(intent.getAction())){
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(info!=null){
                //连接网络成功，并且网络连接可用
                if(NetworkInfo.State.CONNECTED==info.getState()&&info.isAvailable()){
                    if(conn!=null){
                        Log.i("ttss", "onReceive: "+"网络");
                        conn.connect();
                    }
                }else{
                    if(conn!=null){
                        conn.disConnect();
                    }
                }
            }
        }
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态

                if (isConnected) {
                    if(conn!=null){
                        Log.i("ttss", "onReceive: "+"网络");
                        conn.connect();
                    }
                }
            }
        }
    }
}
