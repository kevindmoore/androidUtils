package com.mastertechsoftware.list;

/* $Id: BulletedTextView.java 57 2007-11-21 18:31:52Z steven $
*
* Copyright 2007 Steven Osborn
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IconifiedTextView extends LinearLayout {

	private TextView mText;
	private ImageView mIcon;
	private CheckBox checkbox;
	private IconifiedText iconifiedText;

	public IconifiedTextView(Context context, IconifiedText aIconifiedText) {
		super(context);
		this.iconifiedText = aIconifiedText;

		/* First Icon and the Text to the right (horizontal),
				  * not above and below (vertical) */
		this.setOrientation(HORIZONTAL);

		checkbox = new CheckBox(context);
		checkbox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkbox.isChecked()) {
					iconifiedText.setSelected(true);
				} else {
					iconifiedText.setSelected(false);
				}
			}
		});
		checkbox.setPadding(0, 0, 0, 0);

		mIcon = new ImageView(context);
		mIcon.setImageDrawable(aIconifiedText.getIcon());
		// left, top, right, bottom
		mIcon.setPadding(5, 2, 10, 0); // 5px to the right

		/* At first, add the Icon to ourself
				  * (! we are extending LinearLayout) */
		LayoutParams layoutParams = new LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;

		addView(checkbox, layoutParams);

		addView(mIcon, layoutParams);

		mText = new TextView(context);
		mText.setText(aIconifiedText.getText());
		/* Now the text (after the icon) */
		layoutParams = new LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		addView(mText, layoutParams);
		setMinimumHeight(40);
	}

	public void setText(String words) {
		mText.setText(words);
	}

	public String getText() {
		return mText.getText().toString();
	}

	public void setIcon(Drawable bullet) {
		mIcon.setImageDrawable(bullet);
	}

	public void setSelected(boolean selected) {
		checkbox.setSelected(selected);
	}

	public boolean isSelected() {
		return checkbox.isSelected();
	}
}
