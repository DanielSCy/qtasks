package daniel.stanciu.quicktasks;

public class MyTask extends MyTaskBase {
	public static final String STATUS_COMPLETED = "completed";
	public static final String NEEDS_ACTION = "needsAction";
	public static final int LOW_PRIORITY = 2;
	public static final int NORMAL_PRIORITY = 1;
	public static final int HIGH_PRIORITY = 0;
	private boolean checked;
	private String parentListId;
	private int priority;
	
	public MyTask(String id, String title) {
		super(id, title);
		priority = NORMAL_PRIORITY;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setParentListId(String listId) {
		parentListId = listId;
	}
	
	public String getParentListId() {
		return parentListId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
