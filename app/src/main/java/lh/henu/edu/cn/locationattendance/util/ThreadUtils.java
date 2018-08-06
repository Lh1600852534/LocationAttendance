package lh.henu.edu.cn.locationattendance.util;

import android.os.Handler;

/**
 * Created by bowen on 2017/10/25.
 */

public class ThreadUtils {
    /**
     * 运行在子线程
     */
    public static void runInSubThread(Runnable r)
    {
        new Thread(r).start();
    }
    private static Handler handler = new Handler();
    /**
     * 运行在主线程(UI 线程 更新界面)
     */
    public static void runInUiThread(Runnable r)
    {
        handler.post(r);
    }
}
