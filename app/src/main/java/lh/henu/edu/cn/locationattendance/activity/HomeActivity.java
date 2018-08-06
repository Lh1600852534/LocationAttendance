package lh.henu.edu.cn.locationattendance.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.TabsDataGenerator;
import lh.henu.edu.cn.locationattendance.fragments.ChangeSettingsFragment;
import lh.henu.edu.cn.locationattendance.fragments.GroupFragments;
import lh.henu.edu.cn.locationattendance.fragments.NewsFragment;
import lh.henu.edu.cn.locationattendance.sign.ReceiveMessageService;

public class HomeActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener{

    private BottomNavigationBar tabsNavigationBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //启动接收消息服务
        Intent receiveIntent = new Intent(this, ReceiveMessageService.class);
        startService(receiveIntent);
        initView();

    }
    //初始化视图（添加tabs，并将tabs与fragment关联）
    private void initView(){
        //设置第一个fragment
        getSupportFragmentManager().beginTransaction().add(R.id.home_container_frame_layout,new NewsFragment()).commit();
        tabsNavigationBar = (BottomNavigationBar)findViewById(R.id.home_tabs_bottom_navigation_bar);
        //tabsNavigationBar设置监听事件
        tabsNavigationBar.setTabSelectedListener(this);
        //设置第一个被选中的tab
        tabsNavigationBar.setFirstSelectedPosition(0);
        //添加tabs
        for(int i=0;i< TabsDataGenerator.tabsText.length;i++){
            tabsNavigationBar.addItem(
                    new BottomNavigationItem(TabsDataGenerator.imageSelected[i],TabsDataGenerator.tabsText[i])
                    .setInactiveIconResource(TabsDataGenerator.image[i])
                    .setActiveColorResource(R.color.colorPrimary)
                    .setInActiveColorResource(android.R.color.darker_gray)
            );
        }
        tabsNavigationBar.initialise();

    }

    @Override
    public void onTabSelected(int position) {
        //选中时改变fragment
        Fragment f = null;
        switch(position){
            case 0:
                f = new NewsFragment();
                break;
            case 1:
                f = new GroupFragments();
                break;
            case 2:
                f = new ChangeSettingsFragment();
                break;
        }
        if(f!=null){
            getSupportFragmentManager().beginTransaction().replace(R.id.home_container_frame_layout,f).commit();
        }
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
