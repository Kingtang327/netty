package com.king.netty.core.client;

import com.king.netty.core.DataFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author King
 * @date 2021/7/14
 */
public class AuthorizationRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接成功后发起认证请求
        ctx.writeAndFlush(DataFrame.getAuthorizationDataFrame());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DataFrame dataFrame = (DataFrame) msg;
        // 处理认证响应
        if (dataFrame.getCmd() == DataFrame.CMD_AUTHORIZATION) {
            byte[] params = dataFrame.getParams();
            if (! "success".equals(new String(params))){
                // 认证失败,关闭连接
                ReferenceCountUtil.release(msg);
                ctx.close();
            }
        }
        // 认证成功,继续传递消息
        // 非认证的响应,交给后续业务处理
        ctx.fireChannelRead(msg);
    }
}
