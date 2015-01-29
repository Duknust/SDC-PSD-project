package org.bc.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.zeromq.ZMQ;

public class SubscribeClient {

	public static void main(String args[]) {

		int port = 12346;
		boolean done = false;
		int opt = -1;

		ArrayList<String> tmpChannels = new ArrayList<String>();
		String[] channels = null;

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.SUB);

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				System.out));

		try {
			bw.write("Welcome to BananaChat subscriber\n");
			bw.flush();
			while (!done) {
				bw.write("Users\n");
				bw.flush();
				bw.write("1 - User login\n");
				bw.flush();
				bw.write("2 - User logout\n");
				bw.flush();
				bw.write("Rooms\n");
				bw.flush();
				bw.write("3 - Room creation\n");
				bw.flush();
				bw.write("4 - Room remove\n");
				bw.flush();
				bw.write("Administrative\n");
				bw.flush();
				bw.write("5 - All\n");
				bw.flush();
				bw.write("6 - Close\n");
				bw.flush();

				opt = Integer.parseInt(br.readLine());
				switch (opt) {
				case 1:
					tmpChannels.add("userlogin");
					break;
				case 2:
					tmpChannels.add("userlogout");
					break;
				case 3:
					tmpChannels.add("roomcreated");
					break;
				case 4:
					tmpChannels.add("roomremoved");
					break;
				case 5:
					done = true;
					break;
				case 6:
					socket.close();
					return;
				default:
					break;
				}

			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		socket.connect("tcp://localhost:" + port);
		// if (tmpChannels.size() > 0)
		channels = new String[tmpChannels.size()];
		tmpChannels.toArray(channels);

		if (channels.length == 0)
			socket.subscribe("".getBytes());
		else
			for (int i = 0; i < channels.length; i++)
				socket.subscribe(("[" + channels[i] + "]").getBytes());
		while (true) {
			byte[] b = socket.recv();
			try {
				bw.write(new String(b));
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// System.out.println(new String(b));
		}
		// socket.close();
		// context.term();

	}
}
