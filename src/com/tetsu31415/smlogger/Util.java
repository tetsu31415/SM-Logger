package com.tetsu31415.smlogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {
	
	private Util() {
		
	}
	
	public static String execCmd(String cmd) {
		Process process;
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd.split(" "));
			builder.redirectErrorStream(true);
			process = builder.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null){
				sb.append(line).append("\n");
			}
			reader.close();
			return sb.toString();
					
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}
}
