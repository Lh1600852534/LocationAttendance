package lh.henu.edu.cn.locationattendance.net;

/**
 * Created by bowen on 2017/10/20.
 */

public class AMessage extends ProtocalObj{

    public String id="";
    //type=0为注册 type=1为登陆
    public String type = "0";// 类型的数据 chat login
    public String from = "";// 发送者 account
    public String fromNick = "";// 昵称
    public int fromAvatar = 1;// 头像
    public long to = 0L; // 接收者 account
    public String content = ""; // 消息的内容
    public boolean state = false;//
}
