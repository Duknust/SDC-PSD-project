package org.bc.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AdminClient {

	public static void main(String args[]) {

		int port = 12343;
		boolean done = false;

		Socket socket = null;
		String fromServer = "";
		boolean processed = false;

		try {
			socket = new Socket("localhost", port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		BufferedReader brConsole = new BufferedReader(new InputStreamReader(
				System.in));
		BufferedWriter bwConsole = new BufferedWriter(new OutputStreamWriter(
				System.out));

		BufferedReader brSocket = null;
		BufferedWriter bwSocket = null;
		try {
			brSocket = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			bwSocket = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bwConsole.write("Welcome to BananaChat administration\n");
			bwConsole.flush();
			while (!done) {
				bwConsole.write("Users\n");
				bwConsole.flush();
				bwConsole.write("1 - User listing\n");
				bwConsole.flush();
				bwConsole.write("2 - User create\n");
				bwConsole.flush();
				bwConsole.write("3 - User remove\n");
				bwConsole.flush();
				bwConsole.write("Rooms\n");
				bwConsole.flush();
				bwConsole.write("4 - Rooms listing\n");
				bwConsole.flush();
				bwConsole.write("5 - Room create\n");
				bwConsole.flush();
				bwConsole.write("6 - Room remove\n");
				bwConsole.flush();
				bwConsole.write("Administrative\n");
				bwConsole.flush();
				bwConsole.write("7 - Close\n");
				bwConsole.flush();
				bwConsole.write("> ");
				bwConsole.flush();

				// opt = Integer.parseInt(brConsole.readLine());
				String option = "";
				option = brConsole.readLine();

				// System.out.println(option + "\n");
				// if (opt < 7 && opt > 0) {

				processed = false;
				if (option.equals("1")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}
				if (option.equals("2")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();
					bwConsole.write("Desired username> ");
					bwConsole.flush();
					String user = brConsole.readLine();
					bwSocket.write(user + "\n");
					bwSocket.flush();
					bwConsole.write("Password> ");
					bwConsole.flush();
					String password = brConsole.readLine();
					bwSocket.write(password + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}

				if (option.equals("3")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();
					bwConsole.write("User to remove> ");
					bwConsole.flush();
					String username = brConsole.readLine();
					bwSocket.write(username + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}

				if (option.equals("4")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}

				if (option.equals("5")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();

					bwConsole.write("Desired roomname> ");
					bwConsole.flush();
					String username = brConsole.readLine();
					bwSocket.write(username + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}

				if (option.equals("6")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();
					bwConsole.write("Room to remove> ");
					bwConsole.flush();
					String username = brConsole.readLine();
					bwSocket.write(username + "\n");
					bwSocket.flush();

					fromServer = brSocket.readLine();
					bwConsole.write(fromServer + "\n");
					bwConsole.flush();
					processed = true;
				}
				if (option.equals("7")) {
					bwSocket.write(option + "\n");
					bwSocket.flush();
					brSocket.readLine();
					bwConsole.write("Closed\n");
					bwConsole.flush();
					bwSocket.close();
					done = true;
					processed = true;
				}
				if (!processed) {
					bwConsole.write("Something went wrong. Try again\n");
					bwConsole.flush();
				}
			}
			bwConsole.write("Administration Panel Closed\n");
			bwConsole.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
