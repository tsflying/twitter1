package cn.fhj.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import cn.fhj.util.HttpsUtil;
import cn.fhj.util.PicUtil;

public class Grid {
	private long id;
	private String text;

	private final Date date = new Date();

	private String sinaId;

	private String owner;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getDate() {
		return date;
	}

	public String getSinaId() {
		return sinaId;
	}

	public void setSinaId(String sinaId) {
		this.sinaId = sinaId;
	}

	private final List<String> pics = new ArrayList();

	private List<String> picFiles = new ArrayList();

	public List<String> getPicFiles() {
		return picFiles;
	}

	public final List<Conversation> conversations = new ArrayList();

	public void addConversation(String owner, String text) {
		conversations.add(new Conversation(owner, text));
	}

	public void simpleConverSations() {
		if (this.conversations.isEmpty()) {
			return;
		}
		String txt = this.text;
		List<Conversation> cvs = new ArrayList();
		cvs.addAll(this.conversations);
		this.conversations.clear();
		for (Conversation c : cvs) {
			String cTxt = simpleContent(c.getText());
			if (simpleContent(txt).indexOf(cTxt.length() > 5 ? cTxt.substring(0, cTxt.length() - 2) : cTxt) < 0) {
				this.conversations.add(c);
				txt = c.getText();
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(this.text);
		for (Conversation c : this.conversations) {
			sb.append(" //@").append(c.getOwner()).append(':').append(c.getText());
		}
		if (Twitter.weibo != null && !Twitter.weibo.isLarge(sb.toString())) {
			this.text = sb.toString();
			this.conversations.clear();
		}
	}

	protected String simpleContent(String text) {
		return text.replaceAll("@\\S+", "").replaceAll("\\s+", "");
	}

	public void addPic(String pic) {
		if (!pics.contains(pic)) {
			pics.add(pic);
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID:").append(id).append("\n");
		sb.append("Owner:").append(owner).append("\n");
		sb.append("text:").append(text).append("\n");
		sb.append("pics:").append(pics).append("\n");
		sb.append("conversation:\n").append(conversations).append("\n");
		return sb.toString();
	}

	public String creatPic() {
		this.picFiles = new ArrayList();
		if (pics.isEmpty() && conversations.isEmpty()) {
			return null;
		}
		int i = 0;
		try {
			for (String pic : pics) {
				picFiles.add(HttpsUtil.download(pic, Twitter.getDataFold() + i++));
			}
			return mergePics(i);
		} catch (Exception e) {
			LogFactory.getLog(Grid.class).warn("下载图片出错", e);
			return null;
		}
	}

	private String mergePicFile;

	public void setMergePicFile(String mergePicFile) {
		this.mergePicFile = mergePicFile;
	}

	public String getMergePicFile() {
		return mergePicFile;
	}

	protected String mergePics(int i) {
		int width = PicUtil.getWidth(picFiles);
		if (!conversations.isEmpty()) {
			String filename = Twitter.getDataFold() + i + ".jpg";
			PicUtil.createJpg(conversations, filename, width);
			picFiles.add(0, filename);
		}
		mergePicFile = PicUtil.mergeImage(picFiles);
		return mergePicFile;
	}

}
