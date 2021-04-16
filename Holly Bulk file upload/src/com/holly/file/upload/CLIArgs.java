package com.holly.file.upload;

import com.beust.jcommander.Parameter;

public class CLIArgs {


	@Parameter(
			names = {"--help","-h"},
			help = true,
			description = "Displays help information"
			)
	private boolean help;
	
	@Parameter(
			names = {"--doctype","-dt"},
			description = "Document type -> VAF/W9",
			required = true)
	private String doctype;
	
	@Parameter(
			names = {"--rectype","-rt"},
			description = "Record Type -> HEP/HFC/HFLS",
			required = true)
	private String rectype;
	
	
	@Parameter(
			names = {"--environment","-env"},
			description = "Environment name -> dev/qa/prod",
			required = false)
	private String env = "dev";
	
	@Parameter(
			names = {"--directory","-fd"},
			description = "Directory path where the files to be uploaded are present",
			required = true)
	private String dir;
	
	@Parameter(
			names = {"--username","-u"},
			description = "Username",
			required = true)
	private String username;
	
	@Parameter(
			names = {"--password","-p"},
			description = "Password",
			required = true)
	private String password;
	
	public String getEnv() {
		env = env.toUpperCase();
		if(env.compareTo("PROD")==0) {
			env="https://hollyfrontier-prodint.mdm.informaticahosted.com/cmx/file/orcl-TCR_HUB/TEMP";
		}
		else if(env.compareTo("QA")==0) {
			env="https://hollyfrontier-qaint.mdm.informaticahosted.com/cmx/file/orcl-TCR_HUB/TEMP";
		}
		else if(env.compareTo("DEV")==0) {
			env="https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/file/orcl-TCR_HUB/TEMP";
		}
		else {
			env="Invalid Env";
		}
		
		return env;
	}

	public String getDir() {
		return dir;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDoctype() {
		return doctype;
	}

	public String getRectype() {
		return rectype;
	}
	
	

	public boolean isHelp() {
		return help;
	}

	@Override
	public String toString() {
		return "CLIArgs [doctype=" + doctype + ", rectype=" + rectype + ", env=" + env + ", dir=" + dir + ", username="
				+ username + ", password=" + password + "]";
	}



	


}
