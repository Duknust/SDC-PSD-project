package org.bc.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

public class HandlerCurl implements Runnable {

	static String ADD_USER_COMMAND = "curl -X PUT localhost:8080\\hello\\users\\add\\";
	static String ADD_USER = "Add new user";
	static String ADD_ROOM_COMMAND = "curl -X PUT localhost:8080\\hello\\rooms\\add\\";
	static String ADD_ROOM = "Add new room";
	static String REMOVE_USER_COMMAND = "curl -X REMOVE localhost:8080\\hello\\users\\remove\\";
	static String REMOVE_USER = "Remove user";
	static String REMOVE_ROOM_COMMAND = "curl -X REMOVE localhost:8080\\hello\\rooms\\remove\\";
	static String REMOVE_ROOM = "Remove room";
	// Socket socket = null;
	static Quasar quasar = null;

	// private static String adminUser = "admin";
	// private static String adminPass = "pass";

	private static HashMap<String, String> logins;

	public HandlerCurl(HashMap<String, String> loginsHash, Quasar quasarServer) {
		logins = loginsHash;
		quasar = quasarServer;
	}

	@Override
	public void run() {
		while (true) {
			Socket socket = null;
			BufferedReader br = null;
			BufferedWriter bw = null;
			try {
				@SuppressWarnings("resource")
				ServerSocket ss = new ServerSocket(12343);
				socket = ss.accept();

				br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				bw = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			boolean closeConnection = false;
			String username = null;
			String password = null;
			boolean status = false;

			URL url = null;
			HttpURLConnection httpCon = null;

			// System.out.println("while\n");

			while (!closeConnection) {
				// System.out.println("after while\n");

				status = false;
				try {
					// System.out.println("entrei\n");
					String opt = br.readLine();
					// System.out.println("---" + opt + "\n");
					if (!opt.contains("\u001a"))
						switch (opt) {
						case "1": // user listing
							Set<String> users = quasar.listUsers();
							String res = "";
							for (String s : users)
								res += s + " ";
							// System.out.println("RES:" + res + "\n");
							bw.write(res + "\n");
							bw.flush();
							break;
						case "2": // user create
							bw.write("\ndesired username> \n");
							bw.flush();
							username = br.readLine();
							password = br.readLine();

							if (logins.containsKey(username)) {
								bw.write("[System] This username has been taken\n");
								bw.flush();
								break;
							}
							// bw.write("desired password> \n");
							// bw.flush();

							quasar.createUser(username + "-" + password);
							// logins.put(username, password);
							bw.write("[System] New user added\n");
							bw.flush();
							/*
							 * System.out.println(username + "-" + password);
							 * try { url = new URL(
							 * "http://localhost:8080/hello/user/add?name=" +
							 * username + "-" + password); httpCon =
							 * (HttpURLConnection) url .openConnection();
							 * 
							 * httpCon.setDoOutput(true);
							 * httpCon.setRequestMethod("PUT");
							 * OutputStreamWriter out = new OutputStreamWriter(
							 * httpCon.getOutputStream());
							 * out.write("Resource content"); out.close();
							 * httpCon.getInputStream();
							 * bw.write("[System] New user added\n");
							 * bw.flush(); status = true; } catch (IOException
							 * e) { e.printStackTrace(); }
							 */
							break;
						case "3": // user remove
							// bw.write("User to remove> \n");
							// bw.flush();
							username = br.readLine();
							if (!logins.containsKey(username)) {
								bw.write("[System] This username doesn't exist\n");
								bw.flush();
								break;
							} else {
								logins.remove(username);
								bw.write("[System] User removed\n");
								bw.flush();
								status = true;
							}
							break;
						case "4": // room listing
							Set<String> rooms = quasar.listRooms();
							String resS = "";
							for (String s : rooms)
								resS += s + " ";
							bw.write(resS + "\n");
							bw.flush();
							status = true;
							break;
						case "5": // room create

							// bw.write("desired roomname> \n");
							// bw.flush();
							username = br.readLine();
							// System.out.println("aqui\n");
							// status = quasar.createRoom(username);
							quasar.createRoom(username);
							// System.out.println("e tambem aqui\n");
							bw.write("[System] New room added\n");
							bw.flush();
							/*
							 * try { url = new URL(
							 * "http://localhost:8080/hello/rooms/add?name=" +
							 * username); httpCon = (HttpURLConnection) url
							 * .openConnection();
							 * 
							 * httpCon.setDoOutput(true);
							 * httpCon.setRequestMethod("PUT");
							 * OutputStreamWriter out = new OutputStreamWriter(
							 * httpCon.getOutputStream());
							 * out.write("Resource content"); out.close();
							 * httpCon.getInputStream();
							 * bw.write("[System] New room added\n");
							 * bw.flush(); status = true; } catch (IOException
							 * e) { e.printStackTrace(); }
							 */

							break;
						case "6": // room remove
							// bw.write("roomname to remove> \n");
							// bw.flush();
							username = br.readLine();
							// System.out.println("---" + username + "---");
							quasar.removeRoom(username);
							bw.write("[System] Room removed\n");
							bw.flush();
							break;

						case "7": // close connection
							bw.write("[System] Disconnect from server\n");
							bw.flush();
							socket.close();
							closeConnection = true;
							break;
						default:
							bw.write("[System] Please choose a correct option\n");
							bw.flush();
							break;
						}
					if (!status && !closeConnection) {
						bw.write("\n[System] Something went wrong\n[System] Please try again\n");
						bw.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
