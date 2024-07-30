package com.inmemdb;

public class ReddisFactory {

	
	
	
	public static Parser getParser(String type) {
		
		if(type=="Command") {
			
			return new CommandDocumentParse();
		}
		
		return null;
		
	}
	
	
}
