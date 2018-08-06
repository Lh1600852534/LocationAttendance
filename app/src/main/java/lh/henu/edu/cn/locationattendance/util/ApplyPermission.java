package lh.henu.edu.cn.locationattendance.util;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bowen on 2017/12/14.
 */

public class ApplyPermission {
    public static List<String> permissionList;//请求权限集合
    public final static int requestCode = 3000;//请求码

    public static void applyPermissions(Activity activity){
        if(permissionList==null){
            permissionList = new ArrayList<>();
            permissionList.add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);//写权限
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE);//读取网络连接权限
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);//gps权限
            permissionList.add(Manifest.permission.READ_CONTACTS);
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
            permissionList.add(Manifest.permission.RECEIVE_SMS);
            permissionList.add(Manifest.permission.RECORD_AUDIO);
            permissionList.add(Manifest.permission.CAMERA);


        }
        //移除申请过的权限
        for(int i=0;i<permissionList.size();i++){
            int hasPermission = ContextCompat.checkSelfPermission(activity,permissionList.get(i));
            if(hasPermission== PackageManager.PERMISSION_GRANTED){
                permissionList.remove(permissionList.get(i));
            }
        }

        //申请权限
        if(permissionList.size()!=0){
            ActivityCompat.requestPermissions(activity,permissionList.toArray(new String[0]),requestCode);
        }

    }

}
