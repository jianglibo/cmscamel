package com.m3958.camel;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;

public class LogBinFilter<T> implements GenericFileFilter<T>{
	
	private static Pattern LOG_PATTERN = Pattern.compile("mysqld-bin\\.(\\d){5,}",Pattern.CASE_INSENSITIVE);

	@Override
	public boolean accept(GenericFile<T> file) {
		Matcher m = LOG_PATTERN.matcher(file.getFileName());
		if(!m.matches())return false;
		
		File pf = new File(file.getParent());
		String[] fs = pf.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				Matcher m = LOG_PATTERN.matcher(name);
				return m.matches();
			}
		});
		Arrays.sort(fs);
		String newestfile = null;
		if(fs.length > 0){
			newestfile = fs[fs.length-1];
		}else{
			return false;
		}
		return !newestfile.equals(file.getFileName());
	}

}
