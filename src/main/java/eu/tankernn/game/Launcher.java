package eu.tankernn.game;

import static eu.tankernn.game.Settings.GAME_NAME;

import org.lwjgl.opengl.Display;

import eu.tankernn.gameEngine.TankernnGame;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.util.NativesExporter;

public class Launcher {
	
	public static TankernnGame instance;
	
	public static void main(String[] args) {
		init(args[0], Integer.parseInt(args[1]));
		
		while (!Display.isCloseRequested()) {
			instance.update();
			instance.fullRender();
		}
		
		instance.cleanUp();
	}
	
	private static void init(String host, int port) {
		NativesExporter.exportNatives();
		DisplayManager.createDisplay(GAME_NAME);
		instance = new Game();
		
//		EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//        try {
//            Bootstrap b = new Bootstrap(); // (1)
//            b.group(workerGroup); // (2)
//            b.channel(NioSocketChannel.class); // (3)
//            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
//            b.handler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new GameClientHandler());
//                }
//            });
//
//            // Start the client.
//            ChannelFuture f = b.connect(host, port).sync(); // (5)
//
//            // Wait until the connection is closed.
//            f.channel().closeFuture().sync();
//        } catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//            workerGroup.shutdownGracefully();
//        }
	}
	
}
