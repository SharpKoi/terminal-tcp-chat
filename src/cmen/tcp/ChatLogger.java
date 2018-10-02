package cmen.tcp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import org.fusesource.jansi.Ansi;

import cmen.tcp.client.TCPClient;
import cmen.tcp.server.TCPServer;

public class ChatLogger extends Thread {
	
	public static Map<String, String> colorPool = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("cCyan", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
			put("cRed", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
			put("cPurple", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
			put("cYellow", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).toString());
			put("cReset", Ansi.ansi().a(Ansi.Attribute.RESET).toString());
			put("cGray", Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
		}
	};

	public enum SenderType {
		SYSTEM(colorPool.get("cCyan") + "[System]" + colorPool.get("cReset")),
		ERROR(colorPool.get("cRed") + "[Error]" + colorPool.get("cReset")),
		SERVER(colorPool.get("cPurple") + "[Server]" + colorPool.get("cReset")),
		CLIENT(colorPool.get("cYellow") + "[Client]" + colorPool.get("cReset"));
		
		private String head;
		private SenderType(String head) {
			this.head = head;
		}
		public String head() {
			return head;
		}
	}
	
	//private static final char ESCAPE = '\u00A7';
	private static final Pattern CLEAN_PATTERN = Pattern.compile("\\[0?(;[0-9]+)*m");
	
	private BufferedWriter writer;
	private TCPChatter owner;
	private File logFile;
	private ConcurrentLinkedQueue<String> messagePool;
	private boolean shutDown;
	
	public ChatLogger(TCPChatter owner) {
		this(owner, new File(System.getProperty("user.dir"), "logs/" + owner.getName().toLowerCase() + "log_" + dateFormat(new Date()) + ".log"));
	}
	
	public ChatLogger(TCPChatter owner, File logFile) {
		this.owner = owner;
		this.logFile = logFile;
		shutDown = false;
		messagePool = new ConcurrentLinkedQueue<String>();
		try {
			writer = new BufferedWriter(new FileWriter(logFile));
		} catch (IOException ignore) {
			ignore.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(!shutDown) {
			waitForMessage();
			log();
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignore) {
				ignore.printStackTrace();
			}
		}
	}
	
	public File getLogFile() {
		return logFile;
	}
	
	public static String timeFormat(Date time) {
		return new SimpleDateFormat("Y-M-d HH:mm:ss").format(time);
	}
	
	public static String timeFormat(String colorFormat, Date time) {
		return colorPool.get(colorFormat) + new SimpleDateFormat("Y-M-d HH:mm:ss").format(time) + colorPool.get("cReset");
	}
	
	public static String dateFormat(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	
	private static String clean(String str) {
		String result = CLEAN_PATTERN.matcher(str).replaceAll("");
		if(CLEAN_PATTERN.matcher(result).find()) {
			return clean(result);
		}
		
		return result;
	}
	
	public void info(String message) {
		if(owner instanceof TCPServer) {
			messagePool.offer(SenderType.SERVER.head() + message);
		}else if(owner instanceof TCPClient) {
			messagePool.offer(SenderType.CLIENT.head() + message);
		}
		synchronized(this) {
			notify();
		}
	}
	
	public void info(SenderType sender, String message) {
		synchronized(this) {
			messagePool.add(sender.head() + message);
			notify();
		}
	}
	
	public void waitForMessage() {
		synchronized(this) {
			if(messagePool.isEmpty()) {
				try {
					this.wait(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void log() {
		try {
			while(!messagePool.isEmpty()) {
				String message = messagePool.poll();
				System.out.println(timeFormat("cGray", new Date()) + message);
				writer.write(timeFormat(new Date()));
				writer.write(clean(message));
				writer.write("\r\n");
			}
			writer.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(shutDown) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException ignore) {
				ignore.printStackTrace();
			}
		}
	}
	
	public void shutDown() {
		synchronized(this) {
			this.shutDown = true;
			notify();
		}
	}
	
	public boolean isShutDown() {
		return shutDown;
	}
}
