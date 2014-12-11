package com.mastertechsoftware.filebrowser;


import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mastertechsoftware.AndroidUtil.R;
import com.mastertechsoftware.list.ViewWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBrowserActivity extends ListActivity implements View.OnClickListener {
	private static final String TAG = "FileBrowserActivity";
	private String LAST_LOADED_DIRECTORY = "com.mastertechsoftware.filebrowser.lastdirectory";
	private static final int QUIT_MENU_ITEM = Menu.FIRST;
	private static final int MENU_OPEN = Menu.FIRST + 1;
	private static final int MENU_SELECT = Menu.FIRST + 2;
	private static final int MENU_DELETE = Menu.FIRST + 3;
	private ListView listView;

	enum STATE {
		STATE_BROWSE,
		STATE_PICK_FILE,
		STATE_PICK_DIRECTORY

	}

	private FileBrowserAdapter adapter;
	private File currentFile;
	private File currentDirectory;
	private SelectFileListener selectFileListener = new SelectFileListener();
	private STATE state = STATE.STATE_BROWSE;
	private String initialDirectory;
	private String filter;
    private Pattern filterPattern;
    private Matcher matcher;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_browser);

		adapter = new FileBrowserAdapter(this);
		adapter.setSelectFileListener(selectFileListener);
		listView = (ListView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
        listView.setFastScrollEnabled(true);
		listView.setOnCreateContextMenuListener(this);
		adapter.setSelectItemListener(this);
		readIntent();
	}

	@Override
	protected void onDestroy() {
        if (currentDirectory != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(LAST_LOADED_DIRECTORY, currentDirectory.getAbsolutePath());
            edit.commit();
        }
		super.onDestroy();
	}

	/**
	 * Read the given intent and set the initial directory
	 */
	private void readIntent() {
		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();
			if (action != null && FileBrowserIntents.ACTION_PICK_FILE.equalsIgnoreCase(action)) {
				state = STATE.STATE_PICK_FILE;
			} else if (action != null && FileBrowserIntents.ACTION_PICK_DIRECTORY.equalsIgnoreCase(action)) {
				state = STATE.STATE_PICK_DIRECTORY;
			}
			Uri data = intent.getData();
			if (data != null) {
				initialDirectory = data.getPath();
			} else {
				initialDirectory = intent.getStringExtra(FileBrowserIntents.EXTRA_INITIAL_DIRECTORY);
			}
            filter = intent.getStringExtra(FileBrowserIntents.EXTRA_FILTER);
            if (filter != null) {
                parseFilter();
            }
		}
		if (state == STATE.STATE_BROWSE || state == STATE.STATE_PICK_DIRECTORY) {
			adapter.setShowFileButtons(false);
		}
		if (state != STATE.STATE_BROWSE && state == STATE.STATE_PICK_DIRECTORY) {
			adapter.setShowDirButtons(true);
		}
		if (initialDirectory != null) {
			currentDirectory = new File(initialDirectory);
			if (!currentDirectory.exists()) {
				currentDirectory = null;
			} else if (currentDirectory.isFile()) {
				currentDirectory = currentDirectory.getParentFile();
			}
		} else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String lastDirectory = prefs.getString(LAST_LOADED_DIRECTORY, null);
			if (lastDirectory != null) {
				currentDirectory = new File(lastDirectory);
				if (!currentDirectory.exists()) {
					currentDirectory = null;
				} else if (currentDirectory.isFile()) {
					currentDirectory = currentDirectory.getParentFile();
				}
			}
		}
        if (currentDirectory == null) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                if (externalStorageDirectory != null) {
                    currentDirectory = externalStorageDirectory;
                } else {
                    currentDirectory = new File("/");
                }
            } else {
                currentDirectory = new File("/");
            }
        }
		if (currentDirectory.exists()) {
			listDirectory(currentDirectory);
		}
	}

    /**
     * Parse the filter into a regular expression
     */
    private void parseFilter() {
        StringTokenizer tokenizer = new StringTokenizer(filter, ";");
        StringBuilder filterExpression = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.contains("*")) {
                token = token.replace(".", "\\.");
                int index = token.indexOf("*");
                if (index > 0) {
                    filterExpression.append(token.substring(0, index));
                }
                filterExpression.append(".*");
                filterExpression.append(token.substring(index+1));
            } else {
                filterExpression.append(token);                
            }
            if (tokenizer.hasMoreTokens()) {
                filterExpression.append("|");                                
            }
        }
        filterPattern = Pattern.compile(filterExpression.toString(), Pattern.DOTALL);
        
    }

    /**
	 * Set the list of files for the given directory.
	 *
	 * @param directory
	 */
	private void listDirectory(File directory) {
		currentDirectory = directory;
		setTitle(currentDirectory.getAbsolutePath());
		adapter.setFiles(currentDirectory, getFileList(currentDirectory));
		listView.setSelection(0);
	}

	/**
	 * Back key pressed. If we're at the top, quit the app
	 */
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed");
//		if (!upFolder()) {
			setResult(RESULT_CANCELED);
			finish();
//		}
	}

	/**
	 * Go up a folder.
	 *
	 * @return true if able to
	 */
	private boolean upFolder() {
		if (currentDirectory.getParentFile() != null) {
			Log.d(TAG, "upFolder");
			currentDirectory = currentDirectory.getParentFile();
			setTitle(currentDirectory.getAbsolutePath());
			adapter.setFiles(currentDirectory, getFileList(currentDirectory));
			listView.setSelection(0);
			return true;
		}
		return false;
	}

	/**
	 * Handle touching list item
	 *
	 * @param l
	 * @param v
	 * @param position
	 * @param id
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick: Selecting list item at:" + position);
		handlePosition(position, false);
	}

	@Override
	public void onClick(View view) {
		ViewWrapper viewWrapper = (ViewWrapper) view.getTag();
		Log.d(TAG, "onClick: Selecting list item at:" + viewWrapper.getPosition());
		handlePosition(viewWrapper.getPosition(), false);
	}

	/**
	 * Handle the selection at the current position
	 *
	 *
	 * @param position
	 * @param select
	 * @return true if handled. This currently always returns true
	 */
	private boolean handlePosition(int position, boolean select) {
		Log.d(TAG, "handlePosition: position " + position);
		switch (position) {
			// Parent directory
			case 0:
				if (adapter.hasParent()) {
					upFolder();
				}
				return true;
			// Current Directory
			case 1:
				if (adapter.hasParent()) {
					return true;
				}
			default:
				currentFile = adapter.getFile(position);
				if (currentFile != null) {
					if (currentFile.isFile()) {
						if (select) {
							Log.d(TAG, "handlePosition: Selecting current file:" + position);
							selectCurrentFile();
						}
					} else {
						if (select) {
							Log.d(TAG, "handlePosition: Selecting current directory:" + position);
							currentDirectory = currentFile;
							selectCurrentDirectory();
						} else {
							Log.d(TAG, "handlePosition: Showing directory:" + position);
							showDirectory();
						}
					}
				}
				return true;
		}
	}

	/**
	 * Show the directory pointed to by the current file
	 */
	private void showDirectory() {
		if (currentFile.isDirectory()) {
			currentDirectory = currentFile;
			setTitle(currentDirectory.getAbsolutePath());
			adapter.setFiles(currentDirectory, getFileList(currentDirectory));
			listView.setSelection(0);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER && listView.hasFocus()) {
			int position = Math.max(0, listView.getSelectedItemPosition());
			Log.d(TAG, "onKeyDown: Enter position:" + position);
			if (handlePosition(position, true)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * Handle key events
	 *
	 * @param keyCode
	 * @param event
	 * @return handled or not
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown: key:" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && listView.hasFocus()) {
			int position = Math.max(0, listView.getSelectedItemPosition());
			Log.d(TAG, "onKeyDown: Right position:" + position);
			if (handlePosition(position, false)) {
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && listView.hasFocus()) {
			upFolder();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER && listView.hasFocus()) {
			int position = Math.max(0, listView.getSelectedItemPosition());
			Log.d(TAG, "onKeyDown: Enter position:" + position);
			if (handlePosition(position, true)) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		switch (info.position) {
			// Parent directory
			case 0:
				return;
			// Current Directory
			case 1:
				if (adapter.hasParent()) {
					return;
				}
			default:
				currentFile = adapter.getFile(info.position);
				if (currentFile != null) {
					if (currentFile.isFile()) {
						if (state == STATE.STATE_PICK_FILE) {
							// Show "open" menu
							menu.add(0, MENU_SELECT, 0, R.string.menu_select);
						} else {
							menu.add(0, MENU_OPEN, 0, R.string.menu_open);
							menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
						}
					} else {
						if (state == STATE.STATE_PICK_DIRECTORY) {
							// Show "open" menu
							menu.add(0, MENU_SELECT, 0, R.string.menu_select);
						} else {
							menu.add(0, MENU_OPEN, 0, R.string.menu_open);
							menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
						}
					}
				}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
			.getMenuInfo();
		currentFile = adapter.getFile(menuInfo.position);
		if (currentFile != null) {
			if (currentFile.isFile()) {
				switch (item.getItemId()) {
					case MENU_SELECT:
					case MENU_OPEN:
						selectCurrentFile();
						break;
					case MENU_DELETE:
						deleteCurrentFile();
						break;
				}
			} else {
				switch (item.getItemId()) {
					case MENU_SELECT:
					case MENU_OPEN:
						currentDirectory = currentFile;
						selectCurrentDirectory();
						break;
					case MENU_DELETE:
						deleteCurrentDirectory();
						break;
				}
			}
		}
		return false;
	}

	private void deleteCurrentDirectory() {
		if (currentFile != null && currentFile.exists()) {
			Log.d(TAG, "Deleting " + currentFile.getAbsolutePath());
			currentDirectory = currentFile.getParentFile();
			if (!currentFile.delete()) {
				Log.e(TAG, "Could not delete " + currentFile.getAbsolutePath());
			}
			currentFile = currentDirectory;
			showDirectory();
		}
	}

	private void deleteCurrentFile() {
		if (currentFile != null && currentFile.exists()) {
			Log.d(TAG, "Deleting " + currentFile.getAbsolutePath());
			currentDirectory = currentFile.getParentFile();
			if (!currentFile.delete()) {
				Log.e(TAG, "Could not delete " + currentFile.getAbsolutePath());
			}
			currentFile = currentDirectory;
			showDirectory();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item1 = menu.add(0, QUIT_MENU_ITEM, 0, R.string.quit);
		item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
                setResult(RESULT_CANCELED);
				finish();
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Select the current file
	 */
	private void selectCurrentFile() {
		Log.d(TAG, "selectCurrentFile");
		Intent resultIntent = new Intent();
		resultIntent.setData(Uri.fromFile(currentFile));
		setResult(RESULT_OK, resultIntent);
		finish();
	}

	private void selectCurrentDirectory() {
		if (state == STATE.STATE_PICK_DIRECTORY) {
			Intent resultIntent = new Intent();
			resultIntent.setData(Uri.fromFile(currentDirectory));
			setResult(RESULT_OK, resultIntent);
			finish();
		} else {
			listDirectory(currentDirectory);
		}
	}

	/**
	 * Create an array list of child files
	 *
	 * @param file
	 * @return List<File>
	 */
	private List<File> getFileList(File file) {
		File[] files = file.listFiles();
		if (files == null) {
			return new ArrayList<File>();
		}
		List<File> fileList = new ArrayList<File>(files.length);
		for (File childFile : files) {
			String fileName = childFile.getName();
			if (fileName.startsWith(".")) {
				continue;
			}
			if (fileName.equalsIgnoreCase("LOST.DIR")) {
				continue;
			}
            if (state == STATE.STATE_PICK_DIRECTORY) {
                if (!childFile.isDirectory()) {
                    continue;
                }
            }
            if (filterPattern != null && childFile.isFile()) {
                if (matcher == null) {
                    matcher = filterPattern.matcher(fileName);
                } else {
                    matcher.reset(fileName);
                }
                if (!matcher.find()) {
                    continue;
                }
            }
			fileList.add(childFile);
		}
		Collections.sort(fileList);
		return fileList;
	}


	public class SelectFileListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "SelectFileListener: onClick");
			Object tag = v.getTag();
			if (tag instanceof ViewWrapper) {
				FileListView fileListView = (FileListView) ((ViewWrapper) tag).getOwner();
				currentFile = fileListView.getFile();
				selectCurrentFile();
			}
		}
	}
}
