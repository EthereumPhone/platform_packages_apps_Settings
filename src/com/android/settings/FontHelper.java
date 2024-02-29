package com.android.settings;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.Method;
import android.view.ViewGroup;

public class FontHelper {
    // Load a custom font from the assets folder and return a Typeface object.
    public static Typeface loadFontFromAssets(Context context, String fontFileName) {
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), "font/" + fontFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return typeface;
    }

    // Apply a custom font to a TextView or any other View that supports typefaces.
    public static void applyFontToView(View view, Typeface typeface) {
        if (view != null) {
            try {
                Method setTypefaceMethod = view.getClass().getMethod("setTypeface", Typeface.class);
                if (setTypefaceMethod != null) {
                    setTypefaceMethod.invoke(view, typeface);
                }
            } catch (Exception e) {
                // Handle exceptions if the view does not support setTypeface
            }

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childView = viewGroup.getChildAt(i);
                    applyFontToView(childView, typeface);
                }
            }
        }
    }
}

