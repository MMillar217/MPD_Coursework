package com.marcmillar.S1828600.mpd.cw;
/*
Marc Millar - S1828600
File: ViewWeightAnimationWrapper.java

Copyright:
Mitch Tabian
19/04/2020
ViewWeightAnimationWrapper.java
Java
https://github.com/mitchtabian/Google-Maps-2018/blob/google-directions-api-getting-started-end/app/src/main/java/com/codingwithmitch/googlemaps2018/util/ViewWeightAnimationWrapper.java
* */
import android.view.View;
import android.widget.LinearLayout;

public class ViewWeightAnimationWrapper {
	private View view;

	public ViewWeightAnimationWrapper(View view) {
		if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
			this.view = view;
		} else {
			throw new IllegalArgumentException("The view should have LinearLayout as parent");
		}
	}

	public void setWeight(float weight) {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
		params.weight = weight;
		view.getParent().requestLayout();
	}

	public float getWeight() {
		return ((LinearLayout.LayoutParams) view.getLayoutParams()).weight;
	}
}
