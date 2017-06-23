package com.wyb.demo.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO 操作工具类
 * Created by wyb.
 */
public class IOUtil {

    /**
     * 关闭所有传入的 IO 对象
     * @param io
     */
    public static void closeAll(Closeable ... io) {
        for (Closeable c : io) {
            if (c != null) {
                try {
                    c.close();
                    c = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
