package lh.henu.edu.cn.locationattendance.util;

import java.util.List;

import lh.henu.edu.cn.locationattendance.net.AMessage;

/**
 * Created by bowen on 2017/12/12.
 */

public interface IAMessageDao {
    //添加消息
    public long save(AMessage aMessage);
    //删除消息
    public int delete(AMessage aMessage);
    //找到所有消息
    public List<AMessage> findAllMessage(String userName);

    //查询是否存在消息
    public boolean exist(AMessage aMessage);
}
