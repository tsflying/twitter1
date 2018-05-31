package cn.fhj.util;

public class MsgException extends RuntimeException {

	private static final long serialVersionUID = -1135244146569853246L;

	public MsgException(String msg) {
		super(msg);
	}

	public MsgException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public MsgException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
