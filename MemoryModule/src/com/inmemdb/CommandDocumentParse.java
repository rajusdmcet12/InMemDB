package com.inmemdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandDocumentParse implements Parser {

	public List<Object> parseData(BufferedReader in) {

		List<Object> cacheDataList = new ArrayList<>();
		try {
			String line1 = in.readLine();
			if (line1 == null) {
				cacheDataList.add("exit");
				return cacheDataList;
			}
			if (line1.charAt(0) != '*') {
				throw new RuntimeException("Command need to be valid ");
			}
			int inElementValue = Integer.parseInt(line1.substring(1));
			in.readLine();
			cacheDataList.add(in.readLine());
			System.out.println(
					"Command Typed" + cacheDataList.get(0) + ", and passed number of element " + inElementValue);
			// read data
			String line = null;
			for (int i = 1; i < inElementValue && (line = in.readLine()) != null; i++) {
				if (line.isEmpty())
					continue;
				char type = line.charAt(0);
				switch (type) {
				case '$':
					System.out.println("Parsing the line" + line);
					cacheDataList.add(line);
					cacheDataList.add(in.readLine());
					break;
				case ':':
					System.out.println("ParseData" + line);
					cacheDataList.add(String.valueOf(type));
					cacheDataList.add(Integer.parseInt(line.substring(1)));
					break;
				default:
					System.out.println("Parse Default Line: " + line);
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Parse failed " + e.getMessage());
			throw new RuntimeException(e);
		} catch (Exception e) {
			System.out.println("Parse failed " + e.getMessage());
		}
		System.out.println("Command: " + String.join(" ", cacheDataList.stream().toArray(String[]::new)));
		return cacheDataList;

	}

}
