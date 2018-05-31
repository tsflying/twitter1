package cn.fhj.twitter;

public class Conversation {
	private final String owner;

	private final String text;

	public Conversation(String owner, String text) {
		this.owner = owner;
		this.text = text;
	}

	public String getOwner() {
		return owner;
	}

	public String getText() {
		return text;
	}

	public String toString() {
		return "Conversation " + owner + ":" + text;
	}
}
