package lh.henu.edu.cn.locationattendance.domain;

/**
 * Created by bowen on 2017/10/25.
 */

public class AMessageType {
    public static String TYPE_REGISTER = "0";//类型注册
    public static String CONTENT_REGISTER_SUCCESS = "1";//注册成功
    public static String CONTENT_REGISTER_FAIL = "0";//注册失败

    public static String TYPE_LOGIN = "1";//类型登陆
    public static String CONTENT_LOGIN_FAIL = "0";//登陆失败
    public static String CONTENT_LOGIN_SUCCESS = "1";//登陆成功


    public static String TYPE_GROUP_LIST = "2";//请求群列表
    public static String CONTENT_GROUP_LIST_FAIL = "0";//请求群列表失败

    public static String TYPE_RESET_PASSWORD = "3";//类型重置密码
    public static String CONTENT_RESET_PASSWORD_SUCCESS = "1";//重置密码成功
    public static String CONTENT_RESET_PASSWORD_FAIL = "0";//重置密码失败

    public static String TYPE_SEARCH = "4";//类型搜索
    public static String CONTENT_SEARCH_FAIL = "0";//没有搜索到

    public static String TYPE_REQUEST_JOIN = "5";//类型请求加入
    public static String CONTENT_REQUEST_JOIN_SUCCESS = "1";//加入群成功

    public static String TYPE_REQUEST_USER_INFO = "6";//类型请求用户信息
    public static String CONTENT_REQUEST_USER_INFO_FAIL = "0";//请求用户信息失败

    public static String TYPE_REQUEST_USER_INFO_CHANGE = "7";//类型请求修改用户信息
    public static String CONTENT_REQUEST_USER_INFO_CHANGE_FAIL = "0";//请求修改用户信息失败
    public static String CONTENT_REQUEST_USER_INFO_CHANGE_SUCCESS = "1";//请求修改用户信息成功

    public static String TYPE_REUQEST_MAKE_GROUP = "8";//类型创建群
    public static String CONTENT_REUEST_MAKE_GROUP_SUCCESS = "1";//创建群成功
    public static String CONTENT_REQUEST_MAKE_GROUP_FAIL = "0";//创建群失败

    public static String TYPE_REQUEST_NEWS = "10";//类型请求离线消息
    public static String CONTENT_REQUEST_NEWS_FAIL = "0";//请求离线消息失败

    public static String TYPE_IS_CONNECT = "9";//类型检测是否与服务器连接

    public static String TYPE_REQUEST_GROUP_INFO = "11";//类型请求群信息
    public static String CONTENT_REQUEST_GROUP_INFO_FAIL = "0";//请求群信息失败

    public static String TYPE_REQUEST_GROUP_INFO_CHANGE = "12";//类型请求修改群信息
    public static String CONTENT_REQUEST_GROUP_INFO_CHANGE_FAIL = "0";//请求修改群信息失败
    public static String CONTENT_REQUEST_GROUP_INFO_CHANGE_SUCCESS = "1";//请求修改群信息成功

    public final static String TYPE_REQUEST_JOIN_GROUP_TO = "13";//类型加入群
    public final static String CONTENT_REQUEST_JOIN_GROUP_TO = "0";//加入失败
    public final static String TYPE_REQUEST_JOIN_GROUP_DEAL = "14";//处理加入群

    public static String TYPE_REQUEST_MESSAGE = "15";//请求离线消息
    public static String CONTENT_REQUEST_MESSAGE_FAIL = "0";//请求离线消息失败

    public static String TYPE_REMOVE_MESSAGE  = "16";//删除离线消息

    public final static String TYPE_START_SIGN_IN = "17";//发起签到
    public final static String CONTENT_START_SIGN_IN_FAIL = "0";//发起签到失败

    public final static String TYPE_REQUEST_SIGN_IN = "18";//要求签到

    public final static String TYPE_UPDATE_SIGN_IN_PERSON_NUM = "19";//更新签到人数

    public final static String TYPE_SIGN_IN_RESULT = "20";//签到结果
    //同意签到发给服务器的content为签到时间+群id+纬度+经度
    //不同意签到发给服务器0
    //客户端接收时content为0签到失败
    public final static String CONTENT_SIGN_IN_RESULT_FAIL = "0";


    public final static String TYPE_GET_GROUP_PERSON_SIGN_IN_RESULT = "21";//请求某群的所有成员的签到结果
    //把群号发给服务器
    public final static String TYPE_GET_PERSON_SIGN_IN_RESULT = "22";//请求某群个人的签到记录
    //把群号发给服务器

    //把群号发给服务器
    public final static String TYPE_DELETE_GROUP = "23";//删除签到群
    public final static String CONTENT_DELETE_GROUP_FAIL = "0";//删除失败

    //把群号发给服务器
    public final static String TYPE_OUT_GROUP = "24";//退出签到群

    //系统退出
    public final static String TYPE_OUT = "25";//系统退出




}
