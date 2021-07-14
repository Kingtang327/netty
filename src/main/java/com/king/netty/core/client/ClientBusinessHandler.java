package com.king.netty.core.client;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author King
 * @date 2021/7/14
 */
public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {

    public static final Logger logger = LoggerFactory.getLogger(ClientBusinessHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        if (dataFrame.getCmd() == DataFrame.CMD_AUTHORIZATION) {
            // 发送业务请求
            ctx.writeAndFlush(new DataFrame(DataFrame.CMD_GET_INFO, "which language is the best ?".getBytes()));
        }else {
            // 打印服务器发送的消息
            logger.debug("receive message: " + dataFrame);
        }
        ReferenceCountUtil.release(msg);
    }
}
