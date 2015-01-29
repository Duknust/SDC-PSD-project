package org.bc.pub_sub;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.zeromq.ZMQ;

public class Publisher {
	ZMQ.Socket socket = null;

	public void startPublisher(int port) {
		ZMQ.Context context = ZMQ.context(1);
		socket = context.socket(ZMQ.PUB);
		socket.bind("tcp://*:" + port);
		// byte[] msg = new byte[140];
		// String s = null;
	}

	public void publishClientLogin(String clientName) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		String data = dateFormat.format(cal.getTime());

		this.socket.send(("[clientLogin] [" + data + "] ").toLowerCase()
				+ clientName);
	}

	public void publishClientLogout(String clientName) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		String data = dateFormat.format(cal.getTime());
		this.socket.send(("[ClientLogout] [" + data + "] ").toLowerCase()
				+ clientName);
	}

	public void publishChannelAdded(String channelName) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		String data = dateFormat.format(cal.getTime());
		this.socket.send(("[ChannelAdded] [" + data + "] ").toLowerCase()
				+ channelName);
	}

	public void publishChannelRemoved(String channelName) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		String data = dateFormat.format(cal.getTime());
		this.socket.send(("[ChannelRemoved] [" + data + "] ").toLowerCase()
				+ channelName);
	}

	/*
	 * public static void main(String[] args) { Publisher pub = new Publisher();
	 * pub.startPublisher(12346); while (true) { try { Thread.sleep(5000); }
	 * catch (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } pub.publishClientLogin("ze"); } }
	 */
}
