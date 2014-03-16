package com.m3958.camel;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.io.Files;
import com.m3958.camel.util.Osdetecter;

public class M3958CamelCfg {
	
	public static String[] IMG_EXTS = new String[]{"JPG","PNG","GIF","JPEG","BMP"}; 
	
//	private  String solrXmlDir = "file:///opt/camelworld/fromdir/solrcameldir?delete=true&delay=5000";
	private  String solrXmlDir = "file:///opt/camelworld/fromdir/solrcameldir?delay=5000";
	private  String solrUrl = "http4://localhost:8080/solr-numberone/update";
	private  String sizeLog = "file:///opt/camelworld/fromdir/sizelog";
	private  String sizeLogDir = "/opt/camelworld/fromdir/sizelog";
	private  String virtualAssetDir = "file:///opt/camelworld/fromdir/virtualassets";
	private  String apphostname = "http://sb.m3958.com";
	private  String assetRoot = "/opt/ar";
	private  String convertExec = "/usr/bin/convert";
	
	private  String camelTaskDir = "file:///opt/camelworld/fromdir/cameltasks?delay=5000&";
	
	private  String missingAssetLog = "file:///opt/camelworld/fromdir/missingasset/?delete=true";
	
	
	
	public M3958CamelCfg(){
		String hostname = null;
        try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			System.out.print("cant resolve host name!!!!!!!!!!!!!!!!!!!");
		}
        if(hostname.indexOf("sites.fh.gov.cn") != -1){
        	apphostname = "http://sites.fh.gov.cn";
        }
		if(Osdetecter.isWindows()){
			solrXmlDir = "file://e:/solrcameldir";
			solrUrl = "http4://localhost:8080/solr-numberone/update";
			sizeLog = "file://e:/sizelog";
			sizeLogDir = "e:/sizelog";
			virtualAssetDir = "file://e:/abc/virtualassets";
			apphostname = "http://localhost";
			assetRoot = "e:/siteroots/new";
			convertExec = "C:/Program Files/ImageMagick-6.6.7-Q16/convert.exe";
			missingAssetLog = "file:///e:/assetroot/missingasset/?delete=true";
			camelTaskDir = "file://e:/cameltaskfile?";
		}
	}
	
	public boolean isImageExt(String fname){
		String ext = Files.getFileExtension(fname);
		if(ext.isEmpty())return false;
		for(String s:IMG_EXTS){
			if(s.equalsIgnoreCase(ext)){
				return true;
			}
		}
		return false;
	}

	public String getSolrXmlDir() {
		return solrXmlDir;
	}

	public void setSolrXmlDir(String solrXmlDir) {
		this.solrXmlDir = solrXmlDir;
	}

	public String getSolrUrl() {
		return solrUrl;
	}

	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	public String getSizeLog() {
		return sizeLog;
	}

	public void setSizeLog(String sizeLog) {
		this.sizeLog = sizeLog;
	}

	public String getSizeLogDir() {
		return sizeLogDir;
	}

	public void setSizeLogDir(String sizeLogDir) {
		this.sizeLogDir = sizeLogDir;
	}

	public String getVirtualAssetDir() {
		return virtualAssetDir;
	}

	public void setVirtualAssetDir(String virtualAssetDir) {
		this.virtualAssetDir = virtualAssetDir;
	}

	public String getApphostname() {
		return apphostname;
	}

	public void setApphostname(String apphostname) {
		this.apphostname = apphostname;
	}

	public String getAssetRoot() {
		return assetRoot;
	}

	public void setAssetRoot(String assetRoot) {
		this.assetRoot = assetRoot;
	}

	public String getConvertExec() {
		return convertExec;
	}

	public void setConvertExec(String convertExec) {
		this.convertExec = convertExec;
	}

	public String getCamelTaskDir() {
		return camelTaskDir;
	}

	public void setCamelTaskDir(String camelTaskDir) {
		this.camelTaskDir = camelTaskDir;
	}

	public String getMissingAssetLog() {
		return missingAssetLog;
	}

	public void setMissingAssetLog(String missingAssetLog) {
		this.missingAssetLog = missingAssetLog;
	}
}
