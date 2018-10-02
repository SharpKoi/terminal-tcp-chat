package cmen.tcp;

import java.io.IOException;
import java.net.Socket;

public abstract class TCPChatter {
	protected ChatLogger logger = null;
	protected Socket socket = null;
	protected MessageSender sender;
	protected Thread chatMain;
	
	public abstract String getName();
	
	public ChatLogger getLogger() {
		return logger;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void stopAll() throws IOException {
		if(logger != null)
			logger.shutDown();
		if(sender != null)
			sender.setStop();
		if(chatMain != null)
			chatMain.interrupt();
		if(this.socket != null) {
			this.socket.close();
		}
	}
}