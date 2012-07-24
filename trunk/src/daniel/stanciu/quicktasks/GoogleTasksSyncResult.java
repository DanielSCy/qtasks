package daniel.stanciu.quicktasks;

public class GoogleTasksSyncResult {
	private int status;
	private String message;
	
	public static final int OK = 0;
	public static final int RETRY = 1;
	public static final int FAILED = 2;
	
	public static final GoogleTasksSyncResult SUCCESS = new GoogleTasksSyncResult(OK, "");
	public static final GoogleTasksSyncResult AUTH_RETRY = new GoogleTasksSyncResult(RETRY, "Authentication failure");
	
	public GoogleTasksSyncResult(int status, String message) {
		this.setStatus(status);
		this.setMessage(message);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
