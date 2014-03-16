package com.m3958.camel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;

import com.google.common.io.Files;
import com.m3958.camel.util.M3958CamelUtils;
import com.m3958.camel.util.Osdetecter;

public class ScheduleProducer {

	private File logpath = new File("/var/log/httpd");
	private String yuim3958logpathMoveTo = "/opt/camelworld/fromdir/yuicombolog";
	private String sharefilelogpathMoveTo = "/opt/camelworld/fromdir/sharefilelog";
	private File tomcatLogDir = new File("/var/log/tomcat6");
	private String tomcatLogMoveTo = "/opt/camelworld/fromdir/tomcatlog";
	
	public static Pattern CATALINA_DASH_PATTERN = Pattern.compile("^catalina\\.(\\d{4}-\\d{2}-\\d{2})\\.log$");
	
	public static Pattern LOCALHOST_DASH_PATTERN = Pattern.compile("^localhost\\.(\\d{4}-\\d{2}-\\d{2})\\.log$");
	
	public static Pattern MANAGER_DASH_PATTERN = Pattern.compile("^manager\\.(\\d{4}-\\d{2}-\\d{2})\\.log$");
	
	public static Pattern HOST_MANAGER_DASH_PATTERN = Pattern.compile("^host-manager\\.(\\d{4}-\\d{2}-\\d{2})\\.log$");
	
	
	
	public ScheduleProducer(){
		if(Osdetecter.isWindows()){
			logpath = new File("e:/camelfiletest/from");
			tomcatLogDir = new File("e:/camelfiletest/from");
			yuim3958logpathMoveTo = "e:/camelfiletest/to";
			tomcatLogMoveTo = "e:/camelfiletest/to";
		}
	}
	
	
	public void moveLogs(Exchange exchange)throws IOException{
		moveLog(yuim3958logpathMoveTo,"yuim3958_access_log");
		moveLog(sharefilelogpathMoveTo,"sharefile_access_log");
		moveTomcatLog();
	}
	
	public void moveTomcatLog() throws IOException {
		final Set<String>  ydmDashSet = M3958CamelUtils.ymdSet("-",-2,-1,0,1);
		File[] files = tomcatLogDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				Matcher m = CATALINA_DASH_PATTERN.matcher(name);
				if(!m.matches()){
					m=LOCALHOST_DASH_PATTERN.matcher(name);
					if(!m.matches()){
						m=MANAGER_DASH_PATTERN.matcher(name);
						if(!m.matches()){
							m=HOST_MANAGER_DASH_PATTERN.matcher(name); 
						}
					}
				}
				if(m.matches()){
					String ydm = m.group(1);
					if(ydmDashSet.contains(ydm)){
						return false;
					}else{
						return true;
					}
				}
				return false;
			}
		});
		for(File f : files){
			File dst = new File(tomcatLogMoveTo,f.getName());
			Files.move(f, dst);
		}
	}


	private  void moveLog(String moveTo,final String logPattern) throws IOException{
		File[] files = logpath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith(logPattern) && name.length() == logPattern.length() + 9){
					return true;
				}
				return false;
			}
		});
		for(File f : files){
			File dst = new File(moveTo,f.getName());
			Files.move(f, dst);
			Files.touch(new File(dst.getAbsoluteFile() + ".done"));
		}
	}
	
}
