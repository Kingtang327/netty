package com.king.netty.core.server;

import com.king.netty.core.DataFrameDecoder;
import com.king.netty.core.DataFrameEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author King
 * @date 2021/7/14
 */
@Component
public class NettyServer implements InitializingBean, DisposableBean {

    private boolean started;
    private Channel channel;
    private NioEventLoopGroup parentGroup;
    private NioEventLoopGroup childGroup;

    @Override
    public void destroy() throws Exception {
        // spring销毁对象时调用stop释放服务器
        if (started){
            stopServer();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // spring初始化对象后, 调用启动方法,启动服务
        if (started){
            return;
        }
        startServer();
    }

    void startServer() throws InterruptedException {
        this.parentGroup = new NioEventLoopGroup();
        this.childGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                // 设置线程组
                .group(parentGroup, childGroup)
                // 设置为NIO模式
                .channel(NioServerSocketChannel.class)
                // 设置TCP sync队列大小, 防止洪泛攻击
                .childOption(ChannelOption.SO_BACKLOG, 1024)
                // 设置pipeline中的全部的channelHandler
                // 入站方向的channelHandler需要保证顺序
                // 出站方向的channelHandler需要保证顺序
                .childHandler(new ServerHandlerInit());
        this.channel = serverBootstrap.bind(8888).sync().channel();
        started = true;
    }

    void stopServer(){
        try{
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            channel.closeFuture().syncUninterruptibly();
        }finally {
            this.parentGroup = null;
            this.childGroup = null;
            this.channel = null;
            started = false;
        }
    }

    static class ServerHandlerInit extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // 日志打印
            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            // IP过滤
            pipeline.addLast(new RuleBasedIpFilter(new IpFilterRule() {
                @Override
                public boolean matches(InetSocketAddress remoteAddress) {
                    // 自定义IP地址拦截器,  非127开头的IP不允许连接
                    return ! remoteAddress.getHostName().startsWith("127");
                }
                @Override
                public IpFilterRuleType ruleType() {
                    return IpFilterRuleType.REJECT;
                }
            }));
            // LengthFieldBasedFrameDecoder 用于解决TCP黏包半包问题
            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                    65535,              // maxFrameLength       消息最大长度
                    2,               // lengthFieldOffset    指的是长度域的偏移量，表示跳过指定个数字节之后的才是长度域
                    2,               // lengthFieldLength    记录该帧数据长度的字段，也就是长度域本身的长度
                    0,                // lengthAdjustment     长度的一个修正值，可正可负，Netty 在读取到数据包的长度值 N 后， 认为接下来的 N 个字节都是需要读取的，但是根据实际情况，有可能需要增加 N 的值，也 有可能需要减少 N 的值，具体增加多少，减少多少，写在这个参数里
                    2              // initialBytesToStrip  从数据帧中跳过的字节数，表示得到一个完整的数据包之后，扔掉 这个数据包中多少字节数，才是后续业务实际需要的业务数据。
            ));
            // 设置心跳的超时时间 30s, 如果30s内未收到心跳则会抛出ReadTimeoutException
            pipeline.addLast(new ReadTimeoutHandler(30));
            // 自定义协议解码器
            pipeline.addLast(new DataFrameDecoder());
            // 自定义协议编码器
            pipeline.addLast(new DataFrameEncoder());
            // 认证处理
            pipeline.addLast(new AuthorizationResponseHandler());
            // 心跳处理
            pipeline.addLast(new HeartBeatResponseHandler());
            // 业务处理handler
            pipeline.addLast(new ServerBusinessHandler());
        }
    }
}
