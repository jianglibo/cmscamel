package com.m3958.camel.unused;

import java.io.File;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class FileProcessRouterBuilder extends RouteBuilder{

	@Override
	public void configure() throws Exception {
		
		from("file://e:/camelfiletest/from?recursive=true&noop=true")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				System.out.println(in.getHeader(Exchange.FILE_NAME));
				String fp = (String) in.getHeader("CamelFilePath");
				System.out.println(fp);
				Files.readLines(new File(fp), Charsets.UTF_8, new LineProcessor<String>() {

					@Override
					public String getResult() {
						return null;
					}

					@Override
					public boolean processLine(String line) throws IOException {
						System.out.println(line);
						return true;
					}
				});
				
			}
		}).
		to("file://e:/camelfiletest/to");
		
	}

}
