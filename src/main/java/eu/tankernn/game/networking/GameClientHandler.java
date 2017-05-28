package eu.tankernn.game.networking;

import eu.tankernn.gameEngine.TankernnGame3D;
import eu.tankernn.gameEngine.entities.EntityState;
import eu.tankernn.gameEngine.multiplayer.LoginRequest;
import eu.tankernn.gameEngine.multiplayer.LoginResponse;
import eu.tankernn.gameEngine.multiplayer.WorldState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GameClientHandler extends ChannelInboundHandlerAdapter {

	private TankernnGame3D game;

	public GameClientHandler(TankernnGame3D instance) {
		this.game = instance;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		try {
			ctx.writeAndFlush(new LoginRequest("Username")).sync();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof LoginResponse) {
			LoginResponse response = (LoginResponse) msg;
			game.getPlayer().setState(response.playerState);
			game.getWorld().getEntities().put(response.playerState.getId(), game.getPlayer());
			System.out.println("Logged in.");
		} else if (msg instanceof WorldState) {
			game.getWorld().setState((WorldState) msg);
		} else if (msg instanceof EntityState) {
			EntityState state = (EntityState) msg;
			game.getWorld().updateEntityState(state);
		} else {
			System.err.println("Unknown message: " + msg.toString());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
