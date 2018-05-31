package cn.fhj.util;

import org.apache.log4j.Logger;

public class LogUtil {
	public static Logger getLog(Class clazz) {
		return Logger.getLogger(clazz);
	}
}

