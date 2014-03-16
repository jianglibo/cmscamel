package com.m3958.camel;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class Pjava {

	 public static void main(String[] args) throws Exception{

	        Path path = Paths.get("/home/jianglibo/pjava.txt");
	        FileOwnerAttributeView view = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
	        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
	        UserPrincipal userPrincipal = lookupService.lookupPrincipalByName("tomcat");
	        view.setOwner(userPrincipal);
	        System.out.println("Owner: " + view.getOwner().getName());
	}
	
}
