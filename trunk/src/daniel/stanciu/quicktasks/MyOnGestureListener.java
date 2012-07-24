package daniel.stanciu.quicktasks;

import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class MyOnGestureListener extends SimpleOnGestureListener {
	private QuickTasksActivity activity;
	private final int SWIPE_MIN_DISTANCE;
	private final int SWIPE_MAX_OFF_PATH;
	private final int SWIPE_THRESHOLD_VELOCITY;
	
	public MyOnGestureListener(QuickTasksActivity activity) {
		super();
		this.activity = activity;
		ViewConfiguration vc = ViewConfiguration.get(activity);
		SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
		SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
		SWIPE_MAX_OFF_PATH = 4 * SWIPE_MIN_DISTANCE;
		Log.d(QuickTasksActivity.TAG, "Swipe velocity threshold: " + SWIPE_THRESHOLD_VELOCITY);
		Log.d(QuickTasksActivity.TAG, "Swipe min distance: " + SWIPE_MIN_DISTANCE);
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return super.onDoubleTap(e);
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return super.onDoubleTapEvent(e);
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
//		Log.d("GestureListener", "Single tap confirmed");
		return super.onSingleTapConfirmed(e);
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1 == null || e2 == null) {
			return false;
		}

//		Log.d("GestureListener", "velocityX = " + velocityX + ", velocityY = " + velocityY);
//		Log.d("GestureListener", "e1 pointer count " + e1.getPointerCount());
//		for (int i = 0; i < e1.getPointerCount(); ++i) {
//			Log.d("GestureListener", "e1: Pointer " + i + " id = " + e1.getPointerId(i));
//			Log.d("GestureListener", "e1: Pointer " + i + " X = " + e1.getX(i));
//			Log.d("GestureListener", "e1: Pointer " + i + " Y = " + e1.getY(i));
//		}
//		Log.d("GestureListener", "e2 pointer count " + e2.getPointerCount());
//		for (int i = 0; i < e2.getPointerCount(); ++i) {
//			Log.d("GestureListener", "e2: Pointer " + i + " id = " + e2.getPointerId(i));
//			Log.d("GestureListener", "e2: Pointer " + i + " X = " + e2.getX(i));
//			Log.d("GestureListener", "e2: Pointer " + i + " Y = " + e2.getY(i));
//		}
		
		if (activity.currentViewType == ViewType.LISTS) {
			return false;
		}

		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;
		
		if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			// right to left swipe
			activity.onRightToLeftSwipe();
			return true;
		} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			// left to right swipe
			activity.onLeftToRightSwipe();
			return true;
		}
		return false;
	}

}
