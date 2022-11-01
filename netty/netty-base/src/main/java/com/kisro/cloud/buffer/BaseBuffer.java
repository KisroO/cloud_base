package com.kisro.cloud.buffer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.kisro.cloud.util.FileConstants.DATA_FILE_PATH;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
@Slf4j
public class BaseBuffer {

    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE_PATH, "rw")) {
            FileChannel channel = file.getChannel();
            System.out.println(file.length());
            ByteBuffer buffer = ByteBuffer.allocate(10);
            do {
                // 写入
                int len = channel.read(buffer);
                log.debug("字节数：{}", len);
                if (len == -1) {
                    break;
                }
                // 切换为读模式
                buffer.flip();
                while (buffer.remaining() > 0) {
                    log.debug("{}", (char) buffer.get());
                }
                // 切换为写模式
                buffer.clear();
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
