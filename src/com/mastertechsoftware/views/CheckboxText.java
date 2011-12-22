package com.mastertechsoftware.views;

import com.mastertechsoftware.layout.GridLayout;
import com.mastertechsoftware.layout.LayoutIDGenerator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * User: kevin.moore
 */
public class CheckboxText extends RelativeLayout implements Checkable {
	protected boolean mChecked;
	protected boolean checkBoxOnLeft = true;
	protected TextView textView;
	protected CheckBox checkBox;

	/**
	 * Constructors
	 * @param context
	 * @param checkBoxOnLeft
	 */
	public CheckboxText(Context context, boolean checkBoxOnLeft) {
		super(context);
		this.checkBoxOnLeft = checkBoxOnLeft;
		init();
	}

	public CheckboxText(Context context) {
		super(context);
		init();
	}

	public CheckboxText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Initialize the layout, create the objects
	 */
	protected void init() {
		setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT));
//		setBackgroundColor(Color.RED);
		setPadding(5, 5, 5, 5);
		checkBox = new CheckBox(getContext());
		checkBox.setId(LayoutIDGenerator.getNewID("CheckboxText.checkBox"));
		textView = new TextView(getContext());
		textView.setId(LayoutIDGenerator.getNewID("CheckboxText.textView"));
//		checkBox.setBackgroundColor(Color.DKGRAY);
//		textView.setBackgroundColor(Color.CYAN);
		textView.setSingleLine();
		if (checkBoxOnLeft) {
			addCheckBox();
			addTextView();
		} else {
			addTextView();
			addCheckBox();
		}
	}

	protected void addTextView() {
		LayoutParams params = null;
		params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		if (checkBoxOnLeft) {
			params.addRule(RelativeLayout.RIGHT_OF, LayoutIDGenerator.getID("CheckboxText.checkBox"));
			params.addRule(RelativeLayout.ALIGN_BOTTOM, LayoutIDGenerator.getID("CheckboxText.checkBox"));
			textView.setGravity(Gravity.CENTER_VERTICAL);
			addView(textView, params);
		} else {
			params.addRule(RelativeLayout.LEFT_OF, LayoutIDGenerator.getID("CheckboxText.checkBox"));
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_BOTTOM, LayoutIDGenerator.getID("CheckboxText.checkBox"));
			textView.setGravity(Gravity.CENTER_VERTICAL);
//			params.addRule(RelativeLayout.CENTER_VERTICAL);
			addView(textView, params);
		}
	}

	protected void addCheckBox() {
		LayoutParams params = null;
		params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		if (!checkBoxOnLeft) {
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else {

		}
		if (checkBoxOnLeft) {
			textView.setPadding(10, 0, 2, 0);
//			params.rightMargin = 10;
		} else {
//			params.leftMargin = 10;
//			params.rightMargin = 2;
			checkBox.setPadding(10, 0, 2, 0);
		}
		addView(checkBox, params);
	}

	public CheckBox getCheckBox() {
		return checkBox;
	}

	public TextView getTextView() {
		return textView;
	}

	public void setCheckboxId(int id) {
		checkBox.setId(id);
	}
	
	public void setTextId(int id) {
		textView.setId(id);
	}
	
	public void setTextViewText(String text) {
		textView.setText(text);
	}
	
	public void toggle() {
		setChecked(!mChecked);
	}

	@ViewDebug.ExportedProperty
	public boolean isChecked() {
		return mChecked;
	}

	public void setOnCheckedListener(CompoundButton.OnCheckedChangeListener checkedChangeListener) {
		checkBox.setOnCheckedChangeListener(checkedChangeListener);
	}

	public void setTextOnClickListener(OnClickListener clickListener) {
		textView.setOnClickListener(clickListener);
	}

	/**
	 * <p>Changes the checked state of this text view.</p>
	 *
	 * @param checked true to check the text, false to uncheck it
	 */
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			checkBox.setChecked(checked);
		}
	}

}
