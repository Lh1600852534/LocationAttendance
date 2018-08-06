package lh.henu.edu.cn.locationattendance.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import lh.henu.edu.cn.locationattendance.net.AMessage;

/**
 * Created by bowen on 2017/12/12.
 */

public class DBAdapter implements IAMessageDao {

    public static String DBName = "LocationDataBase";//数据库名
    public static String DBAMessageSql = "create table AMessageTable("
                                            +"id integer not null primary key,"
                                            +"type varchar(20),"
                                            +"DBfrom varchar(20),"
                                            +"fromNick varchar(20),"
                                            +"fromAvatar varchar(20),"
                                            +"DBto varchar(20),"
                                            +"content varchar(100),"
                                            +"state varchar(1)"
                                            +")";//建表语句
    public static int DBVersion = 1;//数据库版本号
    public static String TableAMessage =  "AMessageTable";//表名
    public SQLiteDatabase db;
    private static Context context;
    private static DBAdapter dbAdapter;
    private DBAdapter(Context _context){
        context = _context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBOpenHelper helper = new DBOpenHelper(context);
                try{
                    db = helper.getWritableDatabase();
                }catch (SQLException ex){
                    db = helper.getReadableDatabase();
                }

            }
        }).start();
    }

    public static void setContext(Context oContext){
        context = oContext;
    }



    public static DBAdapter getDBAdapater(){
        if(dbAdapter==null){
            dbAdapter = new DBAdapter(context);
        }
        return dbAdapter;
    }



    @Override
    public long save(AMessage aMessage) {
        ContentValues cv = new ContentValues();
        cv.put("id",(String)null);
        cv.put("type",aMessage.type);
        cv.put("DBfrom",aMessage.from);
        cv.put("fromNick",aMessage.fromNick);
        cv.put("fromAvatar",aMessage.fromAvatar);
        cv.put("DBto",aMessage.to+"");
        cv.put("content",aMessage.content);
        cv.put("state",0);
        long t = db.insert(TableAMessage,null,cv);
        return t;

    }

    @Override
    public int delete(AMessage aMessage) {
        return (int)db.delete(TableAMessage,"id=?",new String[]{aMessage.id});

    }

    @Override
    public List<AMessage> findAllMessage(String userName) {
        List<AMessage> list = new ArrayList<>();

        Cursor cursor = db.query(TableAMessage,new String[]{"id","type","DBfrom","fromNick","fromAvatar","DBto","content","state"},"DBto=?",new String[]{userName},null,null,null,null);
        while (cursor.moveToNext()){
            AMessage msg = new AMessage();
            msg.id = cursor.getString(cursor.getColumnIndex("id"));
            msg.type = cursor.getString(cursor.getColumnIndex("type"));
            msg.from = cursor.getString(cursor.getColumnIndex("DBfrom"));
            msg.fromNick = cursor.getString(cursor.getColumnIndex("fromNick"));
            msg.fromAvatar = Integer.parseInt(cursor.getString(cursor.getColumnIndex("fromAvatar")));
            msg.to = Long.parseLong(cursor.getString(cursor.getColumnIndex("DBto")));
            msg.content = cursor.getString(cursor.getColumnIndex("content"));
            list.add(msg);
        }
        return list;
    }

    @Override
    public boolean exist(AMessage aMessage) {
        Cursor cursor = db.query(TableAMessage,new String[]{"id","type","DBfrom","fromNick","fromAvatar","DBto","content","state"},"DBto=? and content=?",new String[]{aMessage.to+"",aMessage.content},null,null,null,null);
        return cursor.moveToNext();
    }


    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context){
            super(context,DBName,null,DBVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DBAMessageSql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    public void deleteDB(){
        context.deleteDatabase(DBName);
    }



}
