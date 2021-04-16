package com.holly.file.upload;

import java.io.File;
import java.util.Scanner;

public class FileDocTypeExtractor {
	public static void main(String args[]) {
		Scanner s = new Scanner(System.in);
		System.out.println("Enter the dir path for the files: ");
		String dirPath = s.nextLine();
		File directoryPath = new File(dirPath);
		File filesList[] = directoryPath.listFiles();
		String fileName = null;
		for (File file : filesList) {
			fileName = file.getName();
			System.out.println("The Filename is: " + fileName);
			System.out.println("The docType is: " + getDocType(fileName));
			System.out.println();
		}
	}

	private static String getDocType(String fileName) {
		String[] split1 = fileName.split("\\.");
	//	System.out.println(split1.length);
	//	System.out.println(split1[split1.length-2]);
		if(split1.length<2) {
			return "No Doc Type Found";
		}
		String[] split2 = split1[split1.length-2].split(" ");
	//	System.out.println(split2[split2.length-1]);
		
		String docType = split2[split2.length-1];
		if(docType.compareTo("W9")==0||docType.compareTo("VAF")==0||docType.compareTo("W8")==0) {
			return docType;
			
		}
		else {
			int indexW9 = fileName.lastIndexOf("W9");
			int indexW8 = fileName.lastIndexOf("W8");
			int indexVAF = fileName.lastIndexOf("VAF");
			if(indexVAF==-1&&indexW8==-1&&indexW9==-1) {
				return "No Doc Type Found";
			}
			else {
				if(indexVAF>indexW8&&indexVAF>indexW9) {
					return "VAF";
				}
				else if(indexW8>indexVAF&&indexW8>indexW9) {
					return "W8";
				}
				else {
					return "W9";
				}
			}			
		}
	}

}
