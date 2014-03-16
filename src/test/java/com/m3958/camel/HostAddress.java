package com.m3958.camel;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostAddress {

	
	   public static void main(String args[])
	    {
	        try
	        {
	            InetAddress addr = InetAddress.getLocalHost();
	            String hostname = addr.getHostName();
	            System.out.println(addr.getHostAddress());
	            System.out.println(hostname);
	        }catch(UnknownHostException e)
	        {
	             //throw Exception
	        }


	    }
//	javac testpackage/Test.java
//
//	java -cp . testpackage.Test
	   
}
