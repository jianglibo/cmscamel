/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.m3958.camel;

import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.Main;

/**
 * A Camel Router
 */
public class MysqlBinLogRouteBuilder extends RouteBuilder {
	
	private static String FROM_DIR = "e:/camelfiletest/from";
	
	private static Pattern LOG_PATTERN = Pattern.compile("mysqld-bin\\.(\\d){5,}",Pattern.CASE_INSENSITIVE);
	
    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main.main(args);
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
//    	from("timer://foo3?fixedRate=true&repeatCount=1")
//    	.process(new Processor() {
//			@Override
//			public void process(Exchange exchange) throws Exception {
//				File rf = new File(FROM_DIR);
//				String[] fs = rf.list(new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						System.out.println(name);
//						Matcher m = LOG_PATTERN.matcher(name);
//						return m.matches();
//					}
//				});
//			}
//		})
    	from("file://" + FROM_DIR +  "?recursive=false&filter=#logbinfilter&delay=5000")
    	.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message m = exchange.getIn();
				System.out.println(m.getHeader(Exchange.FILE_NAME));
			}
		})
		.to("file://e:/camelfiletest/to");
   	
    }

}
