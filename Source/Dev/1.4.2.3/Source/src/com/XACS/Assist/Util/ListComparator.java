package com.XACS.Assist.Util;

import java.util.Comparator;

import com.XACS.Assist.DO.CListModel;

public class ListComparator implements Comparator<CListModel>
{

	public int compare(CListModel o1, CListModel o2) 
	{
		 return o1.getValue().compareToIgnoreCase(o2.getValue());
	}
	
}
