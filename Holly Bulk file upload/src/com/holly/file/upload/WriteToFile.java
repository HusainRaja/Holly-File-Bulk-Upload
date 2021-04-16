package com.holly.file.upload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

	public static void main(String[] args) {
		 try {
			 File directory = new File("C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Documents\\Holly Project\\Project Files\\File Upload\\test\\logs");
			    if (! directory.exists()){
			        directory.mkdir();
			    }

		      FileWriter myWriter = new FileWriter("C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Documents\\Holly Project\\Project Files\\File Upload\\test\\logs\\SkippedFiles.txt");
		      myWriter.write("Files in Java might be tricky, but it is fun enough! Husain Raja");
		      myWriter.write("\nFiles in Java might be tricky, but it is fun enough! Husain Raja 2");
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		 String str="Husain Raja";
		 String[] parts = str.split(" ");
		 System.out.println(parts.length);
		 if(parts==null) {
			 System.out.println("Parts is null");
		 }

	}

}
