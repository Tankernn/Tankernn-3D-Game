package eu.tankernn.game.networking;

import eu.tankernn.game.Game;
import eu.tankernn.gameEngine.entities.EntityState;
import eu.tankernn.gameEngine.entities.PlayerBehavior;
import eu.tankernn.gameEngine.multiplayer.LoginRequest;
import eu.tankernn.gameEngine.multiplayer.LoginResponse;
import eu.tankernn.gameEngine.multiplayer.WorldState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GameClientHandler extends ChannelInboundHandlerAdapter {

	private Game game;

	public GameClientHandler(Game instance) {
		this.game = instance;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(new LoginRequest("Username")).sync();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof LoginResponse) {
			LoginResponse response = (LoginResponse) msg;
			EntityState s = response.playerState;
			s.addBehavior(new PlayerBehavior());
			game.setPlayer(game.getWorld().updateEntityState(s, true));
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
