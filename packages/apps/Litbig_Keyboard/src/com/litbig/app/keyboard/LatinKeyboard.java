package com.litbig.app.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.view.inputmethod.EditorInfo;

@SuppressLint("InlinedApi")
@SuppressWarnings("unused")
public class LatinKeyboard extends Keyboard {

    private Key mEnterKey;
    private Key mShiftKey;
    
    public Key getShiftKey() {
    	return mShiftKey;
    }
    public LatinKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public LatinKeyboard(Context context, int layoutTemplateResId, 
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        Key key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        if (key.codes[0] == 55001) {
        	mShiftKey = key;
        }
        return key;
    }
    
    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }

		mEnterKey.iconPreview = null;
		mEnterKey.icon = null;
		switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
		case EditorInfo.IME_ACTION_GO:
			mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_go, null);
			break;
		case EditorInfo.IME_ACTION_NEXT:
			mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_next, null);
			break;
		case EditorInfo.IME_ACTION_SEARCH:
			mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_search, null);
			break;
		case EditorInfo.IME_ACTION_SEND:
			mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_send, null);
			break;
		case EditorInfo.IME_ACTION_DONE:
		default:
			mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return, null);
			break;
		}
    }
    
    static class LatinKey extends Keyboard.Key {
        
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }
        
        /**
         * Overriding this method so that we can reduce the target area for the key that
         * closes the keyboard. 
         */
        @Override
        public boolean isInside(int x, int y) {
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }

}
