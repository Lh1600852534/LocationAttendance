package lh.henu.edu.cn.locationattendance.sign;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.util.ApplyPermission;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

public class BaiduPersonSignInResultActivity extends AppCompatActivity implements BaiduMap.OnMarkerClickListener{

    private MapView mapView;//百度地图控件
    private BaiduMap baiduMap;//百度地图对象
    private LocationClient locationClient;
    private SignInDataList signInDataList;//服务器发来的全部签到结果
    private QQConnection conn;
    private ImageView backImageView;//返回图标
    private boolean isFirst = true;//是否第一次启动地图
    public static final String SIGNINDATAINDEX = "lh.henu.edu.cn.locationattendance.sign.BaiduPersonSignInResultActivity.index";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_person_sign_in_result);
        //把群号发给服务器，向服务器要该群的所有签到记录
        conn = ((LocationAttendanceApp)getApplication()).getConn();

        initView();
    }

    //接收服务器签到数据,如果有签到记录，就把content置为SignInDataList的json字符串
    private QQConnection.onMessageListener signInResultListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_GET_PERSON_SIGN_IN_RESULT.equals(msg.type)){
                        Gson g = new Gson();
                        signInDataList = g.fromJson(msg.content,SignInDataList.class);
                        Log.d("ttss","fuwuqi1");
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("ttss","绑定");
                                onBindMarker();
                            }
                        });

                    }
                }
            });
        }
    };

    //百度地图监听器
    private BDAbstractLocationListener bdAbstractLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(isFirst){
                //纬度，经度
                LatLng latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                //移动到该位置
                if(baiduMap!=null){
                    baiduMap.animateMapStatus(mapStatusUpdate);
                }else{
                    return;
                }
            }
            locationClient.stop();
        }
    };


    //初始化界面
    private void initView(){
        backImageView = (ImageView)findViewById(R.id.baidu_person_sign_in_result_back_imageview);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mapView = (MapView)findViewById(R.id.baidu_person_sign_in_result_map_mapview);
        baiduMap = mapView.getMap();
        //设置百度地图中心位置可变
        baiduMap.setMyLocationEnabled(true);
        //设置标记点击事件
        baiduMap.setOnMarkerClickListener(this);
        //启动地图，获取位置
        locationClient = new LocationClient(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            locationClient.registerLocationListener(bdAbstractLocationListener);
            locationClient.start();
        }else{
            ApplyPermission.applyPermissions(this);
        }


        if(conn!=null){
            conn.addOnMessageListener(signInResultListener);
            AMessage getSignInResult = new AMessage();
            getSignInResult.type = AMessageType.TYPE_GET_PERSON_SIGN_IN_RESULT;
            //content为群号
            getSignInResult.content = getIntent().getStringExtra(IndexSignInActivity.GROUPID);
            conn.sendMessage(getSignInResult,true,this);
        }

    }
    //将数据绑定到图标标记上
    private void onBindMarker(){
        baiduMap.clear();
        if(signInDataList!=null&&signInDataList.signInDataList!=null){
            for(int i=0;i<signInDataList.signInDataList.size();i++){
                SignInData signInData = signInDataList.signInDataList.get(i);
                if("1".equals(signInData.done)){
                    SignInData data = signInDataList.signInDataList.get(i);
                    //纬度，经度
                    LatLng point = new LatLng(Double.parseDouble(data.rLatitude),Double.parseDouble(data.rLongitude));
                    //图标
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
                    //附加信息
                    Bundle b = new Bundle();
                    b.putInt(SIGNINDATAINDEX,i);
                    OverlayOptions marker = new MarkerOptions()
                            .icon(bitmapDescriptor)
                            .position(point)
                            .extraInfo(b);
                    baiduMap.addOverlay(marker);
                }
            }
        }
    }
    //点击标记显示签到详情的对话框
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(signInDataList!=null&&signInDataList.signInDataList!=null){
            SignInData data = signInDataList.signInDataList.get(marker.getExtraInfo().getInt(SIGNINDATAINDEX));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date(Long.parseLong(data.time));
            String formatDate = dateFormat.format(date);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if("1".equals(data.result)){
                builder.setTitle("签到详情")
                        .setMessage("发起时间：\t"+formatDate+"\n"+"签到人：\t"+data.receiver+"\n"+"签到结果：\t签到成功");
            }else{
                builder.setTitle("签到详情")
                        .setMessage("发起时间：\t"+formatDate+"\n"+"签到人：\t"+data.receiver+"\n"+"签到结果：\t签到失败");
            }

            builder.create().show();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
