package com.kisro.cloud.buffer;

import com.kisro.cloud.util.FileConstants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.kisro.cloud.util.ByteBufferUtil.debugAll;

/**
 * 分散写入
 *
 * @author Kisro
 * @since 2022/10/31
 **/
public class GatheringWrite {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile(FileConstants.PARTS3_FILE_PATH, "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer d = ByteBuffer.allocate(4);
            ByteBuffer e = ByteBuffer.allocate(4);
            channel.position(11);
            d.put(new byte[]{'f', 'o', 'u', 'r'});
            e.put(new byte[]{'f', 'i', 'v', 'e'});
            d.flip();
            e.flip();
            debugAll(d);
            debugAll(e);
            channel.write(new ByteBuffer[]{d, e});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
