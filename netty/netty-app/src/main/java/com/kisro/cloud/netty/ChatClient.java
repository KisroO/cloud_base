package com.kisro.cloud.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Kisro
 * @since 2022/11/1
 **/
public class ChatClient {
    private int port = 9999;
    private String host = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        new ChatClient().start();
    }

    private void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)//
                    .channel(NioSocketChannel.class)//
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()//
                                    .addLast("framer", new DelimiterBasedFrameDecoder(2048, Delimiters.lineDelimiter()))//
                                    .addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))//
                                    .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))//
                                    .addLast("handler", new ChatClientHandler());
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            System.out.println("请输入要发送的消息：");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                future.channel().writeAndFlush(reader.readLine() + "\n");
            }
        } finally {
            group.shutdownGracefully();
        }

    }
}
