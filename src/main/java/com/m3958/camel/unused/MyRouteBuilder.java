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
package com.m3958.camel.unused;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.spring.Main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;

/**
 * A Camel Router
 */
public class MyRouteBuilder extends RouteBuilder {
	
	public static class ArrayListAggregationStrategy implements AggregationStrategy {

	    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		Object newBody = newExchange.getIn().getBody();
		ArrayList<Object> list = null;
	        if (oldExchange == null) {
				list = new ArrayList<Object>();
				list.add(newBody);
				newExchange.getIn().setBody(list);
				return newExchange;
	        } else {
		        list = oldExchange.getIn().getBody(ArrayList.class);
		        list.add(newBody);
		        return oldExchange;
	        }
	    }
	}
	
	private static Joiner ARTICLE_ID_JOINER = Joiner.on(",").skipNulls();
	
	private static Pattern pattern = Pattern.compile("'(t\\d{8}_\\d{8,}\\.htm)'",Pattern.CASE_INSENSITIVE);
	private static Pattern pattern1 = Pattern.compile("<span class=\"content\">(.*?)</span>",Pattern.CASE_INSENSITIVE);
	
	 private static String OWNER = "nbfhcg@m3958.com";
	 
	 private static int DEST_SITE_ID = 8920;
	 
	 private static int XJ_CAT_ID = 20735;
	 
	 private static String LIUYAN_RECEIVER =  "EB294670CAAC11DE90D0D9B93CBE188A";
	 
	 private static String DEST_URL = "http4://127.0.0.1:8888";

	 
	private static ObjectMapper mapper = new ObjectMapper();
	
	static{
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}
	

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

        

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message
        // using XPath
//        from("file:src/data?noop=true").
//            choice().
//                when(xpath("/person/city = 'London'")).to("file:target/messages/uk").
//                otherwise().to("file:target/messages/others");
        
//        The sample code below produces files using the message ID as the filename:
//        	from("direct:report").to("file:target/reports");
//        	To use report.txt as the filename you have to do:
//        	from("direct:report").setHeader(Exchange.FILE_NAME, constant("report.txt")).to( "file:target/reports");
//        	... the same as above, but with CamelFileName:
//        	from("direct:report").setHeader("CamelFileName", constant("report.txt")).to( "file:target/reports");
//        	And a syntax where we set the filename on the endpoint with the fileName URI option.
//        	from("direct:report").to("file:target/reports/?fileName=report.txt");
        
        

//Attribute	 Type	 Value
//context	org.apache.camel.CamelContext	 The Camel Context
//exchange	org.apache.camel.Exchange	 The current Exchange
//request	org.apache.camel.Message	 The IN message
//response	org.apache.camel.Message	 The OUT message
//properties	org.apache.camel.builder.script.PropertiesFunction	Camel 2.9: Function with a resolve method to make it easier to use Camels Properties component from scripts. See further below for example.
        
        
//        from("timer://foo?fixedRate=true&delay=0&repeatCount=1")
//        		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
//        		.to("http4://www.stats.gov.cn/tjbz/xzqhdm/")
//        		.process(new Processor() {
//					@Override
//					public void process(Exchange exchange) throws Exception {
//						Message in = exchange.getIn();
//						String inStr = in.getBody(String.class);
//						Matcher m = pattern.matcher(inStr);
//						if(m.find()){
//							in.setHeader("newurl", "http4://www.stats.gov.cn/tjbz/xzqhdm/" + m.group(1));
//						}
//					}
//				})
//				.setProperty(Exchange.CHARSET_NAME, constant("GBK"))
//				.recipientList(header("newurl"))
//				.process(new Processor() {
//					@Override
//					public void process(Exchange exchange) throws Exception {
//						String cn = exchange.getProperty(Exchange.CHARSET_NAME,String.class);
//						Message in = exchange.getIn();
//						String inStr = in.getBody(String.class);
//						Matcher m = pattern1.matcher(inStr);
//						if(m.find()){
//							String c = m.group(1);
//							c = c.replaceAll("((&nbsp;)|\\s)+", ",");
//							String[] ccc = c.split("<BR>");
//							StringBuffer sb = new StringBuffer();
//							for(String s : ccc){
//								sb.append(s);
//								sb.append("\n");
//							}
//							in.setBody(sb.toString());
//						}
//					}
//				})
//        		.setHeader(Exchange.FILE_NAME, constant("m3958.html"))
//        		.idempotentConsumer(header(Exchange.FILE_NAME)).messageIdRepositoryRef("messageIdRepository").skipDuplicate(true)
//        		.to("file:target/m3958");
        		
        		//producer template
//                ProducerTemplate pt = getContext().createProducerTemplate();
//                System.out.print(pt);
//                pt.send("direct:start",new Processor() {
//        			@Override
//        			public void process(Exchange exchange) throws Exception {
//        				// do nothing.
//        			}
//        		});

if(true){
    	from("timer://foo3?fixedRate=true&repeatCount=1")
    	.setBody(constant(LIUYAN_RECEIVER))
    	.to("sql: select id,title,xingming,state,shouji,createdAt,publishedAt,updatedAt,email,dianhua,danwei,dizhi,mudi,body,receiver_id from mcontents where receiver_id =# order by id ASC?dataSourceRef=tcmsDS")
    	.split().body()
    	.setHeader(Exchange.FILE_NAME,simple("${body[id]}.json"))
    	.marshal().json(JsonLibrary.Jackson)
    	.to("file:target/m3958/liuyans?doneFileName=${file:name}.done");
    	
    	
        from("file:target/m3958/liuyans?doneFileName=${file:name}.done")
        .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
        .to(DEST_URL + "/migration?action=liuyan&siteid=" + DEST_SITE_ID + "&xjcatid=" + XJ_CAT_ID)
        .process(new Processor() {
    			@Override
    			public void process(Exchange exchange) throws Exception {
    				Message m = exchange.getIn();
    				m.setBody(m.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    			}
        });
}

if(true){    	
        from("timer://foo2?fixedRate=true&repeatCount=1")
        .setBody(constant(OWNER))
        .to("sql:select id from users where email = #?dataSourceRef=tcmsDS")
        .setBody(simple("${body[0][id]}"))
        .to("sql: select id,title,excerpt,body,replyBody,createdAt,publishedAt,updatedAt from contents where author_id = # order by id ASC?dataSourceRef=tcmsDS")
        .split().body()
        .setHeader(Exchange.FILE_NAME,simple("${body[id]}.json"))
        .marshal().json(JsonLibrary.Jackson)
        .to("file:target/m3958/articles?doneFileName=${file:name}.done");
        
        
        from("file:target/m3958/articles?doneFileName=${file:name}.done")
        .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
        .to(DEST_URL + "/migration?action=article&siteid=" + DEST_SITE_ID)
        .process(new Processor() {
    			@Override
    			public void process(Exchange exchange) throws Exception {
    				Message m = exchange.getIn();
    				m.setBody(m.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    			}
        })
//        level	INFO	String	 Logging level to use. Possible values: FATAL, ERROR, WARN, INFO, DEBUG, TRACE, OFF
        .to("log:com.m3958.camel?showAll=true&multiline=true")
        .aggregate(constant(true),new AggregationStrategy() {
			@Override
			public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			        if (oldExchange == null) {
						newExchange.getIn().setBody(1);
						return newExchange;
			        } else {
				        Integer times = oldExchange.getIn().getBody(Integer.class);
				        oldExchange.getIn().setBody(++times);
				        return oldExchange;
			        }
			}
		}).completionTimeout(30000).to("log:com.m3958.camel.aggregate?showAll=true&multiline=true").to("direct:startsection");
        
        
//        from("timer://foo1?fixedRate=true&delay=0&repeatCount=1")
        from("direct:startsection")
        .setBody(constant(OWNER))
        .to("sql:select id from users where email = #?dataSourceRef=tcmsDS")
        .choice()
        	.when(simple("${body.size} > 0"))
        		.setBody(simple("${body[0][id]}"))
        		.to("sql: select id,parentId,name from section_bases where owner_id = #?dataSourceRef=tcmsDS")
        		.split().body()
        		.setHeader(Exchange.FILE_NAME,simple("${body[id]}.json"))
        		.setHeader("articleContent", simple("${body}"))
        		.setBody().simple("${body[id]}")
        		.to("sql: select articles_id from Article_Section where sections_id = #?dataSourceRef=tcmsDS")
        		.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						List<Map<String, Object>> articleids = new ArrayList<Map<String,Object>>();
						articleids = in.getBody(articleids.getClass());
						List<String> ids = new ArrayList<String>();
						for(Map<String, Object> oneid : articleids){
							ids.add((String) oneid.get("articles_id"));
						}
						Map<String, Object> section = in.getHeader("articleContent", Map.class);
						section.put("articleids", ids);
						in.setBody(section);
					}
				})
        		.marshal().json(JsonLibrary.Jackson)
        		.to("file:target/m3958/sections?doneFileName=${file:name}.done");
        
        		
        	from("file:target/m3958/sections?doneFileName=${file:name}.done")
        	.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
            .to(DEST_URL + "/migration?action=section&siteid=" + DEST_SITE_ID)
            .process(new Processor() {
    			@Override
    			public void process(Exchange exchange) throws Exception {
    				Message m = exchange.getIn();
    				System.out.println(m.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    			}
    		});
}        
        
        
//        .process(new Processor() {
//			@Override
//			public void process(Exchange exchange) throws Exception {
//				Message in = exchange.getIn();
//				Map<String, Object> ar = in.getBody(Map.class);
//				in.setBody(mapper.writeValueAsString(ar));
//			}
//		})
        
        
//        .process(new Processor() {
//			@Override
//			public void process(Exchange exchange) throws Exception {
//				Message in = exchange.getIn();
//				List<Map<String, Object>> rows = new ArrayList<Map<String,Object>>();
//				rows = in.getBody(rows.getClass());
//				in.setBody(mapper.writeValueAsString(rows));
//			}
//		})
//        .setHeader(Exchange.FILE_NAME, constant("site_sections.json"))
        
//		from("file://nofile")
//        .split().body()
//        .aggregate(simple("${body[parentId]}"),new ArrayListAggregationStrategy()).completionTimeout(3000)
//        .choice()
//        .when(simple("${body.size} > 0"))
//        .setHeader(Exchange.FILE_NAME, simple("${body[0][parentId]}.html"))
//        .convertBodyTo(String.class)
//        .to("file:target/m3958/sections");
        
        

        
   
        
//      .setHeader("CamelAwsSesFrom", constant("jianglibo@ymail.com"))
//      .setHeader("CamelAwsSesTo", constant("jianglibo@ymail.com"))
//      .setHeader("CamelAwsSesSubject",constant("网站迁移文件"))
//      .to("aws-ses://jianglibo@ymail.com?amazonSESClient=#amazonSESClient");
//      .setHeader(Exchange.FILE_NAME, constant("site_articles.json"))
        
//        
//        
//        from("file:target/m3958/articles?noop=true")
//        .unmarshal().json(JsonLibrary.Jackson)
//        .setBody(simple("${body[sectionids]}"))
//        .split().tokenize(",")
//        .to("sql: select id,parentId,name from section_bases where id = #?dataSourceRef=tcmsDS")
//        .split().body()
//        .setHeader(Exchange.FILE_NAME,simple("${body[id]}.json"))
//        .marshal().json(JsonLibrary.Jackson)
//        .to("file:target/m3958/sections1");
//        .recipientList(simple(""))
        
        
        
        
        
//        from("file:target/m3958/articles")
//      .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
//        .to("http4://127.0.0.1:8888/migration?action=article")
//        .process(new Processor() {
//			@Override
//			public void process(Exchange exchange) throws Exception {
//				Message m = exchange.getIn();
//				System.out.println(m.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
//			}
//		});
        
    }

}
