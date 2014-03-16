package com.m3958.camel.unused;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

public class MyAmazonSimpleEmailServiceClient extends AmazonSimpleEmailServiceClient{
	
	private AWSCredentials aes;

	public MyAmazonSimpleEmailServiceClient(AWSCredentials awsCredentials) {
		super(awsCredentials);
		ClientConfiguration cc = new ClientConfiguration();
	}

}
