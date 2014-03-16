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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.Main;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.m3958.camel.util.Osdetecter;

/**
 * A Camel Router
 */
public class NewTtBuilder extends RouteBuilder {
	
	
	public static int rebuildSolrBatchNumber = 100;
	
	public static M3958CamelCfg ccfg = new M3958CamelCfg();
	
	public static class Website{
		private String imgSizes;

		public String getImgSizes() {
			return imgSizes;
		}

		public void setImgSizes(String imgSizes) {
			this.imgSizes = imgSizes;
		}
	}
	
	public static class Asset{
		
		private long id;
		
		private String relativeUrl;
		
		private String directUrl;
		
		private Website website;
		
		public Asset(){
			
		}
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}

		public String getDirectUrl() {
			return directUrl;
		}
		public void setDirectUrl(String directUrl) {
			this.directUrl = directUrl;
		}
		public Website getWebsite() {
			return website;
		}
		public void setWebsite(Website website) {
			this.website = website;
		}
		public String getRelativeUrl() {
			return relativeUrl;
		}
		public void setRelativeUrl(String relativeUrl) {
			this.relativeUrl = relativeUrl;
		}

		public static String translatePath(String fn,String size){//pure name
			String fn1 = new File(fn).getName();
			String ssize;
			if(size == null || size.isEmpty()){
				ssize = "";
			}else{
				ssize = size + ".";
			}
			String ext = Files.getFileExtension(fn1);
			String barefn = new File(fn1).getName();
			String sid = barefn.substring(0,barefn.length() - ext.length() - 1);
			StringBuilder sb = new StringBuilder();
			int length = sid.length() < 4 ? sid.length() : 4;
			for(int i=0;i<length;i++){
				sb.append(sid.charAt(i)).append(File.separatorChar);
			}
			sb.append(sid).append(".").append(ssize).append(ext);
			return sb.toString();
		}

	}
	
    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main.main(args);
    }
    
    public static Gson gson = new GsonBuilder().create();
    
    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
    	
    	errorHandler(deadLetterChannel("log:com.m3958.camel?showException=true&multiline=true").redeliveryDelay(3).maximumRedeliveries(3).logStackTrace(true));
    	
    	boolean productionMode = true;
    	
    	if(Osdetecter.isWindows()){
    		productionMode = false;
    	}

    	if(productionMode){//solr updater
        	from(ccfg.getSolrXmlDir()).setHeader(Exchange.HTTP_QUERY, constant("commit=true")).
    			setHeader(Exchange.CONTENT_TYPE, constant("Conntent-Type: text/xml")).
    			setHeader(Exchange.HTTP_CHARACTER_ENCODING, constant("UTF-8")).
    			setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST)).to(ccfg.getSolrUrl());
    	}
    	
    	
        	from(ccfg.getVirtualAssetDir()).unmarshal().json(JsonLibrary.Gson,Asset.class).process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					Message in = exchange.getIn();
					Asset a = in.getBody(Asset.class);
					System.out.println(a.getWebsite().getImgSizes());
					exchange.getOut().setHeader("asset", a);
					exchange.getOut().setHeader(Exchange.HTTP_URI, a.getDirectUrl());
				}
			}).to("http4://somewhere").process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					Message in = exchange.getIn();
					Asset a = in.getHeader("asset",Asset.class);
					System.out.print(a.getRelativeUrl());
					final InputStream is = in.getBody(InputStream.class);
					File af = new File(ccfg.getAssetRoot(),a.getRelativeUrl());
					Files.createParentDirs(af);
					Files.copy(new InputSupplier<InputStream>() {
						@Override
						public InputStream getInput() throws IOException {
							return is;
						}
					}, af);
					
			        
			        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
			        UserPrincipal userPrincipal = lookupService.lookupPrincipalByName("tomcat");
			        

					Path assetRootPath = Paths.get(ccfg.getAssetRoot()).normalize();
					Path afp = af.toPath();
					
					while(afp.startsWith(assetRootPath)){
						FileOwnerAttributeView view = java.nio.file.Files.getFileAttributeView(afp, FileOwnerAttributeView.class);
						if(!"tomcat".equals(view.getOwner().getName())){
							view.setOwner(userPrincipal);
						}
						afp = afp.getParent();
					}
					
					exchange.getOut().setHeader("asset", a);
					exchange.getOut().setHeader(Exchange.HTTP_URI, ccfg.getApphostname() + "/fileupload?id=" + a.getId());
				}
			}).to("http4://somewhere").process(new Processor() {
				@Override
				public void process(Exchange exchange) throws Exception {
					Message in = exchange.getIn();
					Asset a = in.getHeader("asset",Asset.class);
					if(!ccfg.isImageExt(a.getDirectUrl())){
						return;
					}
					
	    	    	String[] imgSizes = a.getWebsite().getImgSizes().split(",");
	    	    	int i = 0;
	    	    	for(String is : imgSizes){
	    	    		i++;
	    	    		StringBuilder sb = new StringBuilder(a.getId()+",").append(is).append(",").append(Files.getFileExtension(a.getRelativeUrl()));
	    	    		Files.write(sb, new File(ccfg.getSizeLogDir(),a.getId() + "-" + i + ".txt"), Charsets.UTF_8);
	    	    	}
				}
			});
//    	}
        	

    	from(ccfg.getSizeLog()).beanRef("oneSizelogProcessor", "p1").to("exec:///usr/bin/convert");
    	
    	from(ccfg.getMissingAssetLog()).beanRef("missingAssetProcessor", "p1").to(ccfg.getSizeLog());
    	
    	/*名字必须有一种约定，不可以重叠*/
    	from(ccfg.getCamelTaskDir() + "include=rebuid_site_solr_index_.*").process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message min = exchange.getIn();
				String str = min.getBody(String.class);
				String[] strs = str.split(",");
				Long articleNumbers = Long.valueOf(strs[1]);
				StringBuilder sb = new StringBuilder();
				long l = 0;
				for(;l<articleNumbers;){
					sb.append(l).append(",");
					l += rebuildSolrBatchNumber;
				}
				exchange.getOut().setHeader("siteId", strs[0]);
				exchange.getOut().setBody(sb.toString(), String.class);
			}
		}).split(body(String.class).tokenize(",")).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message min = exchange.getIn();
				String siteId = min.getHeader("siteId",String.class);
				Long ars = min.getBody(Long.class);
				StringBuilder sb = new StringBuilder();
				sb.append(ccfg.getApphostname()).append("/domain/website?start=").append(ars).
					append("&siteId=").append(siteId).
					append("&number=").append(rebuildSolrBatchNumber).
					append("&dowhat=").append("rebuidsolrindex").
					append("&_isxhr=true");
				System.out.println(sb.toString());
				exchange.getOut().setHeader(Exchange.HTTP_URI, sb.toString());
			}
		}).to("http4://somewhere");

    }

}
