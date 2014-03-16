package com.m3958.camel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;



public class ClonePage{
	
	public static class ClonePageTaskDescription{
		private String url;
		private String path;
		private String fn;
		private int siteId;
		
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public String getFn() {
			return fn;
		}
		public void setFn(String fn) {
			this.fn = fn;
		}
		public int getSiteId() {
			return siteId;
		}
		public void setSiteId(int siteId) {
			this.siteId = siteId;
		}
	}
	
	
	private static Joiner joiner = Joiner.on("/").skipNulls();
	
	
	public void p1(Exchange exchange){
		Message in = exchange.getIn();
		ClonePageTaskDescription data = in.getBody(ClonePageTaskDescription.class);
		String url = data.getUrl();
		String siteThemeRoot = data.getPath();
		String ofn = (String) data.getFn();

		String[] hu = ClonePage.getHostnameUri(url);
		
		String hostname = hu[0];
		String uri = hu[1];
		
		final String pageUrl = hostname + uri;
		Message out = exchange.getOut();
		
		out.setHeader("clonepage-hostname", hostname);
		out.setHeader("clonepage-uri", uri);
		out.setHeader("clonepage-siteroot", siteThemeRoot);
		String fn;
		if(ofn.isEmpty()){
			fn = ClonePage.guessMainFileName(uri);
		}else{
			if(ofn.endsWith(".ftl")){
				fn = ofn;
			}else{
				fn = ofn + ".ftl";
			}
		}
		out.setHeader("clonepage-filename", fn);
		out.setHeader("savecontinue", new String[]{"file://" + siteThemeRoot,"direct:htmlpage"});
		out.setHeader(Exchange.HTTP_URI, "http4://" + pageUrl);
	}
	
	public void p2(Exchange exchange) throws IOException{
		Message in = exchange.getIn();
		String html = in.getBody(String.class);
		String hostname = in.getHeader("clonepage-hostname",String.class);
		String uri = in.getHeader("clonepage-uri",String.class);
		org.jsoup.nodes.Document doc = Jsoup.parse(html);
		Elements eles = doc.getElementsByTag("base");
		String basePath = null;
		if(eles.size() > 0){
			String baseHref = Strings.emptyToNull(eles.get(0).attr("href"));
			if(baseHref != null){
				basePath = ClonePage.getHostnameUri(baseHref)[1];
				if(!basePath.endsWith("/"))basePath = basePath + "/";
			}
		}
		
		for(Element e : eles){
			e.remove();
		}
		
		List<String[]> relations = new ArrayList<String[]>();//['img','http://www.51dir.com/abc.jpg','e:/siteroot/5/default/images/abc.jpg']
		Elements imgs = doc.getElementsByTag("img");
		Elements csses = doc.getElementsByTag("link");
		Elements scripts = doc.getElementsByTag("script");
		
		Elements embedes = doc.getElementsByTag("embed");
		
		Elements parames = doc.getElementsByTag("param");
		 
		
		for(Element css:csses){
			String type = css.attr("type");
			if("text/css".equalsIgnoreCase(type)){
				String src = css.attr("href");
				if(src != null && !src.isEmpty()){
					String[] item = ClonePage.getOneItem(src, "css", basePath, hostname, uri); 
					relations.add(item);
					css.attr("href", item[2]);
				}
			}
		}

		for(Element script:scripts){
			String src = script.attr("src");
			if(src != null && !src.trim().isEmpty()){
				String[] item = ClonePage.getOneItem(src, "script", basePath, hostname, uri); 
				relations.add(item);
				script.attr("src", item[2]);
			}
		}
		
		for(Element img : imgs){
			String src = img.attr("src");
			if(src != null && !src.trim().isEmpty()){
				String[] item = ClonePage.getOneItem(src, "img", basePath, hostname, uri); 
				relations.add(item);
				img.attr("src", item[2]);
			}
		}
		
		for(Element embed : embedes){
			String src = embed.attr("src");
			if(src != null && !src.trim().isEmpty()){
				String[] item = ClonePage.getOneItem(src, "img", basePath, hostname, uri); 
				relations.add(item);
				embed.attr("src", item[2]);
			}
		}
		
		for(Element param : parames){
			if(param.hasAttr("name") && param.attr("name").equalsIgnoreCase("movie")){
				String src = param.attr("value");
				if(src != null && !src.trim().isEmpty()){
					String[] item = ClonePage.getOneItem(src, "img", basePath, hostname, uri); 
					relations.add(item);
					param.attr("value", item[2]);
				}
			}
		}
		
		ClonePage.writeTextFile(in, doc.outerHtml());
		Message out = exchange.getOut();
		out.setHeaders(in.getHeaders());
		out.setBody(relations);
	}
	
	
	
	public static String[] getOneItem(String src,String type,String basePath,String hostname,String uri){
		String[] relation = new String[3];
		relation[0] = type;
		if(src == null){
			;
		}else if(src.startsWith("http://")){
			String[] hu = getHostnameUri(src);
			relation[1] = hu[0] + hu[1];
			relation[2] = hu[1];
		}else if(src.startsWith("/")){
			relation[1] = hostname + src;
			relation[2] = src;
		}else if(src.startsWith("../")){//相对于当前uri
			if(basePath == null){
				String s = getRelativeUri(uri, src);
				relation[1] = hostname + s;
				relation[2] = s;
			}else{
				;
			}
		}else{
			if(basePath == null){//相对于当前请求的url定位。
				;
			}else{
				String s = basePath + src;
				relation[1] = hostname + s;
				relation[2] = s;
			}
		}
		
		if(relation[2] != null){
			int qmi = relation[2].indexOf("?"); 
			if(qmi != -1){
				relation[2] = relation[2].substring(0,qmi);
			}
		}
		return  relation;
	}
	
	public static String getRelativeUri(String uri,String ruri){
		
		// /a => ['','a']去掉最后一个
		String[] uris = uri.split("/");
		int uriSeg = uris.length;
		if(uriSeg < 2)uriSeg = 2;
//		if(uris.length == 0){ //单独一个/
//			
//		}else if(uris.length == 1){ //不会出现这种可能
//			;
//		}else{
//			
//		}
		//case 1: /a/b.txt ../c/c.txt
		int i = 0;
		while(ruri.startsWith("../")){
			ruri= ruri.substring(3);
			i++;
		}
		
		int j = uriSeg - i - 1;
		
		if(j < 2)return "/" + ruri;
		
		String[] result = new String[j + 1];
		for(int ii=0;ii < j;ii++){
			result[ii] = uris[ii]; 
		}
		result[j] = ruri;
		return joiner.join(result);
	}
	
	
	public static String[] getHostnameUri(String url){
		int i = url.indexOf("//");
		if(i != -1){
			url = url.substring(i+2);
		}
		
		i = url.indexOf("?");
		if(i != -1){
			url = url.substring(0, i);
		}
		
		String[] ss = new String[2];
		int i1 = url.indexOf("/");//-- www.m3958.com/section/1
		
		if(i1 == -1){
			ss[0] = url;
			ss[1] = "/";
		}else{
			ss[0] = url.substring(0, i1);
			ss[1] = url.substring(i1);
		}
		return ss;
	}
	
	
	public static void  writeBinaryFile(Message in) throws IOException{
		final InputStream is = in.getBody(InputStream.class);
		String root = in.getHeader("clonepage-siteroot", String.class);
		String rp =  in.getHeader("clonepage-filename", String.class);
		File dstfile = new File(root,rp);
		Files.createParentDirs(dstfile);
		Files.copy(new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return is;
			}
		}, dstfile);
	}
	
	public static void  writeTextFile(Message in,String body) throws IOException{
		String root = in.getHeader("clonepage-siteroot", String.class);
		String rp =  in.getHeader("clonepage-filename", String.class);
		File dstfile = new File(root,rp);
		Files.createParentDirs(dstfile);
		Files.write(body, dstfile, Charsets.UTF_8);
	}
	
	
	public static String guessMainFileName(String uri){//-- /section/1
		if("/".equals(uri)){
			return "index.ftl";
		}
		
		String[] ss = uri.split("/");
		String fn = null;
		int i = ss.length - 1;
		for(;i>-1;i--){
			if(ss[i].trim().length() > 0){
				fn = ss[i];
				break;
			}
		}
		if(fn != null){
			int ix = fn.lastIndexOf(".");
			if(ix == -1){
				fn = fn + ".ftl";
			}else{
				fn = fn.substring(0, ix) + ".ftl";
			}
		}else{
			fn = "index.ftl";
		}
		return fn;
	}
}
