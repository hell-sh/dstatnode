package live.dstat.dstatnode.ws;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel>
{
	@Override
	public void initChannel(SocketChannel ch)
	{
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new WebSocketServerCompressionHandler());
		pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true));
		pipeline.addLast(new WebSocketIndexPageHandler());
		pipeline.addLast(new WebSocketFrameHandler());
	}
}

