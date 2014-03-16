package com.m3958.camel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.exec.ExecBinding;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;


public class OneSizeProcessor {
	
	private static Joiner pathJoiner = Joiner.on(File.separatorChar);
	
	public static M3958CamelCfg ccfg = new M3958CamelCfg();
	
	private static Pattern SIZED_FILE_NAME = Pattern.compile("^\\d+\\.\\d+x\\d+\\.(.*)$");
	
	/*@copy from Asset*/
	
	

	public void p1(Exchange exchange) throws SQLException, IOException{
		Message in = exchange.getIn();
		String inBody = in.getBody(String.class).trim();//55,320x240,png
		if(inBody == null || inBody.isEmpty())return;
		
		String[] resizelog = inBody.split(",");
		if(resizelog.length != 3)return;
		String sid = resizelog[0].trim();
		String size = resizelog[1].trim();
		String[] wh = size.split("x");
		if(Integer.parseInt(wh[0]) > 2000 || Integer.parseInt(wh[1])>2000){
			return;
		}
		String ext = resizelog[2].trim();
		
		StringBuilder sb = new StringBuilder();
		int length = sid.length() < 4 ? sid.length() : 4;
		for(int i=0;i<length;i++){
			sb.append(sid.charAt(i)).append(File.separatorChar);
		}
//		sb.append(sid);
		
		File srcd = new File(ccfg.getAssetRoot(),sb.toString());
		
		if(!srcd.exists() || !srcd.isDirectory()){
			return;
		}
		final String sidWithDot = sid + ".";
		File[] files = srcd.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.indexOf(sidWithDot) == 0;
			}
		});
		
		List<File> sizedFiles = Lists.newArrayList(files);
		
		if(sizedFiles.size() > 6){//max 7 size
			Collections.sort(sizedFiles, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					long f1l = f1.lastModified();
					long f2l = f2.lastModified();
					System.out.println(f1.getName() + ":" + f1l);
					if(f1l > f2l){
						return 1;
					}else if(f1l < f2l){
						return -1;
					}else{
						return 0;
					}
				}
			});
			
			for(int i = sizedFiles.size() - 1;i > 5;i--){
				File f = sizedFiles.get(i);
				System.out.println(f.getName());
				Matcher m = SIZED_FILE_NAME.matcher(f.getName());
				if(m.matches()){
					System.out.println(f.getName() + "--- deleted!");
					f.delete();
				}
			}
		};
		String srcf = sid + "." + ext;
		String dstf = sid + "." + size + "." + ext;

		List<String> args = new ArrayList<String>();

		args.add(srcf);
		args.add("-resize");
		args.add(size + ">");
		args.add(dstf);
		
		Message out = exchange.getOut();
		out.setHeader(ExecBinding.EXEC_COMMAND_EXECUTABLE,ccfg.getConvertExec());
		out.setHeader(ExecBinding.EXEC_COMMAND_ARGS, args);
		out.setHeader(ExecBinding.EXEC_COMMAND_TIMEOUT,30000);
		out.setHeader(ExecBinding.EXEC_COMMAND_WORKING_DIR,srcd.getAbsolutePath());
	}
}
