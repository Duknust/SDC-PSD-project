package org.bc.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class CommunicationClient {

	public static void main(String args[]) {
		try {

			Socket toServer = null;
			BufferedReader brConsole = null;
			BufferedWriter bwConsole = null;

			BufferedReader brSocket = null;
			BufferedWriter bwSocket = null;

			toServer = new Socket("localhost", 12345);
			brConsole = new BufferedReader(new InputStreamReader(System.in));
			bwConsole = new BufferedWriter(new OutputStreamWriter(System.out));
			brSocket = new BufferedReader(new InputStreamReader(
					toServer.getInputStream()));
			bwSocket = new BufferedWriter(new OutputStreamWriter(
					toServer.getOutputStream()));

			bwConsole.write("username>");
			bwConsole.flush();
			String username = brConsole.readLine();
			bwConsole.write("password>");
			bwConsole.flush();
			String password = brConsole.readLine();

			bwSocket.write("/li " + username.trim() + ":" + password.trim()
					+ "\n");
			bwSocket.flush();
			String msg = brSocket.readLine();
			// System.out.println(msg);
			if (msg.trim().equals("ok")) {
				// bwConsole.write("OK\n");
				// bwConsole.flush();
				// System.out.println("SOAFHAUF");
				Thread consoleReader = new Thread(new MsgReader(brConsole,
						bwSocket));
				Thread socketReader = new Thread(new MsgReader(brSocket,
						bwConsole));

				consoleReader.start();
				socketReader.start();

				consoleReader.join();
				socketReader.join();

				toServer.close();
			} else {
				bwConsole.write("No logged in. Try again\n");
				bwConsole.flush();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

class MsgReader implements Runnable {
	BufferedReader brIn = null;
	BufferedWriter bwOut = null;
	boolean exit = false;

	public MsgReader(BufferedReader brIn, BufferedWriter bwOut) {
		this.brIn = brIn;
		this.bwOut = bwOut;
	}

	@Override
	public void run() {
		while (!exit) {
			try {
				String msg = brIn.readLine();
				// System.out.println(msg);
				bwOut.write(msg + "\n");
				bwOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
