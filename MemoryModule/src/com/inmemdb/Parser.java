package com.inmemdb;

import java.io.BufferedReader;
import java.util.List;

public interface Parser {
   
	public List<Object> parseData(BufferedReader in);
}
