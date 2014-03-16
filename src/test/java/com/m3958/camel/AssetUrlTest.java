package com.m3958.camel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

public class AssetUrlTest {

	private static Pattern CORRECT_FILE_NAME = Pattern.compile("^/\\d/\\d/\\d/\\d/(\\d+)\\.(\\d+x\\d+)\\.(\\w{1,5})$");
	
	//^/\\d/\\d/\\d/\\d/\\d+/\\.\\d+x\\d+\\.\\w{1,5}$
	
	@Test
	public void t1(){
//		String  s = "/1/3/4/5/12345.55x455.jpg";
		String  s = "/1/3/4/5/12345.55x455.jpg";
		Matcher m = CORRECT_FILE_NAME.matcher(s);
		Assert.assertTrue(m.matches());
		StringBuilder sb = new StringBuilder().append(m.group(1)).append(",").append(m.group(2)).append(",").append(m.group(3));
		Assert.assertEquals("12345,55x455,jpg",sb.toString());
	}
	
}
