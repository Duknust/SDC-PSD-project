package org.bc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bc.pub_sub.Publisher;
import org.bc.pub_sub.Subscriber;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberServerSocketChannel;
import co.paralleluniverse.fibers.io.FiberSocketChannel;

public class Quasar {
	public static final String MAIN_ROOM = "mainRoom";
	private static HashMap<String, Room> rooms = new HashMap<String, Room>();
	private static HashMap<String, ActorRef<Msg>> users = new HashMap<String, ActorRef<Quasar.Msg>>();
	private static HashMap<String, String> logins = new HashMap<String, String>();
	private static Publisher publisher = null;
	static final String JOIN_ROOM_COMMAND = "/jr";
	static final String MAIN_ROOM_COMMAND = "/mr";
	static final String PRIVATE_MESSAGE_COMMAND = "/pm";
	static final String LOGIN_COMMAND = "/li";
	static final String HELP_COMMAND = "/??";
	static final String LIST_ROOMS_COMMAND = "/lr";
	static final String ALL_COMMANDS = "/jr - join room\n/mr - go to main room\n/pm - private message\n/li - login\n/lo - logout\n/lr - list available rooms\n\n";
	static final String SUBSCRIBE_UPDATE_COMMAND = "/su";

	static int MAXLEN = 1024;

	static enum Type {
		DATA, EOF, IOE, ENTER, LEAVE, LINE, LOGON, PM, HELP, SUPDATE
	}

	public static class Msg {
		final Type type;
		final Object o; // careful with mutable objects, such as the byte array
		final String sender;

		Msg(Type type, Object o) {
			this.type = type;
			this.o = o;
			this.sender = null;
		}

		Msg(Type type, Object o, String sender) {
			this.type = type;
			this.o = o;
			this.sender = sender;
		}
	}

	static class LineReader extends BasicActor<Msg, Void> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1186997829580986514L;
		final ActorRef<Msg> userRef;
		final FiberSocketChannel socket;
		ByteBuffer in = ByteBuffer.allocate(MAXLEN);
		ByteBuffer out = ByteBuffer.allocate(MAXLEN);

		LineReader(ActorRef<Msg> dest, FiberSocketChannel socket) {
			this.userRef = dest;
			this.socket = socket;
		}

		@Override
		protected Void doRun() throws InterruptedException, SuspendExecution {
			boolean eof = false;
			byte b = 0;
			try {
				for (;;) {
					if (socket.read(in) <= 0)
						eof = true;
					in.flip();
					while (in.hasRemaining()) {
						b = in.get();
						out.put(b);
						if (b == '\n')
							break;
					}
					if (eof || b == '\n') { // send line
						out.flip();
						if (out.remaining() > 0) {
							boolean msgAlreadyProcessed = false;
							byte[] ba = new byte[out.remaining()];
							out.get(ba);
							out.clear();
							String msg = new String(ba).trim();
							// System.out.println("xx " + msg);
							if (msg.length() >= 3) {
								// System.out
								// .println("xxx " + msg.substring(0, 3));
								switch (msg.substring(0, 3)) {
								case JOIN_ROOM_COMMAND:
									String room = msg
											.substring(4, msg.length()).trim();
									Room newRoom = rooms.get(room);
									if (newRoom != null) {
										userRef.send(new Msg(Type.ENTER,
												newRoom, null));
									} else {
										userRef.send(new Msg(Type.LINE,
												"[System] No such room\n"
														.getBytes()));
									}
									msgAlreadyProcessed = true;
									// System.out.println("JOIN ROOM\n");
									break;
								case PRIVATE_MESSAGE_COMMAND:
									String pm = msg.substring(4, msg.length())
											.trim();
									userRef.send(new Msg(Type.PM, pm));
									msgAlreadyProcessed = true;
									// System.out.println("PM\n");
									break;
								case MAIN_ROOM_COMMAND:
									userRef.send(new Msg(Type.ENTER, rooms
											.get(Quasar.MAIN_ROOM)));
									msgAlreadyProcessed = true;
									// System.out.println("MAIN ROOM\n");
									break;
								case LOGIN_COMMAND:
									String user = msg
											.substring(4, msg.length()).trim();
									userRef.send(new Msg(Type.LOGON, user));
									msgAlreadyProcessed = true;
									// System.out.println("LOGON\n");
									break;
								case SUBSCRIBE_UPDATE_COMMAND:
									String channels = msg.substring(4,
											msg.length()).trim();
									userRef.send(new Msg(Type.SUPDATE, channels));
									msgAlreadyProcessed = true;
									break;
								case HELP_COMMAND:
									userRef.send(new Msg(Type.HELP, null));
									msgAlreadyProcessed = true;
									break;
								case LIST_ROOMS_COMMAND:
									String lr = "";
									for (String r : rooms.keySet())
										lr += r + " ";
									userRef.send(new Msg(Type.LINE, lr
											.getBytes()));
									msgAlreadyProcessed = true;
								}
							} /*
							 * else { userRef.send(new Msg(Type.HELP, null));
							 * msgAlreadyProcessed = true; break; }
							 */
							if (!msgAlreadyProcessed)
								userRef.send(new Msg(Type.DATA, ba));
						}
					}
					if (eof && !in.hasRemaining())
						break;
					in.compact();
				}
				userRef.send(new Msg(Type.EOF, null));
				return null;
			} catch (IOException e) {
				userRef.send(new Msg(Type.IOE, null));
				return null;
			}
		}

	}

	static class User extends BasicActor<Msg, Void> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3565381113105762409L;
		ActorRef<Msg> room;
		final FiberSocketChannel socket;
		String username;
		boolean loggedIn = false;
		boolean clientStatusUpdates = false;
		boolean roomStatusUpdates = false;
		Subscriber subscriber = null;

		public User(ActorRef<Msg> room, FiberSocketChannel socket) {
			this.room = room;
			this.socket = socket;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Void doRun() throws InterruptedException, SuspendExecution {
			new LineReader(self(), socket).spawn();
			while (receive(msg -> {
				try {
					switch (msg.type) {
					case LOGON:
						String credentialStr = (String) msg.o;
						String credentials[] = credentialStr.split(":");
						this.username = credentials[0];
						String password = credentials[1];
						if (logins.containsKey(this.username))
							if (logins.get(username).equals(password)) {
								users.put(username, self());
								room.send(new Msg(Type.ENTER, new UserEntry(
										self(), this.username)));
								loggedIn = true;
								self().send(new Msg(Type.LINE, "ok".getBytes()));
							} else {
								self().send(
										new Msg(Type.LINE, "notok".getBytes()));
								this.socket.close();
							}
						break;
					case DATA:
						if (loggedIn) {
							room.send(new Msg(Type.LINE, msg.o, this.username));
							return true;
						} else {
							String x = new String("[System] Not logged in\n");
							socket.write(ByteBuffer.wrap(x.getBytes()));
						}
						break;
					case EOF:
					case IOE:
						room.send(new Msg(Type.LEAVE, new UserEntry(self(),
								username)));
						socket.close();
						return false;
					case LINE:
						if (loggedIn) {

							String toSend = (msg.sender == null ? "" : "("
									+ msg.sender + ") ")
									+ new String((byte[]) msg.o) + "\n";
							socket.write(ByteBuffer.wrap(toSend.getBytes()));
							return true;
						} else {
							String x = new String("[System] Not logged in\n");
							socket.write(ByteBuffer.wrap(x.getBytes()));
						}
						break;
					case ENTER:// mudar de sala
						if (loggedIn) {
							if (msg.o != null) {
								room.send(new Msg(Type.LEAVE, new UserEntry(
										self(), username)));
								room = ((Room) msg.o).ref();
								room.send(new Msg(Type.ENTER, new UserEntry(
										self(), username)));
							}
							return true;
						} else {
							String x = new String("[System] Not logged in\n");
							socket.write(ByteBuffer.wrap(x.getBytes()));
						}
						break;
					case PM:
						if (loggedIn) {
							String userDest = getUserFromPM((String) msg.o);
							String pm = getPmFromPM((String) msg.o);
							pm = "[from: " + username + "] " + pm;
							ActorRef<Msg> user = users.get(userDest);
							if (user != null) {
								user.send(new Msg(Type.LINE, (pm + "\n")
										.getBytes()));
							} else {
								socket.write(ByteBuffer
										.wrap("[System] No such user\n"
												.getBytes()));
							}
							return true;
						} else {
							String x = new String("[System] Not logged in\n");
							socket.write(ByteBuffer.wrap(x.getBytes()));
						}
						break;
					case HELP:
						socket.write(ByteBuffer.wrap(Quasar.ALL_COMMANDS
								.getBytes()));
						break;
					case SUPDATE:
						if (loggedIn) {
							clientStatusUpdates = (clientStatusUpdates ? false
									: true);

							if (!clientStatusUpdates) {
								subscriber.stopSubscriber();
							} else {
								if (msg.o != null) {
									ArrayList<String> channelList = new ArrayList<String>();
									String channels[] = ((String) msg.o)
											.split(" ");
									for (String s : channels)
										channelList.add("[" + s.toLowerCase()
												+ "]");
									if (subscriber == null) {
										subscriber = new Subscriber(12346,
												channelList.toArray(channels),
												socket);
										subscriber.spawn();
									} else
										subscriber
												.subscribeNewChannels(channels);
								}
							}
							return true;
						} else {
							String x = new String("[System] Not logged in\n");
							socket.write(ByteBuffer.wrap(x.getBytes()));
							return false;
						}
					case LEAVE:
						break;
					default:
						break;
					}
					return true;
				} catch (IOException | RuntimeException e) {
					e.printStackTrace();
					room.send(new Msg(Type.LEAVE, new UserEntry(self(),
							username)));

				}
				return false; // stops the actor if some unexpected message is
								// received
			}))
				;
			return null;
		}

		private String getPmFromPM(String o) {
			return o.replaceFirst(".*\\s", "");
		}

		private String getUserFromPM(String o) {
			return o.split(" ")[0];
		}

		public void sendUpdate(String string) {
			try {
				socket.write(ByteBuffer.wrap(string.getBytes()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	static class Room extends BasicActor<Msg, Void> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2219219030368076253L;
		private HashMap<String, ActorRef<Msg>> users = new HashMap<String, ActorRef<Msg>>();
		private String roomName;

		public Room(String string) {
			this.roomName = string;
			publisher.publishChannelAdded("[Room] " + roomName + " added\n");
		}

		public Set<String> getUsers() {
			return this.users.keySet();
		}

		@Override
		protected Void doRun() throws InterruptedException, SuspendExecution {
			while (receive(msg -> {
				switch (msg.type) {
				case ENTER:
					UserEntry userEntry = (UserEntry) msg.o;
					if (userEntry.username != null) {
						users.put(userEntry.username, userEntry.user);
						// if (!MAIN_ROOM.equals(roomName))
						publisher.publishClientLogin("["
								+ this.roomName.toLowerCase() + "] Client "
								+ userEntry.username + " joined\n");
					}
					return true;
				case LEAVE:
					UserEntry userEntry2 = (UserEntry) msg.o;
					users.remove(userEntry2.username);
					publisher.publishClientLogout("["
							+ this.roomName.toLowerCase() + "] Client "
							+ userEntry2.username + " left\n");
					return true;
				case LINE:
					for (String uname : users.keySet())
						if (!uname.equals(msg.sender)) {
							users.get(uname).send(msg);
						}
					return true;
				default:
					return false;
				}
			}))
				;

			return null;
		}
	}

	static class Acceptor extends BasicActor<Object, Object> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2973989879672144704L;
		final int port;
		final ActorRef<Msg> room;

		Acceptor(int port, ActorRef<Msg> room) {
			this.port = port;
			this.room = room;
		}

		@Override
		protected Void doRun() throws InterruptedException, SuspendExecution {
			try {
				FiberServerSocketChannel ss = FiberServerSocketChannel.open();
				ss.bind(new InetSocketAddress(port));
				while (true) {

					FiberSocketChannel socket = ss.accept();
					ActorRef<Msg> user = new User(room, socket).spawn();
					room.send(new Msg(Type.ENTER, new UserEntry(user, null)));

				}
			} catch (IOException e) {
			}
			return null;
		}
	}

	public void startQuasar(int port) throws Exception {
		// int port = 12345; // Integer.parseInt(args[0]);

		startZMQ(12346);

		Room room = new Room(Quasar.MAIN_ROOM);
		Room room2 = new Room("Benfica");
		Room room3 = new Room("NelsonMandela");
		// users = new HashMap<String, ActorRef<Quasar.Msg>>();
		// rooms = new HashMap<String, Room>();
		// logins = new HashMap<String, String>();
		logins.put("user", "pass");
		logins.put("user2", "pass");
		rooms.put(Quasar.MAIN_ROOM, room);
		rooms.put("Benfica", room2);
		rooms.put("NelsonMandela", room3);

		room2.spawn();
		room3.spawn();

		Acceptor acceptor = new Acceptor(port, room.spawn());
		acceptor.spawn();
		acceptor.join();
	}

	public boolean createRoom(String roomName) {
		boolean res = false;
		if (roomName != null) {
			Room room = new Room(roomName);
			room.spawn();
			rooms.put(roomName, room);
			res = true;
		}
		return res;
	}

	public Set<String> listRooms() {
		return rooms.keySet();
	}

	public List<String> listRoomUsers(String roomName) {
		Room room = rooms.get(roomName);
		List<String> users = new ArrayList<String>();
		for (String user : room.getUsers())
			users.add(user);

		return users;
	}

	public boolean removeRoom(String roomName) {
		System.out.println("entrei no remove");
		boolean res = false;
		if (roomName != null) {
			Room room = rooms.get(roomName);
			if (room != null) {
				rooms.remove(roomName);
				// room.close();
				res = true;
			}
		}
		return res;
	}

	public boolean createUser(String userAndPass) {
		boolean res = false;
		if (userAndPass != null)
			if (userAndPass.contains("-")) {
				String[] items = userAndPass.split("-");
				logins.put(items[0], items[1]);
				res = true;
			}
		return res;
	}

	public boolean removeUser(String user) {
		boolean res = false;
		if (user != null) {
			logins.remove(user);
			res = true;
		}
		return res;
	}

	public int getNumberOfRegisteredUsers() {
		return logins.size();
	}

	public int getNumberOfLoggedInUsers() {
		return users.size();
	}

	public int getNumberOfRooms() {
		return rooms.size();
	}

	private static void startZMQ(int port) {
		publisher = new Publisher();
		publisher.startPublisher(port);
	}

	static class UserEntry {
		ActorRef<Msg> user = null;
		String username = null;

		public UserEntry(ActorRef<Msg> user, String username) {
			super();
			this.user = user;
			this.username = username;
		}
	}

	public HashMap<String, String> getLoginsHash() {
		return logins;
	}

	public Set<String> listUsers() {
		return logins.keySet();
	}
}
