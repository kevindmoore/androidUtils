/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package com.mastertechsoftware.filebrowser;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;

import java.io.File;

/**
 * @version 2009-07-03
 * 
 * @author Peli
 *
 */
public class FileBrowserFileUtils {
	/** TAG for log messages. */
	static final String TAG = "FileBrowserFileUtils";
	public static final String JPG = ".jpg";
	public static final String GIF = ".gif";
	public static final String PNG = ".png";
	public static final String BMP = ".bmp";
	public static final String THREE_GP = ".3gp";
	public static final String MP4 = ".mp4";


	/**
	 * Whether the filename is a video file.
	 * 
	 * @param filename
	 * @return
	 *//*
	public static boolean isVideo(String filename) {
		String mimeType = getMimeType(filename);
		if (mimeType != null && mimeType.startsWith("video/")) {
			return true;
		} else {
			return false;
		}
	}*/

	/**
	 * Whether the URI is a local one.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isLocal(String uri) {
		if (uri != null && !uri.startsWith("http://")) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * Is this an image file
	 * @param path
	 * @return true if file is an image file
	 */
	public static boolean isImageFile(String path) {
		String extension = getExtension(path);
		if (extension != null) {
            if (JPG.equalsIgnoreCase(extension) || GIF.equalsIgnoreCase(extension) || PNG.equalsIgnoreCase(extension) || BMP.equalsIgnoreCase(extension) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get/Generate a thumbnail image from the image stored on a card
	 * @param context
	 * @param path
	 * @param width
	 * @param height
	 * @return Bitmap
	 */
	public static Bitmap getImageThumbnail(Context context, String path, int width, int height) {
		Cursor fileCursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " = '" + path + "'", null);
		int fileId = -1;
		if (fileCursor != null) {
			if (fileCursor.moveToFirst()) {
				int column = fileCursor.getColumnIndex(MediaStore.Images.Media._ID);
				if (column != -1) {
					fileId = fileCursor.getInt(column);
				}
			}
			fileCursor.close();
		}
        if (fileId == -1) {
            path = path.substring(path.lastIndexOf("/") + 1);
            fileCursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " LIKE  '%" + path + "%'", null);
            if (fileCursor != null) {
                if (fileCursor.moveToFirst()) {
                    int column = fileCursor.getColumnIndex(MediaStore.Images.Media._ID);
                    if (column != -1) {
                        fileId = fileCursor.getInt(column);
                    }
                }
                fileCursor.close();
            }
        }
        if (fileId != -1) {
			String[] whereArgs = new String[1];
			whereArgs[0] = String.valueOf(fileId);
			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Thumbnails._ID + " = ?" + fileId, whereArgs, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int column = cursor.getColumnIndex(MediaStore.Images.Thumbnails._ID);
					int thumbnailId = cursor.getInt(column);
					Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), thumbnailId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
					if (bitmap != null) {
						bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
						if (bitmap != null) {
							cursor.close();
							return bitmap;
						}
					}
				} else {
					// thumbnail not found - generate it
					Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), fileId, MediaStore.Images.Thumbnails.MICRO_KIND, null);
					if (bitmap != null) {
						bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
						if (bitmap != null) {
							cursor.close();
							return bitmap;
						}
					}

				}
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * Get the video thumbnail
	 * @param context
	 * @param path
	 * @param width
	 * @param height
	 * @return Bitmap
	 */
	public static Bitmap getVideoThumbnail(Context context, String path, int width, int height) {
		Cursor fileCursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media.DATA + " = '" + path + "'", null);
		int fileId = -1;
		if (fileCursor != null) {
			if (fileCursor.moveToFirst()) {
				int column = fileCursor.getColumnIndex(MediaStore.Video.Media._ID);
				if (column != -1) {
					fileId = fileCursor.getInt(column);
				}
			}
			fileCursor.close();
		}
		if (fileId == -1) {
            path = path.substring(path.lastIndexOf("/")+1);
            fileCursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Media.DATA + " LIKE  '%" + path + "%'", null);
            if (fileCursor != null) {
                if (fileCursor.moveToFirst()) {
                    int column = fileCursor.getColumnIndex(MediaStore.Video.Media._ID);
                    if (column != -1) {
                        fileId = fileCursor.getInt(column);
                    }
                }
                fileCursor.close();
            }
        }
		if (fileId != -1) {
			String[] whereArgs = new String[1];
			whereArgs[0] = String.valueOf(fileId);
			Cursor cursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, null, MediaStore.Video.Thumbnails._ID + " = ?" + fileId, whereArgs, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					int column = cursor.getColumnIndex(MediaStore.Images.Thumbnails._ID);
					int thumbnailId = cursor.getInt(column);
					Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), thumbnailId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
					if (bitmap != null) {
						bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
						if (bitmap != null) {
							cursor.close();
							return bitmap;
						}
					}
				} else {
					// thumbnail not found - generate it
					Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
					if (bitmap != null) {
						bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
						if (bitmap != null) {
							cursor.close();
							return bitmap;
						}
					}

				}
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * Is this an video file
	 * @param path
	 * @return true if file is an video file
	 */
	public static boolean isVideoFile(String path) {
		String extension = getExtension(path);
		if (extension != null) {
            if (THREE_GP.equalsIgnoreCase(extension) || MP4.equalsIgnoreCase(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if uri is a media uri.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isMediaUri(String uri) {
		if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Convert File into Uri.
	 * @param file
	 * @return uri
	 */
	public static Uri getUri(File file) {
		if (file != null) {
			return Uri.fromFile(file);
		}
		return null;
	}
	
	/**
	 * Convert Uri into File.
	 * @param uri
	 * @return file
	 */
	public static File getFile(Uri uri) {
		if (uri != null) {
			String filepath = uri.getPath();
			if (filepath != null) {
				return new File(filepath);
			}
		}
		return null;
	}
	
	/**
	 * Returns the path only (without file name).
	 * @param file
	 * @return
	 */
	public static File getPathWithoutFilename(File file) {
		 if (file != null) {
			 if (file.isDirectory()) {
				 // no file to be split off. Return everything
				 return file;
			 } else {
				 String filename = file.getName();
				 String filepath = file.getAbsolutePath();
	  
				 // Construct path without file name.
				 String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
				 if (pathwithoutname.endsWith("/")) {
					 pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
				 }
				 return new File(pathwithoutname);
			 }
		 }
		 return null;
	}

	/**
	 * Constructs a file from a path and file name.
	 * 
	 * @param curdir
	 * @param file
	 * @return
	 */
	public static File getFile(String curdir, String file) {
		String separator = "/";
		  if (curdir.endsWith("/")) {
			  separator = "";
		  }
		   File clickedFile = new File(curdir + separator
		                       + file);
		return clickedFile;
	}
	
	public static File getFile(File curdir, String file) {
		return getFile(curdir.getAbsolutePath(), file);
	}
}
