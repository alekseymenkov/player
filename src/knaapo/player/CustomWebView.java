package knaapo.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomWebView extends WebView {

    private OnTouchListener mListener;

    public void setOnTouch(OnTouchListener listener) {
        mListener = listener;
        return;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);
        return false;
    }

    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1)
            return true;

        if (mListener != null)
            mListener.onTouch(this, event);
        return super.onTouchEvent(event);

    }
}
