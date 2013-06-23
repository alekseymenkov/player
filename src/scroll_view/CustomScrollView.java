package scroll_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import java.util.ArrayList;

import knaapo.player.R;


public class CustomScrollView extends FrameLayout {

	float mTouchX;
	float mTouchY;

	ScrollView mVerticalScrollView;
	HorizontalScrollView mHorizontalScrollView;
	FrameLayout mFrameLayout;

    ArrayList<View> mWidgets;
	ArrayList<Integer> mLeft;
	ArrayList<Integer> mTop;

	LayoutInflater mInflater;


	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initValues();
	}


	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initValues();
	}


	public CustomScrollView(Context context) {
		super(context);
		initValues();
	}


	void initValues() {

		mTouchX = 0;
		mTouchY = 0;

		mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.scroll_view_area, this);	
		
		mVerticalScrollView = (ScrollView) findViewById(R.id.verticalScrollView);
		mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

		mWidgets = new ArrayList<View>();
		mLeft = new ArrayList<Integer>();
		mTop = new ArrayList<Integer>();

	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float currentX;
		float currentY;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchX = event.getX();
			mTouchY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			currentX = event.getX();
			currentY = event.getY();
			mVerticalScrollView.scrollBy((int) (mTouchX - currentX), (int) (mTouchY - currentY));
			mHorizontalScrollView.scrollBy((int) (mTouchX - currentX), (int) (mTouchY - currentY));
			mTouchX = currentX;
			mTouchY = currentY;
			break;

		case MotionEvent.ACTION_UP:
			currentX = event.getX();
			currentY = event.getY();
			mVerticalScrollView.scrollBy((int) (mTouchX - currentX), (int) (mTouchY - currentY));
			mHorizontalScrollView.scrollBy((int) (mTouchX - currentX), (int) (mTouchY - currentY));
			break;
		}
		

		return false;
	}
	

	public void addWidget(View view, int leftMargin, int topMargin) {

		mWidgets.add(view);
		mLeft.add(leftMargin);
		mTop.add(topMargin);


		final int rightMargin = 0;
		final int bottomMargin = 0;


		LayoutParams frameLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		frameLayoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

		mFrameLayout.addView(view, frameLayoutParams);

    }

	
	public void changeView(float scaleValue, int viewID) {

		final int rightMargin = 0;
		final int bottomMargin = 0;

		LayoutParams frameLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		frameLayoutParams.setMargins((int)(mLeft.get(viewID) * scaleValue), (int)(mTop.get(viewID) * scaleValue), rightMargin, bottomMargin);
		
		mWidgets.get(viewID).setLayoutParams(frameLayoutParams);

    }
	

	public void removeWidget(View view) {

		mWidgets.remove(mWidgets.indexOf(view));

    }
	
	
	@Override
	public void removeAllViews() {
		super.removeAllViews();
		mWidgets.clear();
    }
}