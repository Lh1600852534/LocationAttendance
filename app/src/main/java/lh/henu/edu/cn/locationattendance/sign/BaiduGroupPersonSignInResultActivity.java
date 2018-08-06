package lh.henu.edu.cn.locationattendance.sign;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
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
import lh.henu.edu.cn.locationattendance.util.ApplyPermission;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * 用百度地图显示签到结果
 */
public class BaiduGroupPersonSignInResultActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,BaiduMap.OnMarkerClickListener{

    private SignInDataList signInDataList;//服务器给的所有签到信息
    private List<Long> millsDateList;//毫秒数list
    private List<String> formatDateList;//固定格式的日期
    private List<SignInData> dataAtTimeList;//特定日期的签到数据
    private MapView mapView;//地图控件
    private BaiduMap baiduMap;//地图对象
    private LocationClient locationClient;
    private QQConnection conn;
    private Spinner timeSpinner;//选着签到发起时间
    private ImageView backImageView;//返回图标
    public static final String SIGNINDATAINDEX = "lh.henu.edu.cn.locationattendance.sign.BaiduGroupPersonSignInResultActivity.index";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_group_person_sign_in_result);
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
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT.equals(msg.type)){
                        Gson g = new Gson();
                        signInDataList = g.fromJson(msg.content,SignInDataList.class);
                        ThreadUtils.runInUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //初始化签到时间选择框
                                millsDateList = getMillsTime(signInDataList);
                                formatDateList = getStringTime(millsDateList);
                                if(millsDateList.size()!=0){
                                    ArrayAdapter arrayAdapter = new ArrayAdapter(BaiduGroupPersonSignInResultActivity.this,android.R.layout.simple_spinner_dropdown_item,formatDateList);
                                    timeSpinner.setAdapter(arrayAdapter);
                                    timeSpinner.setOnItemSelectedListener(BaiduGroupPersonSignInResultActivity.this);

                                }else{
                                    ArrayAdapter arrayAdapter = new ArrayAdapter(BaiduGroupPersonSignInResultActivity.this,android.R.layout.simple_spinner_dropdown_item,new String[]{"没有发起签到活动"});
                                    timeSpinner.setAdapter(arrayAdapter);

                                }
                                timeSpinner.setSelection(0);
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
            //纬度，经度
            LatLng latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            //移动到该位置
            if(baiduMap!=null){
                baiduMap.animateMapStatus(mapStatusUpdate);
            }else{
                return;
            }
            locationClient.stop();
        }
    };

    //初始化界面
    private void initView(){
        mapView = (MapView)findViewById(R.id.baidu_group_person_sign_in_result_map_mapview);
        timeSpinner = (Spinner)findViewById(R.id.baidu_group_person_sign_in_result_time_spinner);
        baiduMap = mapView.getMap();
        baiduMap.setOnMarkerClickListener(this);
        //返回图标设置
        backImageView = (ImageView)findViewById(R.id.baidu_group_person_sign_in_result_back_imageview);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //设置百度地图中心位置可变
        baiduMap.setMyLocationEnabled(true);
        //启动地图，获取位置
        locationClient = new LocationClient(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            locationClient.registerLocationListener(bdAbstractLocationListener);
            locationClient.start();
        }else{
            ApplyPermission.applyPermissions(this);
        }
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

    //获得发起签到时间（毫秒数）
    public static List<Long> getMillsTime(SignInDataList dataList){
        List<Long> list = new ArrayList<>();
        //将所有发起签到时间取出来
        if(dataList!=null&&dataList.signInDataList!=null){
            for(int i=0;i<dataList.signInDataList.size();i++){
                if(!list.contains(Long.parseLong(dataList.signInDataList.get(i).time))){
                    list.add(Long.parseLong(dataList.signInDataList.get(i).time));
                }
            }
            Collections.sort(list, new Comparator<Long>() {
                @Override
                public int compare(Long o1, Long o2) {
                    return (int)(o2-o1);
                }
            });
        }

        return list;
    }

    //获得发起签到时间（日期格式）
    public static List<String> getStringTime(List<Long> millsTime){
        List<String> list = new ArrayList<>();
        if(millsTime!=null){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for(int i=0;i<millsTime.size();i++){
                list.add(dateFormat.format(new Date(millsTime.get(i))));
            }
        }
        return list;
    }

    //获得特定发起签到时间的签到结果time为毫秒数
    public static List<SignInData> findByTime(SignInDataList dataList,String time){
        List<SignInData> list = new ArrayList<>();
        if(dataList!=null&&dataList.signInDataList!=null&&time!=null&&!"".equals(time)){
            for(int i=0;i<dataList.signInDataList.size();i++){
                if(dataList.signInDataList.get(i).time.equals(time)){
                    list.add(dataList.signInDataList.get(i));
                }
            }
        }

        return list;
    }


    //选着了特定时间，在地图上标注出来
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        baiduMap.clear();//清理标记
        dataAtTimeList = findByTime(signInDataList,millsDateList.get(position).toString());//选出特定发起签到时间的记录
        for(int i=0;i<dataAtTimeList.size();i++){
            SignInData data = dataAtTimeList.get(i);

            if("1".equals(data.done)){
                //纬度，经度
                LatLng point = new LatLng(Double.parseDouble(data.rLatitude),Double.parseDouble(data.rLongitude));
                //图标
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
                //附加信息
                Bundle b = new Bundle();
                b.putInt(SIGNINDATAINDEX,i);
                //标记
                OverlayOptions marker = new MarkerOptions()
                        .icon(bitmapDescriptor)
                        .position(point)
                        .extraInfo(b);
                baiduMap.addOverlay(marker);
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //点击地图标记，显示对话框，显示签到人，签到结果
    @Override
    public boolean onMarkerClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(dataAtTimeList!=null){
            SignInData data = dataAtTimeList.get(marker.getExtraInfo().getInt(SIGNINDATAINDEX));
            if("0".equals(data.result)){
                builder.setTitle("签到详情")
                        .setMessage("签到人："+data.receiver+"\n"+"签到结果：签到失败");
            }else{
                builder.setTitle("签到详情")
                        .setMessage("签到人："+data.receiver+"\n"+"签到结果：签到成功");
            }

            builder.create().show();
        }

        return true;
    }
}
