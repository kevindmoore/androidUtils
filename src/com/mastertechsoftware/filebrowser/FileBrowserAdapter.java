package com.mastertechsoftware.filebrowser;


import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.list.ViewWrapper;
import com.mastertechsoftware.thread.BasicQueue;
import com.mastertechsoftware.thread.BasicQueueProcessor;
import com.mastertechsoftware.thread.BasicQueueTask;
import com.mastertechsoftware.thread.QueueHandler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: kevin.moore
 */
public class FileBrowserAdapter extends BaseAdapter implements SectionIndexer {
	/** Remember our context so we can use it when constructing views. */
    private int numViews = 6;
	private List<File> files = new ArrayList<File>();
	/** Remember our context so we can use it when constructing views. */
	protected Activity mContext;
	private boolean showFileButtons = true;
	private boolean showDirButtons = false;
	private View.OnClickListener selectFileListener;
	private View.OnClickListener selectItemListener;
	private FileListView parentDir;
	private FileListView currentDir;
	private BasicQueueProcessor<ImageProcessingInfo> imageLoadingProcessor;
    private String alphabet = " 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-.!@#$%^&*()_+=/?><,~`";
    /**
     * The string of characters that make up the indexing sections.
     */
    protected CharSequence mAlphabet;

    /**
     * Cached length of the alphabet array.
     */
    private int mAlphabetLength;

    /**
     * This contains a cache of the computed indices so far. It will get reset whenever
     * the dataset changes or the cursor changes.
     */
    private SparseIntArray mAlphaMap;

    /**
     * Use a collator to compare strings in a localized manner.
     */
    private java.text.Collator mCollator;

    /**
     * The section array converted from the alphabet string.
     */
    private String[] mAlphabetArray;

	/**
	 * Constructor.
	 * @param mContext
	 */
	public FileBrowserAdapter(Activity mContext) {
		this.mContext = mContext;
		QueueHandler<BasicQueue<ImageProcessingInfo>> systemQueueHandler = new QueueHandler<BasicQueue<ImageProcessingInfo>>();
		imageLoadingProcessor = new BasicQueueProcessor<ImageProcessingInfo>();
		systemQueueHandler.start(imageLoadingProcessor);
        mAlphabet = alphabet;
        mAlphabetLength = alphabet.length();
        mAlphabetArray = new String[mAlphabetLength];
        for (int i = 0; i < mAlphabetLength; i++) {
            mAlphabetArray[i] = Character.toString(mAlphabet.charAt(i));
        }
        mAlphaMap = new SparseIntArray(mAlphabetLength);
        // Get a Collator for the current locale for string comparisons.
        mCollator = java.text.Collator.getInstance();
        mCollator.setStrength(java.text.Collator.PRIMARY);
	}


	/**
	 * set whether we show the file select buttons.
	 * @param showFileButtons
	 */
	public void setShowFileButtons(boolean showFileButtons) {
		this.showFileButtons = showFileButtons;
	}

    /**
     * Show the select directory buttons
     * @param showDirButtons
     */
    public void setShowDirButtons(boolean showDirButtons) {
        this.showDirButtons = showDirButtons;
    }

    /**
	 * Set the list of files for the current directory.
     * @param currentDirectory
     * @param files
     */
	public void setFiles(File currentDirectory, List<File> files) {
		this.files = files;
		File parentFile = currentDirectory.getParentFile();
		File parentsParent = parentFile.getParentFile();
		if (parentDir == null) {
			parentDir = new FileListView(mContext, parentFile, selectItemListener, selectFileListener);
			TextView textView = parentDir.getTextView();
			textView.setTextColor(Color.GREEN);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10);
		}
		if (currentDir == null) {
			currentDir = new FileListView(mContext, parentFile, selectItemListener, selectFileListener);
			TextView textView = currentDir.getTextView();
			textView.setTextColor(Color.GREEN);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10);
		}
		this.files.add(0, parentFile);
		parentDir.setFile(parentsParent);
		if (hasParent()) {
			parentDir.setText(parentsParent.getPath() + " (" + mContext.getString(R.string.parent) + ")");
			this.files.add(0, parentsParent);
		}

		currentDir.setFile(parentFile);
		currentDir.setText(parentFile.getName() + " (" + mContext.getString(R.string.currentDirectory) + ")");
		notifyDataSetChanged();
	}

	/**
	 * Does this directory have a parent
	 * @return true if there is a parent
	 */
	public boolean hasParent() {
		return (parentDir.getFile() != null);
	}

	/**
	 * Return the # of list items
	 * @return count
	 */
	@Override
	public int getCount() {
		return files.size();
	}

	/**
	 * Return the view at the given position.
	 * @param position
	 * @return FileListView
	 */
	@Override
	public Object getItem(int position) {
		if (position >= files.size()) {
			return null;
		}
		return files.get(position);
	}

	/**
	 * Get the file at the given position.
	 * @param position
	 * @return File
	 */
	public File getFile(int position) {
		return (File) getItem(position);
	}

	@Override
	public int getItemViewType(int position) {
		if (hasParent()) {
			if (position < 2) {
				return 0;
			}
		} else {
			if (position < 1) {
				return 0;
			}
		}
		return 1;
//		return position % numViews;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
//		return numViews;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Get the View at the given position.
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return View
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < 2) {
			switch (position) {
				case 0:
					if (hasParent()) {
						return parentDir;
					} else {
						return currentDir;
					}
				case 1:
					if (hasParent()) {
						return currentDir;
					}
			}
		}
		FileListView fileListView;
		File currentFile = (File) getItem(position);
		if (convertView != null) {
			fileListView = (FileListView) convertView;
			fileListView.setFile(currentFile);
		} else {
			fileListView = new FileListView(mContext, currentFile, selectItemListener, selectFileListener);
		}
		ImageProcessingInfo imageProcessingInfo = new ImageProcessingInfo();
		imageProcessingInfo.height = FileListView.BITMAP_HEIGHT;
		imageProcessingInfo.width = FileListView.BITMAP_WIDTH;
		imageProcessingInfo.file = currentFile;
		imageProcessingInfo.fileListView = fileListView;
		ImageQueue basicQueueTask = new ImageQueue(imageProcessingInfo, new ImageProcessingTask());
		imageLoadingProcessor.addQueue(basicQueueTask);
		fileListView.showSelectFileButton(showFileButtons);
		fileListView.showSelectDirButton(showDirButtons);
		ViewWrapper viewWrapper = fileListView.getViewWrapper();
		viewWrapper.setPosition(position);
		viewWrapper.setOwner(fileListView);
		return fileListView;
	}

	/**
	 * Set the onclicklistener for files
	 * @param selectFileListener
	 */
	public void setSelectFileListener(View.OnClickListener selectFileListener) {
		this.selectFileListener = selectFileListener;
	}

	public void setSelectItemListener(View.OnClickListener selectItemListener) {
		this.selectItemListener = selectItemListener;
	}

	@Override
    /**
     * Returns the section array constructed from the alphabet provided in the constructor.
     * @return the section array
     */
    public Object[] getSections() {
        return mAlphabetArray;
    }


    @Override
    public int getPositionForSection(int sectionIndex) {
        final SparseIntArray alphaMap = mAlphaMap;

        // Check bounds
        if (sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= mAlphabetLength) {
            sectionIndex = mAlphabetLength - 1;
        }

        int count = files.size();
        int start = 0;
        int end = count;
        int pos;

        char letter = mAlphabet.charAt(sectionIndex);
        String targetLetter = Character.toString(letter);
        int key = letter;
        // Check map
        if (Integer.MIN_VALUE != (pos = alphaMap.get(key, Integer.MIN_VALUE))) {
            // Is it approximate? Using negative value to indicate that it's
            // an approximation and positive value when it is the accurate
            // position.
            if (pos < 0) {
                pos = -pos;
                end = pos;
            } else {
                // Not approximate, this is the confirmed start of section, return it
                return pos;
            }
        }

        // Do we have the position of the previous section?
        if (sectionIndex > 0) {
            int prevLetter =
                    mAlphabet.charAt(sectionIndex - 1);
            int prevLetterPos = alphaMap.get(prevLetter, Integer.MIN_VALUE);
            if (prevLetterPos != Integer.MIN_VALUE) {
                start = Math.abs(prevLetterPos);
            }
        }

        // Now that we have a possibly optimized start and end, let's binary search

        pos = (end + start) / 2;

        while (pos < end) {
            // Get letter at pos
            String curName = files.get(pos).getName();
            if (curName == null) {
                if (pos == 0) {
                    break;
                } else {
                    pos--;
                    continue;
                }
            }
            int diff = compare(curName, targetLetter);
            if (diff != 0) {
                // TODO: Commenting out approximation code because it doesn't work for certain
                // lists with custom comparators
                // Enter approximation in hash if a better solution doesn't exist
                // String startingLetter = Character.toString(getFirstLetter(curName));
                // int startingLetterKey = startingLetter.charAt(0);
                // int curPos = alphaMap.get(startingLetterKey, Integer.MIN_VALUE);
                // if (curPos == Integer.MIN_VALUE || Math.abs(curPos) > pos) {
                //     Negative pos indicates that it is an approximation
                //     alphaMap.put(startingLetterKey, -pos);
                // }
                // if (mCollator.compare(startingLetter, targetLetter) < 0) {
                if (diff < 0) {
                    start = pos + 1;
                    if (start >= count) {
                        pos = count;
                        break;
                    }
                } else {
                    end = pos;
                }
            } else {
                // They're the same, but that doesn't mean it's the start
                if (start == pos) {
                    // This is it
                    break;
                } else {
                    // Need to go further lower to find the starting row
                    end = pos;
                }
            }
            pos = (start + end) / 2;
        }
        alphaMap.put(key, pos);
        return pos;
    }

    /**
     * Default implementation compares the first character of word with letter.
     */
    protected int compare(String word, String letter) {
        final String firstLetter;
        if (word.length() == 0) {
            firstLetter = " ";
        } else {
            firstLetter = word.substring(0, 1);
        }

        return mCollator.compare(firstLetter, letter);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position > files.size()) {
            return 0;
        }
        String curName = files.get(position).getName();
        // Linear search, as there are only a few items in the section index
        // Could speed this up later if it actually gets used.
        for (int i = 0; i < mAlphabetLength; i++) {
            char letter = mAlphabet.charAt(i);
            String targetLetter = Character.toString(letter);
            if (compare(curName, targetLetter) == 0) {
                return i;
            }
        }
        return 0; // Don't recognize the letter - falls under zero'th section
    }


    /**
	 * Class to hold the processing task and image info
	 */
	class ImageQueue extends BasicQueue<ImageProcessingInfo> {

		public ImageQueue(ImageProcessingInfo data, ImageProcessingTask imageProcessingTask) {
			super(data, imageProcessingTask);
		}
	}

	/**
	 * Class to process the image.
	 */
	class ImageProcessingTask implements BasicQueueTask<ImageProcessingInfo> {
		private ImageProcessingInfo imageProcessingInfo;

		@Override
		public void process(ImageProcessingInfo imageProcessingInfo) {
			this.imageProcessingInfo = imageProcessingInfo;
			File file = imageProcessingInfo.file;
			Context context = getActivity();
			if (FileBrowserFileUtils.isImageFile(file.getAbsolutePath())) {
				Bitmap bitmap = FileBrowserFileUtils.getImageThumbnail(context, file.getAbsolutePath(), imageProcessingInfo.width, imageProcessingInfo.height);
				if (bitmap != null) {
					imageProcessingInfo.bitmap = bitmap;
				}
			} else if (FileBrowserFileUtils.isVideoFile(file.getAbsolutePath())) {
				Bitmap bitmap = FileBrowserFileUtils.getVideoThumbnail(context, file.getAbsolutePath(), imageProcessingInfo.width, imageProcessingInfo.height);
				if (bitmap != null) {
					imageProcessingInfo.bitmap = bitmap;
				}
			}
		}

		@Override
		public void finished() {
			if (imageProcessingInfo.bitmap != null) {
				imageProcessingInfo.fileListView.setIconImage(imageProcessingInfo.bitmap);
			} else if (imageProcessingInfo.file.isDirectory()) {
				imageProcessingInfo.fileListView.setImageResource(R.drawable.folder);
			} else {
				imageProcessingInfo.fileListView.setImageResource(R.drawable.file);
			}

		}

		@Override
		public Activity getActivity() {
			return FileBrowserAdapter.this.mContext;
		}
	}
}
