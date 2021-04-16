package com.holly.file.upload;

import com.beust.jcommander.JCommander;

public class CLIArgsImplementor {

	public static void main(String[] args) {
		CLIArgs jArgs = new CLIArgs();
		JCommander helloCmd = JCommander.newBuilder()
		  .addObject(jArgs)
		  .build();
		helloCmd.parse(args);
		if(jArgs.isHelp()){
			helloCmd.usage();
		}
		
		else {
			System.out.println("URL: "+jArgs.getEnv());
		}

	}

}
