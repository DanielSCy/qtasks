package daniel.stanciu.quicktasks;

public class MyTask extends MyTaskBase {
	public static final String STATUS_COMPLETED = "completed";
	public static final String NEEDS_ACTION = "needsAction";
	private boolean checked;
	private String parentListId;
	
	public MyTask(String id, String title) {
		super(id, title);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setParentListId(String listId) {
		// TODO Auto-generated method stub
		parentListId = listId;
	}
	
	public String getParentListId() {
		return parentListId;
	}
}
