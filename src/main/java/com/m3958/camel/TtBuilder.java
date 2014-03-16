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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.exec.ExecBinding;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.Main;

import com.m3958.camel.ClonePage.ClonePageTaskDescription;
import com.m3958.camel.util.Osdetecter;




/**
 * A Camel Router
 */
public class TtBuilder extends RouteBuilder {
	
	
	
//	org.apache.camel.component.file.GenericFile
	
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
    	
    	errorHandler(deadLetterChannel("log:com.m3958.camel?showException=true&multiline=true").redeliveryDelay(3).maximumRedeliveries(3).logStackTrace(true));
    	
    	boolean productionMode = true;
    	
    	if(Osdetecter.isWindows()){
    		productionMode = false;
    	}
    	
    	if(productionMode){
        	String accessJsonFrom = "file:///opt/camelworld/fromdir/accesslogjson?doneFileName=${file:name}.done";
        	String accessJsonToUrl = "http4://sharefile.m3958.com:8080/opendoor?authUsername=presidentskroob&authPassword=ludicrousspeed&dowhat=accessLog";
        	if(Osdetecter.isWindows()){
        		accessJsonFrom = "file://e:/camelfiletest/from?doneFileName=${file:name}.done";
        		accessJsonToUrl = "http4://localhost/opendoor?authUsername=presidentskroob&authPassword=ludicrousspeed&dowhat=accessLog";
        	}
        	
        	from(accessJsonFrom).
    			setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant("UTF-8")).
    			setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST)).to(accessJsonToUrl);
    	}
    	

    	if(productionMode){
        	String sharefileLogSrc = "file:///opt/camelworld/fromdir/sharefilelog?doneFileName=${file:name}.done";
        	
        	if(Osdetecter.isWindows()){
        		 sharefileLogSrc = "file://e:/camelfiletest/from?doneFileName=${file:name}.done";
        	}
        	from(sharefileLogSrc).beanRef("sharefileAccessParser","p1");
    	}

    	
    	if(productionMode){
	    	String sizeLog = "file:///var/www/sizelog";
	    	if(Osdetecter.isWindows()){
	    		sizeLog = "file://e:/sizelog";
	    	}
	    	from(sizeLog).beanRef("sizelogProcessor", "p1").to("exec:///usr/bin/convert");
	//    	.process(new Processor() {
	//			@Override
	//			public void process(Exchange exchange) throws Exception {
	//				Message in = exchange.getIn();
	//				ExecResult er = in.getBody(ExecResult.class);
	//				System.out.println(er.getExitValue());
	//				System.out.println(CharStreams.toString(new InputStreamReader(er.getStderr())));
	//				System.out.println(CharStreams.toString(new InputStreamReader(er.getStdout())));
	//			}
	//		});
    	}
    	if(productionMode){
    		//s m h dom m dow y
    		//1:30 every day
    		from("quartz://myGroup/myTimerName?cron=0+30+1+?+*+*+*").
    			beanRef("scheduleProducer","moveLogs").
    			setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant("UTF-8")).
    			setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST)).
    			to("http4://sharefile.m3958.com:8080/opendoor?authUsername=presidentskroob&authPassword=ludicrousspeed&dowhat=outOfService");
    	}
    	
    	
    	if(productionMode){
    		String yuicombolog = "file:///opt/camelworld/fromdir/yuicombolog?delay=50000&doneFileName=${file:name}.done";
    		if(Osdetecter.isWindows()){
    			yuicombolog = "file://E:/camelfiletest/from?delay=500&doneFileName=${file:name}.done";
    		}
    		from(yuicombolog).beanRef("yuicombolog", "p1").beanRef("yuicombolog","p2");
    	}
    	
    	if(productionMode){//solr updater
    		String solrXmlDir = "file:///opt/camelworld/fromdir/solrcameldir?delete=true&delay=5000";
    		String solrUrl = "http4://localhost:8080/solr-numberone/update";
        	if(Osdetecter.isWindows()){
            	solrXmlDir = "file://e:/solrcameldir";
            	solrUrl = "http4://localhost:8080/solr-numberone/update";
        	}
        	from(solrXmlDir).setHeader(Exchange.HTTP_QUERY, constant("commit=true")).
    			setHeader(Exchange.CONTENT_TYPE, constant("Conntent-Type: text/xml")).
    			setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant("UTF-8")).
    			setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST)).to(solrUrl);
    	}
    	
    	if(productionMode){
        	String ff = "file:///opt/camelworld/fromdir/camelinterchangedir?delay=5000";
        	if(Osdetecter.isWindows()){
        		ff = "file://e:/camelinterchangedir?delay=5000";
        	}
        	
        	from(ff)
        	.unmarshal().json(JsonLibrary.Gson,ClonePageTaskDescription.class)
        	.beanRef("clonePage","p1")
        	.to("http4://will-replace-by-header")
    		.beanRef("clonePage", "p2")
        	.to("log:com.m3958.camel?showException=true&multiline=true")
        	.split().body()
        	.choice()
        	.when().simple("${in.body[0]} == 'img'")
        		.to("direct:imgstart")
    		.when().simple("${in.body[0]} == 'css'")
        	    .setHeader(Exchange.HTTP_URI, simple("http4://${in.body[1]}"))
        	    .setHeader("clonepage-filename",simple("${in.body[2]}"))
        	    .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
    		   	.setBody(simple(""))
        		.to("http4://get-from-header")
        		.process(new Processor() {
    				@Override
    				public void process(Exchange exchange) throws Exception {
    					Message in = exchange.getIn();
    					String b = in.getBody(String.class);
    					
    					String pageUrl = in.getHeader(Exchange.HTTP_URI,String.class);
    			    	List<String[]> relations = new ArrayList<String[]>();
    			    	//url(../Images/lian.gif)
    			    	Pattern p = Pattern.compile("url\\s*?\\(['\"\\s]*(.*?)['\"\\s]*\\)");
    			    	Matcher m = p.matcher(b);
    			    	String[] hu = ClonePage.getHostnameUri(pageUrl);
    			    	StringBuffer sb = new StringBuffer();
    			    	while(m.find()){
    			    		String url = m.group(1);
    			    		String[] item = ClonePage.getOneItem(url, "img", null, hu[0], hu[1]); 
    			    		relations.add(item);
    			    		m.appendReplacement(sb, "url(" + item[2] + ")");
    			    	}
    			    	m.appendTail(sb);
    			    	ClonePage.writeTextFile(in, sb.toString());
    			    	exchange.getOut().setBody(relations);
    			    	exchange.getOut().setHeaders(in.getHeaders());
    				}
    			}).to("direct:cssimgstart")
    		.when().simple("${in.body[0]} == 'script'")
        	    .setHeader(Exchange.HTTP_URI, simple("http4://${in.body[1]}"))
        	    .setHeader("clonepage-filename",simple("${in.body[2]}"))
        	    .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
    		    .setBody(simple(""))
    		    .to("http4://get-from-header")
    		    .process(new Processor() {
    				@Override
    				public void process(Exchange exchange) throws Exception {
    					Message in = exchange.getIn();
    					String b = in.getBody(String.class);
    					ClonePage.writeTextFile(in, b);
    				}
    			})
        	.otherwise()
        		.to("log:com.m3958.camel?showException=true&multiline=true")
    		.end()
    		.end()
    		.setHeader(ExecBinding.EXEC_COMMAND_WORKING_DIR, header("clonepage-siteroot"))
    		.setHeader(ExecBinding.EXEC_COMMAND_ARGS, simple("-R tomcat:tomcat ../"))
//    		.process(new Processor() {
//				@Override
//				public void process(Exchange exchange) throws Exception {
//					Message in = exchange.getIn();
//					Message out = exchange.getOut();
//					String f = (String) in.getHeader("clonepage-siteroot");
//					out.setHeaders(in.getHeaders());
//					out.setHeader(ExecBinding.EXEC_COMMAND_WORKING_DIR, f);
//				}
//			})
    		.to("exec:chown");
//    		.process(new Processor() {
//				@Override
//				public void process(Exchange exchange) throws Exception {
//					Message in = exchange.getIn();
//					System.out.print(in.getBody(String.class));
//					
//				}
//			});// + simple("header[clonepage-siteroot]"));
        	
        	
        	from("direct:imgstart")
        	.doTry()
        	    .setHeader(Exchange.HTTP_URI, simple("http4://${in.body[1]}"))
        	    .setHeader("clonepage-filename",simple("${in.body[2]}"))
        	    .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
        		.setBody(simple(""))
        	   	.to("http4://get-from-header")
        		.process(new Processor() {
    				@Override
    				public void process(Exchange exchange) throws Exception {
    					Message in = exchange.getIn();
    					ClonePage.writeBinaryFile(in);//.to("file://" + simple("${header['clonePage-siteroot']}"))//因为写入的目标不确定，所以采用自己的实现是比较明智的做法。
    				}
    		})
    		.doCatch(Exception.class)
    			.to("log:com.m3958.camel?showException=true&multiline=true")
    		.end();
        	
        	from("direct:cssimgstart")
        	.split().body()
        	.to("direct:imgstart");
    	}
    	
    }

}
