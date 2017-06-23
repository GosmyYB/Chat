package com.wyb.demo.util;

import com.wyb.demo.async.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO 操作工具类
 * Created by wyb.
 */
public class IOUtil {

    private static final Logger logger = LoggerFactory.getLogger(IOUtil.class);

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
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
