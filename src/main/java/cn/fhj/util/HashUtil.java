package cn.fhj.util;

/**
 * Hash值相关的计算处理
 * 
 * @since fan houjun 2008-4-11
 */
public class HashUtil {

	/**
	 * 判断两个对象是否相等
	 */
	public static boolean isEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o1 instanceof Object[]) {
			if (o2 instanceof Object[]) {
				Object[] a1 = (Object[]) o1;
				Object[] a2 = (Object[]) o2;
				if (a1.length == a2.length) {
					for (int i = 0; i < a1.length; i++) {
						if (!HashUtil.isEqual(a1[i], a2[i])) {
							return false;
						}
					}
					return true;
				}
				return false;

			} else {
				return false;
			}
		}
		return o1.equals(o2);
	}

	/**
	 * 计算组合对象的hash值
	 */
	public static int calHash(Object... args) {
		int k = 23;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				int c;
				if (args[i] instanceof Object[]) {
					c = calHash((Object[]) args[i]);
				} else {
					c = args[i] == null ? 0 : args[i].hashCode();
				}
				k = k * 31 + c;
			}
		}
		return k;
	}
}
