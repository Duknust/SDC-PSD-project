package org.bc.hello.representations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Saying {
	public final long id;
	public final String content;
	public final List<String> users;
	public final List<String> rooms;

	@JsonCreator
	public Saying(@JsonProperty("id") long id,
			@JsonProperty("content") String content) {
		this.id = id;
		this.content = content;
		this.rooms = null;
		this.users = null;
	}

	@JsonCreator
	public Saying(@JsonProperty("id") long id,
			@JsonProperty("content") List<String> content) {
		this.id = id;
		this.users = null;
		this.rooms = content;
		this.content = null;
	}

}
