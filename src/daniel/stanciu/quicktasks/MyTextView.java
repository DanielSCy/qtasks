package daniel.stanciu.quicktasks;

import daniel.stanciu.quicktasks.MyGestureDetector.OnDoubleTapListener;
import daniel.stanciu.quicktasks.MyGestureDetector.OnGestureListener;
import android.content.Context;
//import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.MotionEvent;
//import android.view.View;
//import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MyTextView extends TextView implements OnGestureListener, OnDoubleTapListener {
	
	private static final int DELETE_THRESHOLD = 200;
	private QuickTasksActivity activity;
	private MyGestureDetector gd;
	private MyArrayAdapter<MyTaskBase> arrayAdapter;
	private MotionEvent doubleTapStart = null;
	
	private MyScrollView scrollView = null;
	private boolean findParentsCalled = false;
	private boolean disableTouch = false;

	public MyTextView(Context context) {
		super(context);
		gd = new MyGestureDetector(context, this, null, false);
		gd.setOnDoubleTapListener(this);
		//gd.setIsLongpressEnabled(false);
	}

	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		gd = new MyGestureDetector(context, this, null, false);
		gd.setOnDoubleTapListener(this);
		//gd.setIsLongpressEnabled(false);
	}

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gd = new MyGestureDetector(context, this, null, false);
		gd.setOnDoubleTapListener(this);
		//gd.setIsLongpressEnabled(false);
	}

	public void setActivity(QuickTasksActivity act) {
		activity = act;
	}
	
	private void findImportantParents() {
		findParentsCalled = true;
		ViewParent parent = this.getParent();
		while (parent != null) {
			if (parent instanceof MyScrollView) {
				scrollView = (MyScrollView)parent;
				return;
			}
			parent = parent.getParent();
		}
	}
	
	private MyScrollView getScrollViewParent() {
		if (!findParentsCalled) {
			findImportantParents();
		}
		return scrollView;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.d("MyTextView", "Event received " + event.getActionMasked());
		if (disableTouch) {
			return false;
		}
		if(!gd.onTouchEvent(event))
			super.onTouchEvent(event);
		
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (activity.currentViewType == ViewType.LISTS) {
			// TODO: edit current list
			MyTasksList list = (MyTasksList)getTag();
			int position = arrayAdapter.getPosition(list);
			arrayAdapter.setEditingPosition(position);
			activity.clearFocus();
			activity.replaceView(position);
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (activity.currentViewType == ViewType.TASKS) {
			MyTask task = (MyTask)getTag();
			int position = arrayAdapter.getPosition(task);
			arrayAdapter.setEditingPosition(position);
			activity.clearFocus();
			disableTouch = true;
			activity.replaceView(position);
			return true;
		} else {
			activity.currentList = (MyTasksList)getTag();
			activity.currentViewType = ViewType.TASKS;
			activity.getDataFromDB();
			activity.populateView();
		}
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		doubleTapStart = e;
//		Log.d("QuickTaskActivity", "onDoubleTap()");
		if (getScrollViewParent() != null) {
			getScrollViewParent().setDoNotIntercept(true);
		}
		if (activity.currentViewType == ViewType.TASKS) {
			((View)getParent()).setBackgroundResource(R.drawable.marked_color);
		} else {
			setBackgroundResource(R.drawable.marked_color);
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		MyTaskBase item = (MyTaskBase)getTag();
		int position = arrayAdapter.getPosition(item);
		float distanceX = 0;
		//LayoutParams lp;
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			if (doubleTapStart == null) {
				return false;
			}
			if (getScrollViewParent() != null) {
				getScrollViewParent().setDoNotIntercept(false);
			}
			distanceX = Math.abs(e.getX() - doubleTapStart.getX());
			if (distanceX > DELETE_THRESHOLD) {
				if (activity.currentViewType == ViewType.TASKS) {
					activity.getDbManager().deleteTask((MyTask)item);
				} else {
					activity.getDbManager().deleteList((MyTasksList)item);
				}
				arrayAdapter.remove(item);
				disableTouch = true;
				activity.deleteView(position);
			} else {
				if (activity.currentViewType == ViewType.TASKS) {
					((View)getParent()).setBackgroundResource(0);
				} else {
					setBackgroundResource(0);
				}
//				((View)this.getParent()).setPadding(0, 0, 0, 0);
//				lp = (LayoutParams)((View)this.getParent()).getLayoutParams();
//				lp.leftMargin = 0;
//				((View)this.getParent()).setLayoutParams(lp);
//				requestLayout();
			}
			doubleTapStart = null;
			return true;
		case MotionEvent.ACTION_CANCEL:
			doubleTapStart = null;
			if (activity.currentViewType == ViewType.TASKS) {
				((View)getParent()).setBackgroundResource(0);
			} else {
				setBackgroundResource(0);
			}
//			((View)this.getParent()).setPadding(0, 0, 0, 0);
//			lp = (LayoutParams)((View)this.getParent()).getLayoutParams();
//			lp.leftMargin = 0;
//			((View)this.getParent()).setLayoutParams(lp);
//			requestLayout();
			if (getScrollViewParent() != null) {
				getScrollViewParent().setDoNotIntercept(false);
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (doubleTapStart == null) {
				return false;
			}
//			Log.d(QuickTasksActivity.TAG, "I got the moves");
			distanceX = Math.abs(e.getX() - doubleTapStart.getX());
			if (distanceX > DELETE_THRESHOLD) {
				if (activity.currentViewType == ViewType.TASKS) {
					((View)getParent()).setBackgroundResource(R.drawable.delete_color);
				} else {
					setBackgroundResource(R.drawable.delete_color);
				}
			} else {
				if (activity.currentViewType == ViewType.TASKS) {
					((View)getParent()).setBackgroundResource(R.drawable.marked_color);
				} else {
					setBackgroundResource(R.drawable.marked_color);
				}
			}
			// TODO: move the view
//			((View)this.getParent()).setPadding((int)(e.getX() - doubleTapStart.getX()), 0, 0, 0);
//			lp = (LayoutParams)((View)this.getParent()).getLayoutParams();
//			lp.leftMargin = (int)(e.getX() - doubleTapStart.getX());
//			((View)this.getParent()).setLayoutParams(lp);
//			requestLayout();
			return true;
		}
		return false;
	}
	
	public void setArrayAdapter(MyArrayAdapter<MyTaskBase> adapter) {
		arrayAdapter = adapter;
	}
}
