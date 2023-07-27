package com.xavor.plmxl.assist.Util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Date;


public final class GenericComparator implements Comparator, Serializable {    
	private static final long serialVersionUID = -2293914106471884607L;	
	// Logger
	AssistLogger log = AssistLogger.getInstance();
	
	private static final int LESSER = -1;
	private static final int EQUAL = 0;
	private static final int GREATER = 1;
	private static final String METHOD_GET_PREFIX = "get";
	private static final String DATATYPE_STRING = "java.lang.String";
	private static final String DATATYPE_DATE = "java.util.Date";
	private static final String DATATYPE_INTEGER = "java.lang.Integer";
	private static final String DATATYPE_LONG = "java.lang.Long";
	private static final String DATATYPE_FLOAT = "java.lang.Float";
	private static final String DATATYPE_DOUBLE = "java.lang.Double";
	private enum CompareMode { EQUAL, LESS_THAN, GREATER_THAN, DEFAULT }

	// generic comparator attributes
	private String targetMethod;
	private boolean sortAscending;
	

	/**
	 * <p>constructor with <code>sortField, sortAscending</code> parameter for Derived type of <code>Class</code></p>
	 * 
	 * <p>For Example-</p> 
	 * <p><code>PersonVO person = new PersonVO();<br />
	 * person.setId(10001);<br />
	 * person.setName("Jacob");<br />
	 * <p>and person2, person3, so on.. And Defining &amp; adding all the created objects in to below list</p>  
	 * <p><code>List&lt;PersonVO&gt; persons = new ArrayList&lt;PersonVO&gt;();<br />
	 * persons.add(person1);<br />
	 * persons.add(person2);<br />
	 * persons.add(person3); </code>and so on <br /> 
	 * <p>Invoking sort method with passing <code>{@link com.myjeeva.comparator.GenericComparator}</code> for  <br /> 
	 * <code>Collections.sort(persons, new GenericComparator("name"), false);</code><br /></p> 
	 * @param sortField - a {@link java.lang.String} - which field requires sorting; as per above example "sorting required for <code>name</code> field"
	 * @param sortAscending - a {@link boolean} - <code>true</code> ascending order or <code>false</code> descending order
	 */
	public GenericComparator(String sortField, boolean sortAscending) {
		super();
		this.targetMethod = prepareTargetMethod(sortField);
		this.sortAscending = sortAscending;
	}

	/**
	 * {@inheritDoc}
	 */
	
	public int compare(Object o1, Object o2) {
		int response = LESSER;
		try {
			Object v1 = (null == this.targetMethod) ? o1 : getValue(o1);
			Object v2 = (null == this.targetMethod) ? o2 : getValue(o2);		
			CompareMode cm = findCompareMode(v1, v2);
			
			if (!cm.equals(CompareMode.DEFAULT)) {
				return compareAlternate(cm);
			}

			final String returnType = (null == this.targetMethod) 
											? o1.getClass().getName() : getMethod(o1).getReturnType().getName();
			response = compareActual(v1, v2, returnType);
		} catch (NoSuchMethodException nsme) {
			log.error("NoSuchMethodException occurred while comparing", nsme);
		} catch (IllegalAccessException iae) {
			log.error("IllegalAccessException occurred while comparing", iae);
		} catch (InvocationTargetException ite) {
			log.error("InvocationTargetException occurred while comparing", ite);
		}
		return response;
	}

	/**
	 * alternate to actual value comparison i.e., either (lsh &amp; rhs) one the value could be null  
	 * 
	 * @param cm - a enum used to idetify the position for sorting
	 */
	private int compareAlternate(CompareMode cm) {
		int compareState = LESSER;
		switch(cm) {
			case LESS_THAN:
				compareState = LESSER * determinePosition();
				break;
			case GREATER_THAN:
				compareState = GREATER * determinePosition();
				break;
			case EQUAL:
				compareState = EQUAL * determinePosition();
				break;
		}
		return compareState;
	}	

	/**
	 * actual value comparison for sorting; both lsh &amp; rhs value available
	 * 
	 * @param v1 - value of lhs
	 * @param v2 - value of rhs
	 * @param returnType - datatype of given values
	 * @return int - compare return value
	 */
	private int compareActual(Object v1, Object v2, String returnType) {
		int acutal = LESSER;
		if (returnType.equals(DATATYPE_INTEGER)) {
			acutal = (((Integer) v1).compareTo((Integer) v2) * determinePosition());
		} else if (returnType.equals(DATATYPE_LONG)) {
			acutal = (((Long) v1).compareTo((Long) v2) * determinePosition());
		} else if (returnType.equals(DATATYPE_STRING)) {
			acutal = (((String) v1).compareTo((String) v2) * determinePosition());
		} else if (returnType.equals(DATATYPE_DATE)) {
			acutal = (((Date) v1).compareTo((Date) v2) * determinePosition());
		} else if (returnType.equals(DATATYPE_FLOAT)) {
			acutal = (((Float) v1).compareTo((Float) v2) * determinePosition());
		} else if (returnType.equals(DATATYPE_DOUBLE)) {
			acutal = (((Double) v1).compareTo((Double) v2) * determinePosition());
		}
		return acutal;
	}
	
	/**
	 * preparing target name of getter method for given sort field
	 * 
	 * @param name a {@link java.lang.String}
	 * @return methodName a {@link java.lang.String}
	 */
	private final static String prepareTargetMethod(String name) {
		StringBuffer fieldName =  new StringBuffer(METHOD_GET_PREFIX);
		fieldName.append(name.substring(0, 1).toUpperCase());
		fieldName.append(name.substring(1));
		return fieldName.toString();
	}	

	/**
	 * fetching method from <code>Class</code> object through reflect
	 * 
	 * @param obj - a {@link java.lang.Object} - input object
	 * @return method - a {@link java.lang.reflect.Method}
	 * @throws NoSuchMethodException 
	 */
	private final Method getMethod(Object obj) throws NoSuchMethodException {
		return obj.getClass().getMethod(targetMethod, null);
	}

	/**
	 * dynamically invoking given method with given object through reflect
	 * 
	 * @param method - a {@link java.lang.reflect.Method}
	 * @param obj - a {@link java.lang.Object}
	 * @return object - a {@link java.lang.Object} - return of given method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private final static Object invoke(Method method, Object obj) throws InvocationTargetException, IllegalAccessException {		
		return method.invoke(obj, null);
	}
	
	/**
	 * fetching a value from given object
	 * 
	 * @param obj - a {@link java.lang.Object}
	 * @return object - a {@link java.lang.Object} - return of given method
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 */
	private Object getValue(Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		return invoke(getMethod(obj), obj);
	}
	
	/**
	 * identifying the comparison mode for given value 
	 * 
	 * @param o1 - a {@link java.lang.Object}
	 * @param o2 - a {@link java.lang.Object}
	 * @return compareMode - a {@link com.myjeeva.comparator.GenericComparator.CompareMode}
	 */
	private CompareMode findCompareMode(Object o1, Object o2) {
		CompareMode cm = CompareMode.LESS_THAN;
		
		if(null != o1 && null != o2) {
			cm = CompareMode.DEFAULT;
		} else if (null == o1 && null != o2) {
			cm = CompareMode.LESS_THAN;
		} else if (null != o1 && null == o2) {
			cm = CompareMode.GREATER_THAN;
		} else if (null == o1 && null == o2) {
			cm = CompareMode.EQUAL;			
		}
		
		return cm;		
	}	 

	/**
	 * Determining positing for sorting
	 * 
	 * @return -1 to change the sort order if appropriate.
	 */
	private int determinePosition() {
		return sortAscending ? GREATER : LESSER;
	}
}
