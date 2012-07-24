package daniel.stanciu.quicktasks;

public class MyTaskBase {
	private String id;
	private String title;
	private long internalId;
	private boolean deleted;
	
	@Override
	public String toString() {
		return title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public MyTaskBase(String id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public MyTaskBase() {
		id = null;
		title = null;
	}

	public long getInternalId() {
		return internalId;
	}

	public void setInternalId(long internalId) {
		this.internalId = internalId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
