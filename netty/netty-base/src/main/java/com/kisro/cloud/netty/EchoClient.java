package com.kisro.cloud.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @author Kisro
 * @since 2022/10/31
 **/
public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buffer = (ByteBuf) msg;
                                System.out.println(buffer.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .connect("127.0.0.1", 9999)
                .sync()
                .channel();
        channel.closeFuture().addListener(future -> group.shutdownGracefully());

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String msg = sc.nextLine();
                if ("q".equalsIgnoreCase(msg)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(msg);
            }
        }).start();
    }
}
