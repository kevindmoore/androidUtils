package com.mastertechsoftware.filebrowser;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.list.ViewWrapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

/**
 * User: kevin.moore
 */
public class FileListView extends LinearLayout {
    private static final String TAG = "FileBrowserActivity";
	private static int ICON_ID = 6000;
	private static int TEXT_ID = 6001;
	private static int BUTTON_ID = 6002;
	public static final int BITMAP_WIDTH = 48;
	public static final int BITMAP_HEIGHT = 48;
	private File file;
	private Context context;
	private ImageView mIcon;
	private TextView mText;
	private Button selectFile;
	private View.OnClickListener selectFileListener;
	private View.OnClickListener selectItemListener;
	private ViewWrapper viewWrapper;

	public FileListView(Context context, File file, View.OnClickListener selectItemListener, View.OnClickListener selectFileListener) {
		super(context);
		this.context = context;
		this.selectItemListener = selectItemListener;
		this.selectFileListener = selectFileListener;
		viewWrapper = new ViewWrapper();
		this.setOrientation(HORIZONTAL);
		mIcon = new ImageView(context);
		mIcon.setId(ICON_ID);
		mIcon.setTag(viewWrapper);
		mIcon.setScaleType(ImageView.ScaleType.FIT_XY);
		if (file.isDirectory()) {
			mIcon.setImageResource(R.drawable.folder);
		} else {
			mIcon.setImageResource(R.drawable.file);
		}
		if (selectItemListener != null) {
			mIcon.setOnClickListener(selectItemListener);
		}
		// left, top, right, bottom
//		mIcon.setPadding(5, 2, 10, 0); // 5px to the right
		LayoutParams layoutParams = new LayoutParams(
			BITMAP_WIDTH, BITMAP_HEIGHT);
		layoutParams.setMargins(5, 2, 10, 0); // 5px to the right
		addView(mIcon, layoutParams);
		mText = new TextView(context);
		mText.setId(TEXT_ID);
		mText.setText(file.getName());
		mText.setTag(viewWrapper);
		if (selectItemListener != null) {
			mText.setOnClickListener(selectItemListener);
		}
		/* Now the text (after the icon) */
		layoutParams = new LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		addView(mText, layoutParams);
		setMinimumHeight(40);
		selectFile = new Button(context);
		selectFile.setId(BUTTON_ID);
		selectFile.setText(R.string.select);
		selectFile.setTag(viewWrapper);
		if (selectFileListener != null) {
			selectFile.setOnClickListener(selectFileListener);
		}
		layoutParams = new LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(10, 5, 5, 2);
		addView(selectFile, layoutParams);
		if (file.isDirectory()) {
			selectFile.setVisibility(GONE);
		}
		setFile(file);
	}

	public ViewWrapper getViewWrapper() {
		return viewWrapper;
	}

    @Override
   	public boolean onKeyDown(int keyCode, KeyEvent event) {
   		Log.d(TAG, "FileListView:onKeyDown: key:" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * Set the file associated with this view
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
		if (file == null) {
			return;
		}
		if (file.isDirectory()) {
			mIcon.setImageResource(R.drawable.folder);
		} else {
			if (FileBrowserFileUtils.isImageFile(file.getAbsolutePath())) {
				Bitmap bitmap = FileBrowserFileUtils.getImageThumbnail(context, file.getAbsolutePath(), BITMAP_WIDTH, BITMAP_HEIGHT);
				if (bitmap != null) {
					mIcon.setImageBitmap(bitmap);
				} else {
					mIcon.setImageResource(R.drawable.file);
				}
			} else if (FileBrowserFileUtils.isVideoFile(file.getAbsolutePath())) {
				Bitmap bitmap = FileBrowserFileUtils.getVideoThumbnail(context, file.getAbsolutePath(), BITMAP_WIDTH, BITMAP_HEIGHT);
				if (bitmap != null) {
					mIcon.setImageBitmap(bitmap);
				} else {
					mIcon.setImageResource(R.drawable.file);
				}
			} else {
				mIcon.setImageResource(R.drawable.file);
			}
		}
		mText.setText(file.getName());
		showSelectFileButton(true);
	}

	public void setIconImage(Bitmap bitmap) {
		mIcon.setImageBitmap(bitmap);
	}

	public void setImageResource(int resource) {
		mIcon.setImageResource(resource);
	}

	public void setText(String text) {
		mText.setText(text);
	}

	public TextView getTextView() {
		return mText;
	}

	public File getFile() {
		return file;
	}

	public void showSelectFileButton(boolean show) {
		if (file == null || file.isDirectory() || !show) {
			selectFile.setVisibility(GONE);
		} else if (file.isFile()) {
			selectFile.setVisibility(VISIBLE);
		}
	}

    public void showSelectDirButton(boolean show) {
        if (file == null || !show) {
      			selectFile.setVisibility(GONE);
      		} else if (file.isDirectory()) {
      			selectFile.setVisibility(VISIBLE);
      		}

    }
}
