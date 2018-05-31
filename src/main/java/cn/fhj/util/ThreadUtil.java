package cn.fhj.util;

public class ThreadUtil {
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			// TODO IGNORE
		}
	}
}
