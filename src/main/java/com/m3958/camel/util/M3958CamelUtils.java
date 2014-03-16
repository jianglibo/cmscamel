package com.m3958.camel.util;

import java.util.Calendar;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

public class M3958CamelUtils {
	public static Pattern YMD_DASH_PATTERN = Pattern.compile("^catalina\\.(\\d{4})-(\\d{2})-(\\d{2})\\.log$");
	
	public static Set<String> ymdSet(String sep,int...dn){
		Set<String> ss = Sets.newHashSet();
		for(int oned : dn){
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, oned);
			String ydm = c.get(Calendar.YEAR) + sep;
			int m = c.get(Calendar.MONTH) + 1;
			if(m < 10){
				ydm = ydm + "0";
			}
			ydm += m + sep;
			int d = c.get(Calendar.DATE);
			if(d<10){
				ydm = ydm + "0";
			}
			ydm += d;
			ss.add(ydm);
		}
		return ss;
	}
}
