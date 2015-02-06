package com.mastertechsoftware.util;

import android.content.Context;

import com.mastertechsoftware.json.JSONData;
import com.mastertechsoftware.json.JSONDataException;
import com.mastertechsoftware.util.log.Logger;
import com.mastertechsoftware.util.reflect.UtilReflector;

import java.util.List;
/**
 * Manage setting up Contexts for singletons
 */
public class ContextManager {
	public static final String CLASSES = "classes";
	public static final String NAME = "name";
	public static final String METHOD = "method";

	public static void runConfiguration(String json, Context context) {
		try {
			JSONData jsonData = new JSONData(json);
			if (jsonData.has(CLASSES)) {
				JSONData classes = jsonData.findChild(CLASSES);
				List<JSONData> children = classes.getChildren();
				for (JSONData childData : children) {
					if (childData.has(NAME)) {
						String className = childData.findChild(NAME).getStringValue();
						String methodName = null;
						if (childData.has(METHOD)) {
							methodName = childData.findChild(METHOD).getStringValue();
							UtilReflector.executeStaticMethod(className, methodName, new Class[] {Context.class}, new Object[] {context});

						} else {
							Logger.error("No method found for class " + className);
						}
					}
				}
			}
		} catch (JSONDataException e) {
			Logger.error("Problems reading configuraton file for Context Manager", e);
		}
	}
}
