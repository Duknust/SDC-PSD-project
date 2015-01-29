package org.bc.server;

public class HandlerQuasar implements Runnable {

	private int port;
	private static Quasar quasar = null;

	public HandlerQuasar(int port) {
		this.port = port;
		quasar = new Quasar();
	}

	Quasar getQuasar() {
		return quasar;
	}

	@Override
	public void run() {
		try {
			quasar.startQuasar(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
