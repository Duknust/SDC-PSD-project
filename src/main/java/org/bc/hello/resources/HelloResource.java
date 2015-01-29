package org.bc.hello.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bc.hello.representations.Saying;
import org.bc.server.Quasar;

import com.google.common.base.Optional;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {
	private final String template;
	private volatile String defaultName;
	private long counter;
	static HashMap<String, String> users = null;
	static Quasar quasar = null;

	static String PREFIX_ROOM = "http://localhost:8080/hello/rooms/";

	static String PATH_ROOMS = "http://localhost:8080/hello/rooms/";
	static String PATH_USERS = "http://localhost:8080/hello/users/";
	static String PATH_FAQ = "http://localhost:8080/hello/faq/";
	static String PATH_MAIN_PAGE = "http://localhost:8080/hello/teste/";
	static String PATH_STATS = "http://localhost:8080/hello/stats/";

	static String START_HTML = "<!DOCTYPE html><html><head>"
			+ "<style>table {width:auto;}\n"
			+ "table, th, td {border: 1px solid black;border-collapse: collapse;}\n"
			+ "th, td {padding: 5px;text-align: left;}\n"
			+ "table#t01 tr:nth-child(even) {background-color: #eee;}\n"
			+ "table#t01 tr:nth-child(odd) {background-color:#fff;}\n"
			+ "table#t01 th {background-color: black;\n" + "color: white;}\n"
			+ "#menu {position: fixed;" + "right: 0;" + "top: 0%;"
			+ "width: 8em;" + "margin-top: 0.5em;}" + "</style></head><body>\n"
			+ "<a id=\"menu\">" + "<a href=\"" + PATH_MAIN_PAGE
			+ "\">BananaChat</a> | " + "<a href=\"" + PATH_USERS
			+ "\">List of Users</a> | " + "<a href=\"" + PATH_ROOMS
			+ "\">List of Rooms</a> | " + "<a href=\"" + PATH_STATS
			+ "\">Stats</a> | " + "<a href=\"" + PATH_FAQ + "\">FAQ</a>"
			+ "</a>";

	public HelloResource(String template, String defaultName,
			HashMap<String, String> theOthers, Quasar quasarServer) {
		this.template = template;
		this.defaultName = defaultName;
		users = theOthers;
		quasar = quasarServer;
	}

	@GET
	public Saying sayHello(@QueryParam("name") Optional<String> name) {
		final String content = String.format(template, name.or(defaultName));
		long i;
		synchronized (this) {
			counter++;
			i = counter;
		}
		return new Saying(i, content);
	}

	@PUT
	@Path("/default/{name}")
	public Response put(@PathParam("name") String name) {
		defaultName = name;
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/stats/")
	public String getStats(@QueryParam("name") Optional<String> name) {
		// final String content = String.format(template, name.or(defaultName));
		long i;
		synchronized (this) {
			counter++;
			i = counter;
		}
		/*
		 * List<String> userNames = new ArrayList<String>(); for (String a :
		 * users.keySet()) userNames.add(a);
		 */

		int onlineUsers = quasar.getNumberOfLoggedInUsers();
		int numberUsers = quasar.getNumberOfRegisteredUsers();
		int numberRooms = quasar.getNumberOfRooms();

		String res = START_HTML
				+ "<p><strong>Stats</strong></p>\n<table id=\"t01\">";

		res += "<tr><td>#Users Online</td><td><font color=green>" + onlineUsers
				+ "</font></td><tr>\n";
		res += "<tr><td>#Total Users</td><td>" + numberUsers + "</td><tr>\n";
		res += "<tr><td>#Rooms</td><td>" + numberRooms + "</td><tr>\n";

		res += "</tr></table></body></html>";
		return res;

		// return new Saying(i, new ArrayList<String>(userNames));
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/users/")
	public String getUsers(@QueryParam("name") Optional<String> name) {
		// final String content = String.format(template, name.or(defaultName));
		long i;
		int numb = 1;
		synchronized (this) {
			counter++;
			i = counter;
		}
		/*
		 * List<String> userNames = new ArrayList<String>(); for (String a :
		 * users.keySet()) userNames.add(a);
		 */
		Set<String> userNames = quasar.listUsers();

		String res = START_HTML
				+ "<p><strong>List of Users</strong></p>\n<table id=\"t01\">"
				+ "<tr><th>Number</th>\n<th>Username</th></tr>\n<tr>";

		for (String room : userNames) {
			res += "<tr><td>" + numb + "</td><td><a href=\"" + PREFIX_ROOM
					+ room + "\">" + room + "</a></td><tr>\n";
			numb++;
		}

		res += "</tr></table></body></html>";
		return res;

		// return new Saying(i, new ArrayList<String>(userNames));
	}

	@PUT
	@Path("/users/add")
	public Response putUser(@QueryParam("name") String name) {
		// defaultName = name;
		quasar.createUser(name);
		return Response.ok().build();
	}

	@DELETE
	@Path("/users/remove/{name}")
	public Response removeUser(@PathParam("name") String name) {
		// defaultName = name;
		quasar.removeUser(name);
		return Response.ok().build();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/rooms/")
	public String getRooms(@QueryParam("name") Optional<String> name) {
		// final String content = String.format(template, name.or(defaultName));
		long i;
		int numb = 1;
		synchronized (this) {
			counter++;
			i = counter;
		}
		Set<String> roomNames = quasar.listRooms();

		String res = START_HTML
				+ "<p><strong>List of Rooms</strong></p>\n<table id=\"t01\">"
				+ "<tr><th>Number</th>\n<th>Room Name</th></tr>\n<tr>";

		for (String room : roomNames) {
			res += "<tr><td>" + numb + "</td><td><a href=\"" + PREFIX_ROOM
					+ room + "\">" + room + "</a></td><tr>\n";
			numb++;
		}

		res += "</tr></table></body></html>";
		return res;
		// return new Saying(i, new ArrayList<String>(roomNames));
	}

	@GET
	@Path("/rooms/{roomName}")
	@Produces(MediaType.TEXT_HTML)
	public String getUserFromRoom(@PathParam("roomName") String roomName) {
		// final String content = String
		// .format(template, roomName.or(defaultName));
		long i;
		int numb = 1;
		synchronized (this) {
			counter++;
			i = counter;
		}
		List<String> users = quasar.listRoomUsers(roomName);

		String res = START_HTML + "<p><strong>List of Users in Room "
				+ roomName + "</strong></p>\n<table id=\"t01\">"
				+ "<tr><th>Number</th>\n<th>Room Name</th></tr>\n<tr>";

		for (String room : users) {
			res += "<tr><td>" + numb + "</td><td><a href=\"" + PREFIX_ROOM
					+ room + "\">" + room + "</a></td><tr>\n";
			numb++;
		}

		res += "</tr></table></body></html>";
		return res;
	}

	@PUT
	@Path("/rooms/add")
	public Response putRoom(@QueryParam("name") String name) {
		// defaultName = name;
		quasar.createRoom(name);
		return Response.ok().build();
	}

	@DELETE
	@Path("/rooms/remove/{name}")
	public Response removeRoom(@PathParam("name") String name) {
		// defaultName = name;
		quasar.removeRoom(name);
		return Response.ok().build();
	}

	@GET
	@Path("/faq/")
	@Produces(MediaType.TEXT_HTML)
	public String sayHtmlHello() {
		return START_HTML
				+ "<h1 padding: 5px 10px>"
				+ "<strong>Welcome to <em>BananaChat</em></strong></h1>"
				+ "  <h3 style=\"border: 1px solid rgb(204, 204, 204); padding: 5px 10px; background: rgb(238, 238, 238);\">"
				+ " We offer a <span style=\"line-height: 20.7999992370605px;\">"
				+ "communication over the Internet in a real-time transmission of text messages from sender to receiver."
				+ " This </span><span style=\"line-height: 1.6;\">"
				+ "Online Chat may address point-to-point communications "
				+ "(called <em>Private Messages</em>) as well as multicast communications "
				+ "from one sender to many receivers (<em>Chat Rooms</em>).</span></h3>  "
				+ "<h2>Forget all this text, We want your <span class=\"marker\">attention</span>&nbsp;"
				+ "on what&#39;s really important. You know what you need to use this <ins>"
				+ "AMAZING SERVICE</ins> ?</h2>  <ul>  <li>A computer.</li>  <li>Be alive.</li> "
				+ " <li>and a Keyboard.</li> </ul>  <h2>And It is totally&nbsp;<strong>FREE * </strong>!</h2>  "
				+ "<p>&nbsp;</p>  <p>What are you waiting for ?</p>  <h1>Join <strong>NOW </strong>!</h1>"
				+ "<h3> <strong>*</strong> But you can donate so we can make some money !</h3>"
				+ "<p><img alt=\"Please ! Please ! We beg you !\" src=\"http://theimho.org/sites/default/files/"
				+ "paypal-donate-button.gif\" style=\"width: 300px; height: 70px;\" /></p>"
				+ "</body></html>";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/teste/")
	public String teste(@QueryParam("name") Optional<String> name) {
		// final String content = String.format(template, name.or(defaultName));
		long i;
		int numb = 1;
		synchronized (this) {
			counter++;
			i = counter;
		}

		String res = START_HTML;

		res += "</tr></table></body></html>";
		return res;

	}
}
