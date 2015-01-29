package org.bc.pub_sub;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bc.server.Quasar;
import org.zeromq.ZMQ;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

public class Subscriber extends BasicActor<Quasar.Msg, Void> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2631592353570451378L;

	ZMQ.Socket socket = null;
	int port = -1;
	FiberSocketChannel fscout = null;

	public Subscriber(int port, String[] channels, FiberSocketChannel sout) {
		ZMQ.Context context = ZMQ.context(1);
		this.port = port;
		this.fscout = sout;
		socket = context.socket(ZMQ.SUB);
		socket.connect("tcp://localhost:" + this.port);
		if (channels.length == 0)
			socket.subscribe("".getBytes());
		else
			for (int i = 0; i < channels.length; i++)
				socket.subscribe(channels[i].getBytes());
	}

	public void subscribeNewChannels(String[] channels) {
		if (channels != null)
			for (int i = 0; i < channels.length; i++)
				socket.subscribe(channels[i].getBytes());
	}

	public void stopSubscriber() {
		if (this.port != -1) {
			this.socket.disconnect("tcp://localhost:" + port);
			this.socket.close();
		}
	}

	@Override
	protected Void doRun() throws InterruptedException, SuspendExecution {
		while (true) {
			byte[] b = socket.recv();
			// System.out.println(new String(b));
			try {
				if (fscout != null)
					fscout.write(ByteBuffer.wrap(b));
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * public static void main(String[] args) { Subscriber pub = new
	 * Subscriber(12346, new String[] { "[clientlogin]" }, null);
	 * pub.spawnThread(); try { pub.join(); } catch (ExecutionException |
	 * InterruptedException e) { e.printStackTrace(); } }
	 */
}
