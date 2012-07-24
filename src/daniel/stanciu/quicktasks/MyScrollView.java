package daniel.stanciu.quicktasks;

import android.content.Context;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	private QuickTasksActivity activity;
	private boolean doNotIntercept;

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MyScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setActivity(QuickTasksActivity act) {
		activity = act;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (doNotIntercept) {
			return false;
		}
//		Log.d(QuickTasksActivity.TAG, "Scroll intercepting");
		activity.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (doNotIntercept) {
			return false;
		}
//		Log.d(QuickTasksActivity.TAG, "Scroll touch");
		activity.onTouchEvent(ev);
		return super.onTouchEvent(ev);
		//return true;
	}

	public void setDoNotIntercept(boolean doNotIntercept) {
//		Log.d(QuickTasksActivity.TAG, "setDoNotIntercept(" + doNotIntercept + ")");
		this.doNotIntercept = doNotIntercept;
	}

}
