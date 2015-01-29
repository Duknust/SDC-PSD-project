/**
 * 
 */
package org.bc.server;

import java.util.HashMap;

import org.bc.hello.HelloApplication;

/**
 * @author duarteduarte
 *
 */
public class Server {

	// private static Publisher publisher = null;
	// TODO : rooms tem um ou passa a haver um global?
	// private static List<Relay> allClients = new ArrayList<Relay>();
	// private static HashMap<String, String> users = new HashMap<String,
	// String>();
	private static HashMap<String, String> loginsHash = null;

	// private static Quasar quasar = null;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		String[] argsLess = new String[args.length - 2];
		// users.put("user", "pass");
		// users.put("user1", "pass");
		// users.put("user2", "pass");
		// users.put("user3", "pass");

		try {
			/*
			 * Thread zmqThread = new Thread() {
			 * 
			 * @Override public void run() {
			 * startZMQ(Integer.parseInt(args[1])); } };
			 * 
			 * zmqThread.start();
			 */

			System.out.println("HandlerQuasar started\n");
			HandlerQuasar hq = new HandlerQuasar(12345);
			new Thread(hq).start();
			// Thread.sleep(1000);
			Quasar quasar = hq.getQuasar();

			System.arraycopy(args, 2, argsLess, 0, args.length - 2);
			startREST(argsLess, quasar);
			System.out.println("Rest started\n");

			// @SuppressWarnings("resource")
			// ServerSocket ss = new ServerSocket(12343);

			loginsHash = quasar.getLoginsHash();

			System.out.println("HandlerCurl started\n");
			System.out.println("Server is online");

			HandlerCurl hc = new HandlerCurl(loginsHash, quasar);
			new Thread(hc).start();

			// startActor(Integer.parseInt(args[0]));

			// BufferedReader reader = new BufferedReader(new InputStreamReader(
			// System.in));
			// String roomName = null;

			// while (true) {
			// roomName = reader.readLine();
			// HandlerQuasar hq =

			// new Thread(hq).start();
			// }

			// HandlerQuasar hq2 = new HandlerQuasar(12350);
			// new Thread(hq2).start();

			// while (true) {
			/*
			 * ClientHandler ch = new ClientHandler(ss.accept(), allClients,
			 * publisher, users); new Thread(ch).start();
			 */

			// System.out.println("NEW CLIENT!");

			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void startREST(String[] args, Quasar quasar)
			throws Exception {
		new HelloApplication(loginsHash, quasar).run(args);
	}

	@SuppressWarnings("unused")
	private static void startActor(int port) {
		// new ClientQuasar(port);
		try {
			new HandlerQuasar(port);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
