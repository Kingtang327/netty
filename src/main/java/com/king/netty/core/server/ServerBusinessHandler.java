package com.king.netty.core.server;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author King
 * @date 2021/7/14
 */
public class ServerBusinessHandler extends ChannelInboundHandlerAdapter {

    public static final Logger logger = LoggerFactory.getLogger(ServerBusinessHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        logger.debug("receive message: " + dataFrame);

        // 返回客户端数据
        DataFrame response = doBusiness(dataFrame);
        ctx.writeAndFlush(response);
    }

    private DataFrame doBusiness(DataFrame dataFrame){
        // 处理自己的业务
        // todo
        // 响应客户端
        return new DataFrame(dataFrame.getCmd(), "java is the best language".getBytes());
    }
}
