package com.mastertechsoftware.util.reflect;


import com.mastertechsoftware.util.log.Logger;

import java.lang.reflect.*;
import java.util.ArrayList;

/**
* Author: Kevin Moore
* Descrption: Class to get the fields from another class
* Note: Put a wrapper class around this class to access fields & methods from class in other packages
*/
public class UtilReflector
{

    /**
     * Return the class object associated with this string.
     * @param className
     * @return Class
     * @throws ClassNotFoundException
     */
    public static Class getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Return a list of constructors for this class.
     * @param mainClass class to get the constructors from
     * @return Constructor[]
     */
    public static Constructor[] getConstructors(Object mainClass) {
        return mainClass.getClass().getConstructors();
    }

    /**
     * Return a list of constructors for this class.
     * @param mainClass class to get the constructors from
     * @return Constructor[]
     */
    public static Constructor[] getConstructors(Class mainClass) {
        return mainClass.getConstructors();
    }

    /**
     * Create a new instance of a class.
     * @param constructor constructor class. Use the getConstructors method above
     * @param args arguments for constructor
     * @return new Object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    public static Object createInstance(Constructor constructor, Object...args)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return constructor.newInstance(args);
    }

    /**
	* getField - method to return a field object
	* @param mainClass class to check for field
	* @param fieldName field name  to check for
	* @return field object
	*/
	public static Object getField(Object mainClass, String fieldName)
	{
		if (mainClass == null)
		{
	    	Logger.error("Object is null for field " + fieldName);
	    	return null;
	    }

		Class mainClassObject = mainClass.getClass();
		if (mainClassObject == null)
		{
	    	Logger.error("Class object is null for field " + fieldName);
	    	return null;
	    }
		boolean found = false;
		while (!found)
		{
	    	try
	    	{
		   		Field fieldObject = mainClassObject.getDeclaredField(fieldName);
	    		fieldObject.setAccessible(true);
	    		Object itemObject = fieldObject.get(mainClass);
				return itemObject;
        	}
	    	catch (NoSuchFieldException nsf)
	    	{
	    		mainClassObject = mainClassObject.getSuperclass();
	    		if (mainClassObject == null)
				{
		    		Logger.error("No Such Field Exception: " + nsf.getMessage() + " for field " + fieldName);
	    			return null;
	    		}
	    	}
	    	catch (SecurityException security)
	    	{
	    		Logger.error("Security Exception: " + security.getMessage() + " for class " + mainClassObject.getName() + " & field " + fieldName);
	    		return null;
	    	}
	    	catch (Exception e)
	    	{
				Logger.error("Error in getting field " + fieldName + " for Class " + mainClassObject.getName());
	    		Logger.error("Unknown Error", e);
	    		return null;
	    	}
	    }
	    return null;
	}

	/**
	* checkField - method to check for the existance of a field
	* @param mainClass class to check for field
	* @param fieldName field name  to check for
	* @return true if field exists
	*/
	public static boolean checkField(Object mainClass, String fieldName)
	{
		Object field = getField(mainClass, fieldName);
		if (field == null)
			return false;
		else
			return true;
	}

	/**
	* getFields - method to return the names of all of the fields
	* @param mainClass class to check for field
	* @return array of fields
	*/
	public static Field[] getFields(Class mainClass)
	{
	    try
	    {
		   	Field[] fields = mainClass.getDeclaredFields();
			return fields;
        }
	    catch (SecurityException security)
	    {
	    	Logger.error("Security Exception: " + security.getMessage() + " for class " + mainClass.getName());
	    	return null;
	    }
	    catch (Exception e)
	    {
	    	Logger.error("Unknown Error", e);
	    	return null;
	    }
	}

	/**
	* getAllFields - method to return the names of all of the fields and the fields of the superclasses
	* @param mainClass class to check for field
	* @return array of fields
	*/
	public static ArrayList<Field> getAllFields(Class mainClass)
	{
		ArrayList<Field> list = new ArrayList<Field>();
		while (mainClass != null)
		{
		   	Field[] fields = getFields(mainClass);
		   	for (int i=0; i < fields.length; i++)
		   	{
		   		list.add(fields[i]);
		   	}
		   	mainClass = mainClass.getSuperclass();
		}
		return list;
	}

	/**
	* getFieldNames - method to return the names of all of the fields
	* @param mainClass class to check for field
	* @return array of field names
	*/
	public static String[] getFieldNames(Class mainClass)
	{
	    try
	    {
		   	Field[] fields = mainClass.getDeclaredFields();
		   	String[] fieldNames = new String[fields.length];
		   	for (int i=0; i < fields.length; i++)
		   	{
		   		fieldNames[i] = fields[i].getName();
		   	}
			return fieldNames;
        }
	    catch (SecurityException security)
	    {
	    	Logger.error("Security Exception: " + security.getMessage() + " for class " + mainClass.getName());
	    	return null;
	    }
	    catch (Exception e)
	    {
	    	Logger.error("Unknown Error", e);
	    	return null;
	    }
	}

	/**
	* checkMethod - method to check for the existance of a method
	* @param mainClass class to check for method
	* @param methodName method name  to check for
     * @param params array of classes describing parameters
	* @return true if method exists
	*/
	public static boolean checkMethod(Object mainClass, String methodName, Class[] params)
	{
		Method aMethod = getMethod(mainClass, methodName, params);
		if (aMethod == null)
			return false;
		else
			return true;
	}

	/**
	* getMethod - return a method object
	* @param mainClass class to check for method
	* @param methodName method name  to check for
    * @param params array of classes describing parameters
	* @return Method object
	*/
	public static Method getMethod(Object mainClass, String methodName, Class[] params)
	{
		if (mainClass == null)
		{
			Logger.error("Error: Null main class in getting method " + methodName);
			return null;
		}
		Class mainClassObject = null;
        if (mainClass instanceof Class)
            mainClassObject = (Class)mainClass;
        else
            mainClassObject = mainClass.getClass();
		if (mainClassObject == null)
		{
			Logger.error("Error: Null main class object in getting method " + methodName);
			return null;
		}
		boolean found = false;
		while (!found)
		{
	    	try
	    	{
			   	Method methodObject = mainClassObject.getDeclaredMethod(methodName, params);
				found = true;
				return methodObject;
        	}
	    	catch (NoSuchMethodException nsm)
	    	{
	    		mainClassObject = mainClassObject.getSuperclass();
	    		if (mainClassObject == null)
				{
		    		Logger.error("No Such Method Exception: " + nsm.getMessage() + " for method " + methodName);
	    			return null;
	    		}
	    	}
	    	catch (SecurityException security)
	    	{
	    		Logger.error("Security Exception: " + security.getMessage() + " for class " + mainClassObject.getName());
	    		return null;
	    	}
	    	catch (Exception e)
	    	{
	    		Logger.error("Unknown Error", e);
	    		return null;
	    	}
	    }
	    return null;
	}

    /**
    * return an array of method object
    * @param mainClass class to check for method
    * @return Method[]
    */
    public static Method[] getMethods(Object mainClass)
    {
        if (mainClass == null)
        {
            Logger.error("Error: Null main class in getting methods ");
            return null;
        }
        Class mainClassObject = null;
        if (mainClass instanceof Class)
            mainClassObject = (Class)mainClass;
        else
            mainClassObject = mainClass.getClass();
        if (mainClassObject == null)
        {
            Logger.error("Error: Null main class object in getting methods ");
            return null;
        }
        boolean found = false;
        while (!found)
        {
            try
            {
                Method[] methodObjects = mainClassObject.getDeclaredMethods();
                found = true;
                return methodObjects;
            }
            catch (SecurityException security)
            {
                Logger.error("Security Exception: " + security.getMessage() + " for class " + mainClassObject.getName());
                return null;
            }
            catch (Exception e)
            {
                Logger.error("Unknown Error", e);
                return null;
            }
        }
        return null;
        
    }

	/**
	* executeMethod - return a method object
	* @param mainClass class to check for method
	* @param methodName method name  to check for
    * @param params array of parameter classes
    * @param args array of actual arguments
	* @return Method object
	*/
	public static Object executeMethod(Object mainClass, String methodName, Class[] params, Object[] args)
	{
		if (mainClass == null)
		{
			Logger.error("Error: Null main class in executing method " + methodName);
			return null;
		}
		Method methodObject = getMethod(mainClass, methodName, params);

		if (methodObject == null)
		{
			Logger.error("Error: Null method object in executing method " + methodName);
			return null;
		}
        return executeMethod(mainClass, args, methodObject);
    }

    public static Object executeMethod(Object mainClass, Object[] args, Method methodObject) {
        try
        {
            methodObject.setAccessible(true);
            Object returnObject = methodObject.invoke(mainClass, args);
            return returnObject;
		}
        catch (IllegalAccessException iae)
        {
            Logger.error("Illegal Access Exception: " + iae.getMessage() + " for method " + methodObject.getName());
            return null;
        }
        catch (IllegalArgumentException iArge)
        {
            Logger.error("Illegal Arguement Exception: " + iArge.getMessage() + " for method " + methodObject.getName());
            return null;
        }
        catch (InvocationTargetException ite)
        {
            Logger.error("Invocation Target Exception: " + ite.getMessage() + " for method " + methodObject.getName());
            return null;
        }
        catch (Exception e)
        {
            Logger.error("Unknown Error", e);
            return null;
        }
    }

    /**
	* executeMethod - return a method object
	* @param readMethod class to check for method
	* @param classInstance method name  to check for
	* @return Method object
	*/
	public static Object getReadMethodObject(Method readMethod, Object classInstance)
	{
		if (readMethod == null)
		{
	    	Logger.error("Read Method null in getReadMethodObject");
			return null;
		}
		Object returnValue = null;
		try
		{
			Class[] params = readMethod.getParameterTypes();
			Object[] args = new Object[params.length];
			for (int j=0; j < params.length; j++)
			{
				Logger.error("Parameter Name: " + params[j].getName());
				args[j] = params[j].newInstance();
			}

			if (Modifier.isStatic(readMethod.getModifiers()))
			{
				if (params.length == 0)
					returnValue = readMethod.invoke(null, null);
				else
					returnValue = readMethod.invoke(null, args);
			}
			else
			{
				if (params.length == 0)
					returnValue = readMethod.invoke(classInstance, null);
				else
					returnValue = readMethod.invoke(classInstance, args);
			}
		}
	    catch (Exception e)
	    {
	    	Logger.error("Unknown Error", e);
	    }
	    return returnValue;
	}

}
