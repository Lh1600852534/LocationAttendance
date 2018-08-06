package lh.henu.edu.cn.locationattendance.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.R;
import lh.henu.edu.cn.locationattendance.domain.GroupData;

/**
 * Created by bowen on 2017/9/26.
 */

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {

    private String[] groups;
    private final int firstGroupIndex = 0;
    private final int secondGroupIndex = 1;
    private List<List<GroupData>> child;//child.get(0):创建的群  child.get(1):加入的群
    private Activity activity;

    public MyExpandableListViewAdapter(String[] groups,List<List<GroupData>> child,Activity activity)
    {
        this.groups = groups;
        this.child = child;
        this.activity = activity;
    }


    public void setChild(List<List<GroupData>> child) {
        this.child = child;
    }

    public void setGroups(String[] groups) {

        this.groups = groups;
    }




    @Override
    public int getGroupCount() {
        return groups.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return child.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child.get(groupPosition).get(childPosition).groupName;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.item_group,null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.groupTextView);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.groupImageView);
        textView.setText(groups[groupPosition]);
        if(!isExpanded)
        {
            imageView.setImageResource(R.drawable.ic_arrow_right);
        }else {
            imageView.setImageResource(R.drawable.ic_arrow_down);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
        if(convertView==null)
        {
            convertView = activity.getLayoutInflater().inflate(R.layout.item_child,null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.childTextView);
        ImageView imageView = (ImageView)convertView.findViewById(R.id.childImageView);

        textView.setText(child.get(groupPosition).get(childPosition).groupName);
        //图片未实现
        //Glide.with(imageView.getContext()).load(child.get(groupPosition).get(childPosition).imageUrl).into(imageView);
        imageView.setImageResource(R.drawable.ic_toolbar_face);
        convertView.setTag(R.id.item_child_goup_index_textview,groupPosition+"");
        convertView.setTag(R.id.item_child_goup_number_textview,child.get(groupPosition).get(childPosition).groupId+"");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

}
