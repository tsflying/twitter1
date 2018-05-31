package cn.fhj.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class HtmUtil {
	public static Element getBody(String html) {
		return Jsoup.parseBodyFragment(html).body();
	}

}
