package lh.henu.edu.cn.locationattendance.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.activity.JoinGroupActivity;
import lh.henu.edu.cn.locationattendance.activity.MakeGroupActivity;
import lh.henu.edu.cn.locationattendance.adapter.MyExpandableListViewAdapter;
import lh.henu.edu.cn.locationattendance.domain.AMessageType;
import lh.henu.edu.cn.locationattendance.domain.GroupData;
import lh.henu.edu.cn.locationattendance.domain.GroupDataList;
import lh.henu.edu.cn.locationattendance.net.AMessage;
import lh.henu.edu.cn.locationattendance.net.QQConnection;
import lh.henu.edu.cn.locationattendance.sign.IndexSignInActivity;
import lh.henu.edu.cn.locationattendance.util.LocationAttendanceApp;
import lh.henu.edu.cn.locationattendance.util.ThreadUtils;

/**
 * Created by bowen on 2017/11/12.
 */

public class GroupFragments extends Fragment {
    private View groupFragmentView;
    private Toolbar toolBar;
    private TextView titleTextView;
    private ExpandableListView expandableListView;
    private ImageButton searchButton;
    private String host = "192.168.1.245";
    private int port = 8090;
    private QQConnection conn;
    private String[] groups = {"我创建的群","我加入的群"};//择叠面板的group
    private List<List<GroupData>> child = new ArrayList<>();//择叠面板的child
    public final static String GROUPINDEX = "lh.henu.edu.cn.locationattendance.fragments.GroupFragments.group.index";
    public final static String CHILDINDEX = "lh.henu.edu.cn.locationattendance.fragments.GroupFragments.child.index";
    //fragment初始化视图
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.activity_group,container,false);
        conn = ((LocationAttendanceApp)(getActivity().getApplication())).getConn();
        if(conn!=null)
        {
            conn.bindContext(getContext());
            conn.addOnMessageListener(groupListener);
        }


        //初始化工具栏
        initToolBar();

        return groupFragmentView;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(conn!=null){
            //请求群列表的消息
            AMessage msg = new AMessage();
            msg.type = AMessageType.TYPE_GROUP_LIST;
            msg.content = ((LocationAttendanceApp)(getActivity().getApplication())).getUserName();
            conn.sendMessage(msg,false,null);
        }
    }





    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_list,menu);
    }

    private void initToolBar()//初始化ToolBar
    {

        toolBar = (Toolbar) groupFragmentView.findViewById(R.id.toolBar);
        toolBar.setTitle("");
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolBar);
        //菜单点击事件
        toolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.add_menu_item:
                        //点击创建群
                        Intent makeGroupIntent = new Intent(getActivity(), MakeGroupActivity.class);
                        getActivity().startActivity(makeGroupIntent);
                        break;
                    case R.id.invent_menu_item:
                        //点击加入群
                        Intent joinIntent = new Intent(getActivity(),JoinGroupActivity.class);
                        getActivity().startActivity(joinIntent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        titleTextView = (TextView) groupFragmentView.findViewById(R.id.titleTextView);
        setHasOptionsMenu(true);


    }

    private void initExpandableListView()//初始化折叠列表
    {
        expandableListView = (ExpandableListView) groupFragmentView.findViewById(R.id.expandableListView);
        MyExpandableListViewAdapter myExpandableListViewAdapter = new MyExpandableListViewAdapter(groups,child,getActivity());
        expandableListView.setGroupIndicator(null);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent indexIntent = new Intent(getActivity(), IndexSignInActivity.class);
                indexIntent.putExtra(GROUPINDEX,groupPosition);
                indexIntent.putExtra(CHILDINDEX,childPosition);
                getActivity().startActivity(indexIntent);
                return true;
            }
        });
        //长按是否删除或退出
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String groupIndex = (String)view.getTag(R.id.item_child_goup_index_textview);
                final String groupNumber = (String)view.getTag(R.id.item_child_goup_number_textview);
                if(groupIndex!=null&&groupNumber!=null){
                    if("0".equals(groupIndex)){
                        //我发起的签到群
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("确定删除签到群？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(conn!=null){
                                    AMessage msg = new AMessage();
                                    msg.type = AMessageType.TYPE_DELETE_GROUP;
                                    msg.content = groupNumber;
                                    conn.sendMessage(msg,false,null);
                                }
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.create().show();

                    }else{
                        //我加入的签到群
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("确定退出签到群？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(conn!=null){
                                    AMessage msg = new AMessage();
                                    msg.type = AMessageType.TYPE_OUT_GROUP;
                                    msg.content = groupNumber;
                                    conn.sendMessage(msg,false,null);

                                }
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.create().show();
                    }
                }
                return true;
            }
        });
        expandableListView.setAdapter(myExpandableListViewAdapter);
    }

    //实现监听接口，可以用来刷新，或当加入群和创建群时刷新（未实现）
    private QQConnection.onMessageListener groupListener = new QQConnection.onMessageListener() {
        @Override
        public void onReceive(final AMessage msg) {
            ThreadUtils.runInSubThread(new Runnable() {
                @Override
                public void run() {
                    if(AMessageType.TYPE_GROUP_LIST.equals(msg.type)){
                        if(AMessageType.CONTENT_GROUP_LIST_FAIL.equals(msg.content)){

                        }else{
                            //请求群列表成功
                            LocationAttendanceApp locationAttendanceApp = (LocationAttendanceApp)(getActivity().getApplication());
                            //更新LocationAttendanceApp的GroupListStrinng
                            locationAttendanceApp.setGroupListString(msg.content);
                            //将原来的数据清空
                            child.clear();
                            //更新界面
                            Gson gson = new Gson();
                            //获得群数据
                            GroupDataList newList = gson.fromJson(locationAttendanceApp.getGroupListString(),GroupDataList.class);
                            if(newList!=null){
                                //成功转化为对象后
                                child = splitList(newList.list);
                            }else{
                                //转化对象出错
                                child = new ArrayList<List<GroupData>>();
                                child.add(new ArrayList<GroupData>());
                                child.add(new ArrayList<GroupData>());
                            }
                            locationAttendanceApp.setChild(child);
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initExpandableListView();
                                }
                            });
                        }
                    }
                    if(AMessageType.TYPE_OUT_GROUP.equals(msg.type)){
                        if("0".equals(msg.content)){
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"退出群失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"退出群成功",Toast.LENGTH_SHORT).show();
                                    //请求群列表的消息
                                    AMessage groupMsg = new AMessage();
                                    groupMsg.type = AMessageType.TYPE_GROUP_LIST;
                                    groupMsg.content = ((LocationAttendanceApp)(getActivity().getApplication())).getUserName();
                                    conn.sendMessage(groupMsg,false,null);
                                    initExpandableListView();
                                }
                            });
                        }
                    }

                    if(AMessageType.TYPE_DELETE_GROUP.equals(msg.type)){
                        if("0".equals(msg.content)){
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"删除群失败",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            ThreadUtils.runInUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(),"删除群成功",Toast.LENGTH_SHORT).show();
                                    //请求群列表的消息
                                    AMessage groupMsg = new AMessage();
                                    groupMsg.type = AMessageType.TYPE_GROUP_LIST;
                                    groupMsg.content = ((LocationAttendanceApp)(getActivity().getApplication())).getUserName();
                                    conn.sendMessage(groupMsg,false,null);
                                    initExpandableListView();
                                }
                            });
                        }
                    }

                }
            });
        }
    };
    //将服务器发来的list，判断每个群是创建的群还是加入的群
    private List<List<GroupData>> splitList(List<GroupData> child){
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> joinGroupList = new ArrayList<>();
        List<GroupData> makeGroupList = new ArrayList<>();
        for(GroupData d : child){
            if(d.groupown){
                //是创建的群
                makeGroupList.add(d);
            }else{
                //是加入的群
                joinGroupList.add(d);
            }
        }
        //get(0):创建的群  get(1):加入的群
        list.add(makeGroupList);
        list.add(joinGroupList);
        return list;
    }

    //关闭时释放资源
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(conn!=null){
            conn.unBindContext();
            conn.removeOnMessageListener(groupListener);
        }

    }
}
