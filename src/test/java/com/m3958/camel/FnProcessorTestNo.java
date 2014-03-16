package com.m3958.camel;

import java.io.File;
import java.io.FilenameFilter;

import junit.framework.Assert;

import org.junit.Test;

import com.m3958.camel.NewTtBuilder.Asset;

public class FnProcessorTestNo {
	
	@Test
	public void t(){
		String fn = "5401.jpg";
		String fn1 = Asset.translatePath(fn, "48x48");
		Assert.assertEquals("5\\4\\0\\1\\5401.48x48.jpg", fn1);
		
		fn = "5/4/0/5401.jpg";
		fn1 = Asset.translatePath(fn, "48x48");
		Assert.assertEquals("5\\4\\0\\1\\5401.48x48.jpg", fn1);
	}
	
	@Test
	public void t2(){
		String p = "E:\\assetroot\\new\\5\\6\\5\\1";
		final String fn = "5651.";
		File pf = new File(p);
		File[] fs = pf.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.indexOf(fn) == 0;
			}
		});
		
		Assert.assertEquals(1, fs.length);
	}
}
