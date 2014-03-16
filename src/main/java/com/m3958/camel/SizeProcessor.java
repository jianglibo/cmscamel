package com.m3958.camel;

import java.io.File;
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
import com.m3958.camel.util.Osdetecter;



public class SizeProcessor {
	private static Joiner pathJoiner = Joiner.on(File.separatorChar);
	
	private static Pattern SIZED_FILE_NAME = Pattern.compile("^\\d+\\.\\d+x\\d+\\.(.*)$");

	public void p1(Exchange exchange) throws SQLException, IOException{
		Message in = exchange.getIn();
		String inBody = in.getBody(String.class).trim();
		String[] resizelog = inBody.split(",");//55,320x240,png
		String id = resizelog[0].trim();
		String size = resizelog[1].trim();
		String[] wh = size.split("x");
		if(Integer.parseInt(wh[0]) > 2000 || Integer.parseInt(wh[1])>2000){
			return;
		}
		String ext = resizelog[2].trim();

		File srcd;
		if(Osdetecter.isWindows()){
			srcd = new File("e:/assetroot",pathJoiner.join(id.split("", 5)));
		}else{
			srcd = new File("/opt/assetroot",pathJoiner.join(id.split("", 5)));
		}
		
		List<File> sizedFiles = Lists.newArrayList(srcd.listFiles());
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
//			Collections2.orderedPermutations(Lists.newArrayList(sizedFiles),new  Comparator<File>() {
//
//				@Override
//				public int compare(File o1, File o2) {
//					
//					return 0;
//				}
//			});
//			Collections2.filter(Lists.newArrayList(sizedFiles), new Predicate<File>() {
//				@Override
//				public boolean apply(File input) {
//					 input.
//					return false;
//				}
//			});
		};
		String srcf = id + "." + ext;
		String dstf = id + "." + size + "." + ext;

		List<String> args = new ArrayList<String>();

		args.add(srcf);
		args.add("-resize");
		args.add(size);
		args.add(dstf);
//		for(String a : args){
//			System.out.println(a);
//		}
		Message out = exchange.getOut();
		out.setHeader(ExecBinding.EXEC_COMMAND_EXECUTABLE,"/usr/bin/convert");
		out.setHeader(ExecBinding.EXEC_COMMAND_ARGS, args);
		out.setHeader(ExecBinding.EXEC_COMMAND_TIMEOUT,30000);
		out.setHeader(ExecBinding.EXEC_COMMAND_WORKING_DIR,srcd.getAbsolutePath());
	}
}
