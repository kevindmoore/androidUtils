package com.mastertechsoftware.file;

import android.content.Context;

import com.mastertechsoftware.util.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
/**
 * File Cache for Data
 */
public class FileCache {
    protected static int defaultBufferSize = 8192;
    private static FileCache instance;
    private Context context;
	private File cacheDir;

    public static FileCache getInstance(Context context) {
        if (instance == null) {
            instance = new FileCache(context);
			instance.cacheDir = context.getCacheDir();
        }
        return instance;
    }

    public static FileCache getInstance() {
        return instance;
    }

    private FileCache(Context context) {
        this.context = context;
    }

	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}

	/**
     * Clear all cache files
     */
    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }
    public File getFileCache(String name) {
        return new File(cacheDir, name);
    }

    public String getFileCacheData(String name) {
        File cacheFile = getFileCache(name);
        StringBuilder stringBuilder = new StringBuilder();
        if (cacheFile.exists()) {
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(cacheFile));
                String line;
                while ((line = fileReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                fileReader.close();
            } catch (IOException e) {
                Logger.error("IOException", e);
            }
        }
        return stringBuilder.toString();
    }

    public void saveFileCache(String name, String data) {
        File fileCache = new File(cacheDir, name);
        try {
            FileWriter writer = new FileWriter(fileCache, false);
            writer.write(data);
            writer.close();
        } catch (FileNotFoundException e) {
            Logger.error("File not found", e);
        } catch (IOException e) {
            Logger.error("IOException", e);
        }
    }
}
