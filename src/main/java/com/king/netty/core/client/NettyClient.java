package com.king.netty.core.client;

import com.king.netty.core.DataFrameDecoder;
import com.king.netty.core.DataFrameEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author King
 * @date 2021/7/14
 */
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        startServer();
    }

    static void startServer() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                // 设置线程组
                .group(group)
                // 设置为NIO模式
                .channel(NioSocketChannel.class)
                // 设置pipeline中的全部的channelHandler
                // 入站方向的channelHandler需要保证顺序
                // 出站方向的channelHandler需要保证顺序
                .handler(new ClientHandlerInit());
        bootstrap.connect("127.0.0.1", 8888).sync();
    }

    static class ClientHandlerInit extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // 日志打印
            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            // LengthFieldBasedFrameDecoder 用于解决TCP黏包半包问题
            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                    65535,              // maxFrameLength       消息最大长度
                    2,               // lengthFieldOffset    指的是长度域的偏移量，表示跳过指定个数字节之后的才是长度域
                    2,               // lengthFieldLength    记录该帧数据长度的字段，也就是长度域本身的长度
                    0,                // lengthAdjustment     长度的一个修正值，可正可负，Netty 在读取到数据包的长度值 N 后， 认为接下来的 N 个字节都是需要读取的，但是根据实际情况，有可能需要增加 N 的值，也 有可能需要减少 N 的值，具体增加多少，减少多少，写在这个参数里
                    2              // initialBytesToStrip  从数据帧中跳过的字节数，表示得到一个完整的数据包之后，扔掉 这个数据包中多少字节数，才是后续业务实际需要的业务数据。
            ));
            // 自定义协议解码器
            pipeline.addLast(new DataFrameDecoder());
            // 自定义协议编码器
            pipeline.addLast(new DataFrameEncoder());
            // 处理认证请求的handler
            pipeline.addLast(new AuthorizationRequestHandler());
            // 处理心跳的handler
            pipeline.addLast(new HeartBeatRequestHandler());
            // 客户端业务handler
            pipeline.addLast(new ClientBusinessHandler());
        }
    }
}
