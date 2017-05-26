package eu.tankernn.game.networking;

import eu.tankernn.gameEngine.TankernnGame3D;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class GameClientHandler extends ChannelInboundHandlerAdapter {

	private TankernnGame3D game;

	public GameClientHandler(TankernnGame3D instance) {
		this.game = instance;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("Username");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof Integer) {
			System.out.println("Seed: " + msg);
		}

		// else if (msg instanceof Pair<Integer, Vector3f>) {
		// ByteBuf buf = (ByteBuf) msg;
		// int entityId = buf.readInt();
		// float x = buf.readFloat();
		// float y = buf.readFloat();
		// float z = buf.readFloat();
		// game.getEntities().stream().filter(e -> e.getId() ==
		// entityId).findFirst()
		// .ifPresent(e -> e.getPosition().set(x, y, z));
		// }
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			ctx.writeAndFlush(new Object());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
