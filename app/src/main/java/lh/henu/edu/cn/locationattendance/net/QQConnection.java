package lh.henu.edu.cn.locationattendance.net;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * Created by bowen on 2017/11/24.
 */

public class QQConnection {
    /**
     * 心跳频率
     */
    private static final long HEART_BEAT_RATE = 600 * 1000;
    private WeakReference<Socket> mSocket;
    private long sendTime = 0L;
    // 为了心跳机制的handler
    private Handler mHandler = new Handler();
    private String host = "192.168.1.245";
    private int port = 8090;
    private DataInputStream reader;
    private DataOutputStream writer;
    private WaitThread waitThread;
    public boolean isWaiting;
    private Context context;
    private String userName = "";//账号
    private String passWord = "";//密码（MD5加密后的）

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    /**
     * 心跳任务，不断重复调用自己
     */
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                boolean isSuccess = sendMsg();//就发送一个心跳信息过去 如果发送失败，就重新初始化一个socket
                if (!isSuccess) {
                    //发送失败，释放上一个socket连接的资源，重新连接，并提示网络断开
                    mHandler.removeCallbacks(heartBeatRunnable);
                    releaseLastSocket(mSocket);
                    new InitSocketThread().start();
                    mHandler.postDelayed(this, HEART_BEAT_RATE);
                }else{
                    //发送成功，准备下一次调用线程
                    mHandler.postDelayed(this, HEART_BEAT_RATE);
                }
            }

        }
    };




    /**
     * 心跳机制判断出socket已经断开后，就销毁连接方便重新创建连接
     *
     * @param mSocket
     */
    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    writer.close();
                    reader.close();
                    sk.close();
                }
                isWaiting = false;
                sk = null;
                writer = null;
                reader = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void bindContext(Context context){
        this.context = context;
    }
    //在每个界面销毁时解绑handler
    public void unBindContext(){
        this.context = null;
    }

    //与服务器连接
    private void inConnect(){
        Log.i("ttss", "inConnect: "+"重连");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //连接失败时，就2秒连接一次
                do{
                    //释放上次连接资源
                    if(mSocket!=null&&mSocket.get()!=null){
                        releaseLastSocket(mSocket);
                    }
                    //启动初始化socket线程
                    new InitSocketThread().start();
                    //创建连接的时候开启接收消息的线程
                    isWaiting = true;
                    waitThread = new WaitThread();
                    waitThread.start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    inLogin();
                }while(mSocket!=null&&mSocket.get()!=null&&!sendMsg());

            }
        }).start();
    }

    /**
     * 创建与服务器之间的连接
     */

    public void connect(){
        inConnect();
    }




    //断线重连后登录
    public void inLogin(){
        if(!"".equals(userName)&&!"".equals(passWord)){
            AMessage loginMsg = new AMessage();
            loginMsg.type = AMessageType.TYPE_LOGIN;
            loginMsg.content = userName+"#"+passWord;
            inSendMessage(loginMsg,false,null);
        }

    }

    /**
     * 启动心跳机制
     */

    public void runHeart(){
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//初始化成功后，就准备发送心跳包
    }

    /**
     * 断开与服务器的连接
     */
    public void disConnect() {
        //关闭连接
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (!sk.isClosed()) {
                    writer.close();
                    reader.close();
                    sk.close();
                }
                isWaiting = false;
                sk = null;
                writer = null;
                reader = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //发送消息


    private void inSendMessage(final AMessage msg,final boolean isShowResult,final Context context){
        ThreadUtils.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Gson g = new Gson();
                try {
                    writer.writeUTF(g.toJson(msg));
                    if(isShowResult&&context!=null){
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"已发送给服务器",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i("ttss", "run: "+"报错"+e.getMessage());
                    if(isShowResult&&context!=null){
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,"网络故障，请稍后重试",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //重新连接
                    Log.i("ttss", "run: "+"报错");
                    inConnect();
                }
            }
        });
    }


    /**
     *发送消息
     * @param msg 发送的消息
     * @param isShowResult 是否提示发送结果
     * @param
     */
    public void sendMessage(final AMessage msg,final boolean isShowResult,final Context context){
        inSendMessage(msg,isShowResult,context);
    }

    //向服务器发送心跳消息
    public boolean sendMsg() {
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        final Socket soc = mSocket.get();
        if (!soc.isClosed() && !soc.isOutputShutdown()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AMessage msg = new AMessage();
                        //消息类型为心跳
                        msg.type = AMessageType.TYPE_IS_CONNECT;
                        msg.content = System.currentTimeMillis()+"";
                        Gson g = new Gson();
                        if(writer!=null){
                            writer.writeUTF(g.toJson(msg));
                        }
                    } catch (IOException e) {

                    }
                }
            }).start();
            sendTime = System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
        } else {
            return false;
        }
        return true;
    }

    //监听接口集合
    private List<onMessageListener> listeners = new ArrayList<onMessageListener>();

    //添加监听
    public void addOnMessageListener(onMessageListener listener) {
        listeners.add(listener);
    }

    //移除监听
    public void removeOnMessageListener(onMessageListener listener) {
        listeners.remove(listener);
    }

    //消息的监听接口，当消息来的时候就调用onReceive（我的理解就是为Activity安装监听，Activity实现onReceive接口，来实现对消息的处理。）
    public static interface onMessageListener {
        public void onReceive(AMessage msg);
    }
    /**
     * 等待线程，接收消息
     */
    private class WaitThread extends Thread {

        @Override
        public void run() {
            while (isWaiting) {

                try {
                    //来自服务器的消息
                    if(reader!=null){
                        String jsonString = reader.readUTF();
                        System.out.println(jsonString);
                        Gson gson = new Gson();
                        //将消息转化为java对象
                        AMessage msg = gson.fromJson(jsonString, AMessage.class);
                        //把消息发送给监听接口
                        for (onMessageListener listener : listeners) {
                            listener.onReceive(msg);
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();

                }


            }
        }
    }
    private void initSocket() {//初始化Socket
        try {
            Socket so = new Socket(host, port);
            writer = new DataOutputStream(so.getOutputStream());
            reader = new DataInputStream(so.getInputStream());
            mSocket = new WeakReference<Socket>(so);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //初始化socket的线程
    private class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }
}
