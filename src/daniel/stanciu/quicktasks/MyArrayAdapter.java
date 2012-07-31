package daniel.stanciu.quicktasks;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class MyArrayAdapter<T> extends ArrayAdapter<T> {
	
	public static final int ITEM_TYPE_LIST = 1;
	public static final int ITEM_TYPE_TASK = 2;
	public static final int ITEM_TYPE_UNKNOWN = 0;
	public static final int ITEM_TYPE_SELECTED_TASK = 3;
	public static final int ITEM_TYPE_SELECTED_LIST = 4;
    
    private LayoutInflater mInflater;
    private int editingPosition = -1;
    QuickTasksActivity activity;
    private EditText editText = null;

	public MyArrayAdapter(Context context, int textViewResourceId,
			List<T> objects, GestureDetector gestureDetector) {
		super(context, textViewResourceId, objects);
		activity = (QuickTasksActivity)context;
		
		init(context);
	}

    private void init(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		T item = getItem(position);
		if (item instanceof MyTask) {
			return getTaskView(position, convertView, parent);
		} else if (item instanceof MyTasksList) {
			return getTaskListView(position, convertView, parent);
		}
		return super.getView(position, convertView, parent);
	}

	@SuppressWarnings("unchecked")
	private View getTaskListView(int position, View convertView,
			ViewGroup parent) {
//		View view = super.getView(position, convertView, parent);
		MyTextView view = null;
		editText = null;
		if (convertView == null) {
			if (position == editingPosition) {
				editText = (EditText)mInflater.inflate(R.layout.listviewselected, parent, false); 
			} else {
				view = (MyTextView)mInflater.inflate(R.layout.listview, parent, false);
			}
		} else {
			view = (MyTextView)convertView;
		}
		MyTasksList item = (MyTasksList)getItem(position);
		if (position == editingPosition) {
			editText.setText(item.getTitle());
			editText.setTag(item);
			editText.requestLayout();
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					if (editText != null) {
						editText.requestFocus();
					}
				}
			});

			editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						MyTasksList list = (MyTasksList)v.getTag();
						int myPos = MyArrayAdapter.this.getPosition((T) list);
						if (myPos == editingPosition) {
							editingPosition = -1;
							editText = null;
						}

						String newTitle = ((EditText)v).getText().toString();
						if (!newTitle.equals(list.getTitle())) {
							list.setTitle(newTitle);
							//activity.getDbManager().updateTask(task);
							activity.getDbManager().updateList(list);
						}
						activity.replaceView(myPos);
					}
				}
			});
			
			return editText;			
		} else {
			view.setActivity(activity);
			view.setTag(getItem(position));
			view.setText(item.getTitle());
			view.setArrayAdapter((MyArrayAdapter<MyTaskBase>)this);
//			view.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					activity.currentList = (MyTasksList)v.getTag();
////					if (currentList.getId() == null) {
////						return;
////					}
//					activity.currentViewType = ViewType.TASKS;
//					activity.getDataFromDB();
//					activity.populateView();
//					//gotAccount();
//				}
//			});
	
			return view;
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		editingPosition = -1;
		editText = null;
		super.notifyDataSetChanged();
	}
	
	public int getColor(int attribute) {
		TypedValue typedValue = new TypedValue();
		activity.getTheme().resolveAttribute(attribute, typedValue, true);
		final int color = activity.getResources().getColor(typedValue.resourceId);
		return color;
	}

	@SuppressWarnings("unchecked")
	private View getTaskView(int position, View convertView, ViewGroup parent) {
		View view;
		CheckBox checkView;
		TextView textView = null;
		editText = null;
		MyTask item = (MyTask)getItem(position);
		
		if (convertView == null) {
			if (position == editingPosition) {
				view = mInflater.inflate(R.layout.taskviewselected, parent, false);
			} else {
				view = mInflater.inflate(R.layout.taskview, parent, false);
			}
		} else {
			view = convertView;
		}
		checkView = (CheckBox)view.findViewById(R.id.taskCheckBox);
		checkView.setText("");
		checkView.setChecked(item.isChecked());
		checkView.setTag(item);
		if (item.getPriority() == MyTask.HIGH_PRIORITY) {
			checkView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.high_pri_mark, 0, 0, 0);
		} else if (item.getPriority() == MyTask.LOW_PRIORITY) {
			checkView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.low_pri_mark, 0, 0, 0);
		} else {
			checkView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		checkView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MyTask task = (MyTask)buttonView.getTag();
				int position = MyArrayAdapter.this.getPosition((T)task);
				task.setChecked(isChecked);
				activity.getDbManager().updateTask(task);
				TextView view = (TextView)((View)buttonView.getParent()).findViewById(R.id.taskText);
				if (view != null) {
					setNotifyOnChange(false);
					if (isChecked) {
						view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						view.setTextColor(getColor(android.R.attr.textColorSecondary));
						activity.moveViewToEnd(position);
						remove((T)task);
						add((T)task);
					} else {
						int targetPos = getFirstPositionForPriority(task.getPriority() + 1);
						view.setPaintFlags(view.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
						view.setTextColor(getColor(android.R.attr.textColorPrimary));
						if (targetPos >= 0 && targetPos < position) {
							activity.moveViewToPosition(position, targetPos);
							MyArrayAdapter.this.remove((T)task);
							MyArrayAdapter.this.insert((T)task, targetPos);
						}
					}
					setNotifyOnChange(true);
				}
			}
		});

		if (position == editingPosition) {
			editText = (EditText)view.findViewById(R.id.taskEdit);
			editText.setText(item.getTitle());
			editText.setTag(item);
			editText.requestLayout();
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					if (editText != null) {
						editText.requestFocus();
					}
				}
			});
		} else {
			textView = (TextView)view.findViewById(R.id.taskText);
			textView.setText(item.getTitle());
			textView.setTag(item);
			((MyTextView)textView).setActivity(activity);
			((MyTextView)textView).setArrayAdapter((MyArrayAdapter<MyTaskBase>)this);
			if (item.isChecked()) {
				textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				textView.setTextColor(getColor(android.R.attr.textColorSecondary));
			}
		}
		
		if (textView != null) {
//			textView.setOnClickListener(new View.OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					editingPosition = ((Integer)v.getTag()).intValue();
//					activity.clearFocus();
//					activity.replaceView(editingPosition);
//				}
//			});
		}
		if (position == editingPosition) {
			editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (!hasFocus) {
						MyTask task = (MyTask)v.getTag();
						int myPos = MyArrayAdapter.this.getPosition((T)task);
						if (myPos == editingPosition) {
							editingPosition = -1;
							editText = null;
						}
						String newTitle = ((EditText)v).getText().toString();
						if (!newTitle.equals(task.getTitle())) {
							task.setTitle(newTitle);
							activity.getDbManager().updateTask(task);
						}
						activity.replaceView(myPos);
					}
				}
			});
		}
		
		return view;
	}

	protected int getFirstCheckedPosition() {
		for (int i = 0; i < getCount(); ++i) {
			MyTask task = (MyTask)getItem(i);
			if (task.isChecked())
				return i;
		}
		return -1;
	}
	
	protected int getFirstPositionForPriority(int priority) {
		for (int i = 0; i < getCount(); ++i) {
			MyTask task = (MyTask)getItem(i);
			if (task.isChecked() || task.getPriority() == priority) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		T item = getItem(position);
		if (item instanceof MyTask) {
			if (editingPosition == position) {
				return ITEM_TYPE_SELECTED_TASK;
			} else {
				return ITEM_TYPE_TASK;
			}
		} else  if (item instanceof MyTasksList) {
			if (editingPosition == position) {
				return ITEM_TYPE_SELECTED_LIST;
			} else {
				return ITEM_TYPE_LIST;
			}
		} else {
			return ITEM_TYPE_UNKNOWN;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}
	
	public boolean exitEditMode() {
		if (editingPosition != -1) {
			int position = editingPosition;
			editingPosition = -1;
			activity.replaceView(position);
			return true;
		}
		return false;
	}

	public void setEditingPosition(int editingPosition) {
		this.editingPosition = editingPosition;
	}
	
}
