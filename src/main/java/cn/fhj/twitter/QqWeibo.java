//package cn.fhj.twitter;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import cn.fhj.util.HttpsUtil;
//import cn.fhj.util.ThreadUtil;
//
//public class QqWeibo extends Weibo {
//
//	public QqWeibo(String cks) {
//		util = HttpsUtil.getQqInstance(cks);
//		String msg = isLogined() ? "Succes logined QQ" : "Failed to login";
//		showMessage(msg + " " + this.getClass().getSimpleName());
//	}
//
//	private boolean isLogined() {
//		String html = util.doGetForString("http://w.t.qq.com/touch");
//		this.setReady(html.indexOf("icon_3g write") > 0);
//		return this.isReady();
//	}
//
//	public String send(Grid grid) {
//		Map<String, String> formData = new HashMap();
//		formData.put("content", trim(grid.getText()).replace("//@", "//").replaceAll("@", " "));
//		if (grid.getMergePicFile() != null) {
//			showMessage("发送图片...");
//			formData.put("pic", uploadPic(grid.getMergePicFile()));
//			ThreadUtil.sleep((long) (1000 * 10 * (1 + Math.random() * 5)));
//		}
//		formData.put("apiType", String.valueOf(17));
//		formData.put("latitude", "");
//		formData.put("longitude", "");
//		formData.put("apiHost", "http://w.t.qq.com");
//		for (int i = 0; i < 3; i++) {
//			showMessage("发送文字。。。");
//			String result = util.post("http://api.t.qq.com/old/publish.php?_=" + System.currentTimeMillis(), formData,
//					null, null);
//			if (result != null) {
//				String sinaId = getSinaId(result);
//				if (sinaId != null) {
//					grid.setSinaId(sinaId);
//					this.getRecentGrids().add(grid);
//					return sinaId;
//				}
//			}
//			showMessage("发送失败：" + result);
//			sleepAfterSend();
//		}
//		return null;
//	}
//
//	public long getSleep() {
//		return 59000;
//	}
//
//	public boolean shouldSave() {
//		return false;
//	}
//
//	private String getSinaId(String s) {
//		String beginTag = "[{\"id\":\"";
//		int begin = s.indexOf(beginTag) + beginTag.length();
//		int end = s.indexOf("\",\"content\":");
//		if (begin > 0 && end > 0) {
//			return s.substring(begin, end);
//		}
//		return null;
//	}
//
//	protected String uploadPic(String pic) {
//		Map<String, String> formData = new HashMap();
//		String s = util.post("http://upload.t.qq.com/asyn/uploadpic.php?g_tk=123456&retType=3", formData, "pic", pic);
//		if (s == null) {
//			System.out.print(s);
//			return null;
//		}
//		String beginTag = "'image':'";
//		int begin = s.indexOf(beginTag) + beginTag.length();
//		int end = s.indexOf("','path':'");
//		if (begin > 0 && end > 0) {
//			return s.substring(begin, end);
//		}
//		return null;
//	}
//
//	public void delete(String sinaId) {
//		Map<String, String> formData = new HashMap();
//		formData.put("id", sinaId);
//		formData.put("apiType", String.valueOf(17));
//		formData.put("apiHost", "http://w.t.qq.com");
//		util.post("http://api.t.qq.com/old/delete.php?_=" + System.currentTimeMillis(), formData, null, null);
//	}
//
//	private static final String URL_PATTERN = "(http://|https://)[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
//
//	public String trim(String text) {
//		int count = 0;
//		for (char c : text.toCharArray()) {
//			if (c < 256) {
//				count++;
//			}
//		}
//		int dif = text.length() - getWeiboLength() - count / 2;
//		if (dif <= 0) {
//			return text;
//		}
//
//		Pattern pattern = Pattern.compile(URL_PATTERN);
//		// 空格结束
//		Matcher matcher = pattern.matcher(text);
//		while (matcher.find()) {
//			String s = matcher.group(0);
//			dif -= s.length() / 2 - 10;
//			System.out.println(s);
//			if (dif <= 0) {
//				return text;
//			}
//		}
//		return trim(text.substring(0, text.length() - dif));
//	}
//
//	public void refresh() {
//		Map<String, String> formData = new HashMap();
//		formData.put("type", "1,2,3,4,notice");
//		formData.put("apiType", String.valueOf(17));
//		formData.put("apiHost", "http://w.t.qq.com");
//		util.doGetForString("http://message.t.qq.com/newMsgCount.php", formData);
//	}
//
//	@Override
//	public String getName() {
//		return "QQ";
//	}
//}
