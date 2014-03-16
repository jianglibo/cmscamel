package com.m3958.camel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;



public class MissingAssetProcessor {
	
	private static Pattern CORRECT_FILE_NAME = Pattern.compile("^/\\d/\\d/\\d/\\d/(\\d+)\\.(\\d+x\\d+)\\.(\\w{1,5})$");
	
	/*@copy from Asset*/
	
	

	public void p1(Exchange exchange) throws SQLException, IOException{
		Message in = exchange.getIn();
		String url = in.getBody(String.class).trim();// correct url /1/3/4/5/12345.55x455.jpg
		Matcher m = CORRECT_FILE_NAME.matcher(url);
		if(m.matches()){
			StringBuilder sb = new StringBuilder().append(m.group(1)).append(",").append(m.group(2)).append(",").append(m.group(3));
			exchange.getOut().setBody(sb.toString());
		}else{
			exchange.getOut().setHeader(Exchange.FILE_NAME, "nil.txt");
			exchange.getOut().setBody("");
		}
	}
}
