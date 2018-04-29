package live.dstat.dstatnode.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import live.dstat.dstatnode.Main;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame)
	{
		if(frame instanceof TextWebSocketFrame)
		{
			String rawdata = ((TextWebSocketFrame) frame).text();
			if(rawdata.equals("p"))
			{
				synchronized(Main.sigar)
				{
					ctx.channel().writeAndFlush(new TextWebSocketFrame("p " + Main.down + " " + Main.up + " " + Main.requests));
				}
			}
		}
	}
}
