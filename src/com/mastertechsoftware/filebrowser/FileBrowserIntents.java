package com.mastertechsoftware.filebrowser;

/**
 * User: kevin.moore
 */
public final class FileBrowserIntents {

	/**
	 * Activity Action: Pick a file through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current file name or file name suggestion.
	 * Returns a new file name as file URI in data.
	 *
	 * <p>Constant Value: "com.mastertechsoftware.FileBrowseraction.PICK_FILE"</p>
	 */
	public static final String ACTION_PICK_FILE = "com.mastertechsoftware.FileBrowseraction.PICK_FILE";

	/**
	 * Activity Action: Pick a directory through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current directory name or directory name suggestion.
	 * Returns a new directory name as file URI in data.
	 *
	 * <p>Constant Value: "com.mastertechsoftware.FileBrowseraction.PICK_DIRECTORY"</p>
	 */
	public static final String ACTION_PICK_DIRECTORY = "com.mastertechsoftware.FileBrowseraction.PICK_DIRECTORY";


	/**
	 * Extra for specifying an initial directory
	 */
	public static final String EXTRA_INITIAL_DIRECTORY = "com.mastertechsoftware.FileBrowserextra.EXTRA_INITIAL_DIRECTORY";

    /**
     * Filter. *.ext
     */
    public static final String EXTRA_FILTER = "com.mastertechsoftware.FileBrowserextra.EXTRA_FILTER";

}
