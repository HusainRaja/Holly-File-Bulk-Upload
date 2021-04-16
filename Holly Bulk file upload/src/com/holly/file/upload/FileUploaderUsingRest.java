package com.holly.file.upload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.siperian.sif.client.SiperianClient;
import com.siperian.sif.client.SoapSiperianClient;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Parameter;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.mrm.GetRequest;
import com.siperian.sif.message.mrm.GetResponse;
import com.siperian.sif.message.mrm.PromotePendingXrefsRequest;
import com.siperian.sif.message.mrm.PromotePendingXrefsResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileUploaderUsingRest {
	
	static SoapSiperianClient sipClient = null;
	static OkHttpClient client = new OkHttpClient().newBuilder().build();
	static String cookie = null;
	static FileWriter fileWriter = null;
	
	public static void main(String args[]) throws IOException {
	      String rowid = null;
	      String SAPId = null;
	      String fileName = null;
	      Scanner s = new Scanner(System.in);
	      System.out.println("Enter the dir path for the files: ");
	      String dirPath = s.nextLine();
	      System.out.println("path is:" + dirPath);
	      File directory = new File(dirPath+"//logs");
		    if (! directory.exists()){
		        directory.mkdir();
		    }
	      fileWriter = new FileWriter(dirPath+"//logs//SkippedFiles.txt");
	      
	     // System.out.println("Enter doc type, Either 'VAF' or 'W9': ");
	     // String docType = s.nextLine();
	      System.out.println("Enter the record type, Either 'HFC', 'HEP' or 'HFLS' : ");
	      String recType = s.nextLine();
	      recType = recType.toUpperCase();
	      //docType = docType.toUpperCase();
	      //dirPath = "C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Documents\\Holly Project\\Project Files\\File Upload\\Customer_Files\\HFC";
	      File directoryPath = new File(dirPath);
	      
	      //Getting the list of files
	      
	      File filesList[] = directoryPath.listFiles();
	      
	      //Initializing the Siperian Client for the SOAP calls
	      
	      initializeSipClient();
	      
	      //Iterating for every file
	      
	      
	      for(File file : filesList) {
	    	try { 
	    	  fileName = file.getName();
	    	  System.out.println("\n\n****** Uploading the File : "+fileName+" ******\n\n");
	    	  if(fileName.compareTo("logs")==0) {
	    		  System.out.println("Breaking from loop");
	    		  continue;
	    	  }
	    	  //System.out.println("\n\n****** Uploading the File : "+fileName+" ******\n\n");
	         
	         //Fetching the SAP ID from the file name
	         
	         SAPId = getSAPId(fileName);
	         System.out.println("The SAP ID is: " + SAPId);
	         
	         //Fetching Doc type from the file name
	         
	         String docType = getDocType(fileName);
	         if(docType.compareTo("Invalid doc type")==0) {
	        	 System.out.println("Invalid doc type, ignoring this file");
	        	 writeToFile(fileName+" - Invalid Doc Type");
	        	 continue;
	         }
	         else if(docType.compareTo("Invalid file format, no space before Doc Type")==0) {
	        	 System.out.println("Invalid file format, no space before Doc Type");
	        	 writeToFile("Invalid file format, no space before Doc Type");
	        	 continue;
	         }
	         
	         //Fetching the Party rowid using the SAP ID
	         
	         rowid = getRowid(SAPId, recType);
	         System.out.println("Rowid is: " + rowid);
	         if(rowid == null) {
	        	 System.out.println("Party FK not found, not processing this file.");
	        	 writeToFile(fileName+" - Party FK Not Found");
	        	 continue;
	         }
	         else if(rowid.compareTo("Invalid Record Type")==0) {
	        	 writeToFile(recType+" - Invalid Record Type");
	        	 break;
	         }
	         else if(rowid.compareTo("Multiple FKs")==0) {
	        	 System.out.println("SAP ID is not unique, skipping this record");
	        	 writeToFile(fileName+" - SAP ID Not Unique");
	        	 continue;
	         }
	         
	        //Creating the file Metadata and uploading the file
	         
	        String tempid = createFileMetadata(fileName);
	 		if(!uploadFile(tempid,file)) {
	 			System.out.println("File upload failed, skipping this file");
	 			writeToFile(fileName+" - File upload failed");
	 			continue;
	 		}
	 		else if(tempid.compareTo("unsupported File Extension/Type")==0) {
	 			System.out.println("Unsupported File Extension/Type, skipping this file");
	 			writeToFile(fileName+" - Unsupported File Extension/Type");
	 			continue;
	 		}
	 		
	 		//Adding the file attachment to the BE record
	 		
	 		String strResponse = addFileToRecord(tempid,rowid,docType);
	 		if(strResponse == null) {
	 			System.out.println("Breaking out of the loop as the doc type entered is invalid");
	 			writeToFile(fileName+" - Invalid Doc Type");
	 			continue;
	 		}
	 		
	 		//Extracting the rowid of the doc from the response of the BES call to add the attachement to the record
	        
	 		String docRowid = extractDocRowid(strResponse);
	 		
	 		//Getting the Interaction from the document record for performing the promote call
	 		
	 		String interactionId = getInteractionID(docRowid);
	 		
	 		//Promoting the doc record that was created
	 		
	 		promoteDoc(docRowid,interactionId);
	 		
	        System.out.println("\n\n****** Successfully uploaded the File : "+file.getName()+" ******\n\n");
	         
	      }
	    	catch (Exception e) {
				System.out.println(e);
				writeToFile("Exiting loop");
				writeToFile(e.toString());
			}
	      }
	      
	      System.out.println("out of the loop");
	      
	      fileWriter.close();
	      System.out.println("Closed the filewriter");
	      }
	      
	     
	   

	private static void writeToFile(String string) throws IOException {
		System.out.println("String = "+string);
		fileWriter.write("\n"+string);
		
	}

	/*private static String getDocType(String fileName) {
		
		String[] parts = fileName.split(" ");
		if(parts.length==1) {
			return "Invalid file format, no space before Doc Type";
		}
		String[] parts1 = parts[1].split("\\.");
		System.out.println(parts1[0]);
		if((parts1[0].compareTo("W9")+parts1[0].compareTo("W9")+parts1[0].compareTo("W9"))>0) {
			return "Invalid doc type";
		}
		else {
			System.out.println("The doc type is: "+parts1[0]);
			return parts1[0];
		}
		}*/
	
	private static String getDocType(String fileName) {
		String[] split1 = fileName.split("\\.");
	//	System.out.println(split1.length);
	//	System.out.println(split1[split1.length-2]);
		if(split1.length<2) {
			return "Invalid doc type";
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
				return "Invalid doc type";
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
		
		
		
	

	private static void promoteDoc(String docRowid, String interactionId) {
		
		PromotePendingXrefsRequest request = new PromotePendingXrefsRequest();
        request.setInteractionId(interactionId);
        request.setFlagForPromote(false);
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_DOCS");
        RecordKey recKey = new RecordKey();
        recKey.setRowid(docRowid);
        request.setRecordKey(recKey);
        PromotePendingXrefsResponse response = (PromotePendingXrefsResponse) sipClient.process(request);
        System.out.println(response.getMessage());
		
	}

	private static String getInteractionID(String docRowid) {
		GetRequest request = new GetRequest();
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_DOCS");
        RecordKey recKey = new RecordKey();
        recKey.setRowid(docRowid);
        request.setRecordKey(recKey);
        GetResponse response = (GetResponse) sipClient.process(request);
        ArrayList<Record> recList = response.getRecords();
        Record record = recList.get(0);
        System.out.println("Interaction ID = "+record.getField("INTERACTION_ID").getBigIntegerValue());
        System.out.println("The Hub State Ind = "+record.getField("HUB_STATE_IND").getBigIntegerValue());
        
        return record.getField("INTERACTION_ID").getBigIntegerValue().toString();

	}

	private static String extractDocRowid(String strResponse) {
		System.out.println("Extracting the rowid");
		String item = StringUtils.substringBetween(strResponse, "<ns6:item>", "<ns6:item>");
		String rowid = StringUtils.substringBetween(item, "<ns1:rowid>", "</ns1:rowid>");
		System.out.println("\n\n The rowid of the Doc is: "+rowid);
		return rowid;		
	}

	private static String addFileToRecord(String tempid, String rowid, String docType) throws IOException {
		System.out.println("adding file to the record");
		MediaType mediaType = MediaType.parse("application/json");
		String docTypDesc = getDocTypDesc(docType);
		//RequestBody body = RequestBody.create(mediaType, "{\r\n    \"ExDocuments\": {\r\n        \"item\": [\r\n           \r\n            {\r\n                \"ExFileName\": \""+tempid+"\",\r\n                \"ExDisclaimer\": \"2\"\r\n            }\r\n        ]\r\n    }\r\n}");
		RequestBody body = RequestBody.create(mediaType, "{\r\n    \"ExDocuments\": {\r\n        \"item\": [\r\n           \r\n            {\r\n                \"ExFileName\": \""+tempid+"\",\r\n                \"ExDisclaimer\": \"2\",\r\n                \"ExDocType\": {\r\n                    \"docTypCd\": \""+docType+"\",\r\n                    \"docTypDesc\": \""+docTypDesc+"\"\r\n                }\r\n            }\r\n        ]\r\n    }\r\n}");
		Request request = new Request.Builder()
		  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/cs/orcl-TCR_HUB/ExHFC_HEP_CustomerOrg/"+rowid+"?systemName=Admin")
		  .method("POST", body)
		  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
		  .addHeader("Content-Type", "application/json")
		  .addHeader("Cookie", cookie)
		  .build();
		Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();
		String strResponse = responseBody.string();
		if(docTypDesc == null) {
			System.out.println("Incorrect Doc type entered");
			strResponse=null;
		}
		System.out.println("response: "+ strResponse);
		responseBody.close();
		return strResponse;
	}

	private static String getDocTypDesc(String docType) {
		if(docType.compareTo("VAF")==0) {
			return "VAF";
		}
		else if(docType.compareTo("W9")==0) {
			return "W9 FEIN";
		}
		else if(docType.compareTo("W8")==0) {
			return "W8 FEIN";
		}
		else {
			return null;
		}
	}

	private static String createFileMetadata(String fileName) throws IOException {
		MediaType mediaType = MediaType.parse("application/json");
		String fileType, fileContentType;
		fileType = getFileType(getFileExtension(fileName));
		if(fileType==null) {
			return "unsupported File Extension/Type";
		}
		fileContentType = getFileContentType(getFileExtension(fileName));
		System.out.println("file type = "+fileType);
		System.out.println("File content= "+fileContentType);
		RequestBody body = RequestBody.create(mediaType, "{\r\n    \"fileName\": \""+fileName+"\",\r\n    \"fileType\": \""+fileType+"\",\r\n    \"fileContentType\": \""+fileContentType+"\"\r\n}");
		Request request = new Request.Builder()
		  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/file/orcl-TCR_HUB/TEMP")
		  .method("POST", body)
		  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
		  .addHeader("Content-Type", "application/json")
		  .build();
		Response response = client.newCall(request).execute();
		ResponseBody responseBody = response.body();
		Headers headers = response.headers();
		
		
		String[] parts = headers.value(7).split(";");
		String JsessionCookie = parts[0];
		parts = headers.value(4).split(";");
		String AWSALB = parts[0];
		parts = headers.value(5).split(";");
		String AWSALBCORS = parts[0];
		cookie = JsessionCookie + "; " + AWSALB + "; " + AWSALBCORS;
		
		String tempId = responseBody.string();
		System.out.println("TempID = " + tempId);
		System.out.println("Cookie value from the metadata Method :" + cookie);
		System.out.println("response:"+response.message());
		System.out.println("response:"+response.body());
		responseBody.close();
		
		
		return tempId;
		
	}

	private static String getFileContentType(String fileExtension) {
		if (fileExtension.compareTo("pdf")==0) {
			return "application/pdf";
		}
		else if(fileExtension.compareTo("txt")==0) {
			return "text/plain";
		}
		
		else if((fileExtension.compareTo("jpg")==0) || (fileExtension.compareTo("jpeg")==0)) {
			return "image/jpeg";
		}
		else if(fileExtension.compareTo("msg")==0) {
			return "application/vnd.ms-outlook";
		}
		else if(fileExtension.compareTo("png")==0) {
			return "image/png";
		}
		else if(fileExtension.compareTo("doc")==0) {
			return "application/msword";
		}
		else if((fileExtension.compareTo("tif")==0) || (fileExtension.compareTo("tiff")==0)) {
			return "image/tiff";
		}
		else if(fileExtension.compareTo("bmp")==0) {
			return "image/bmp";
		}
		
		else {
			return null;
		}
		
	}

	private static String getFileType(String fileExtension) {
		if (fileExtension.compareTo("pdf")==0) {
			return "pdf";
		}
		
		else if(fileExtension.compareTo("txt")==0) {
			return "text";
		}
		else if((fileExtension.compareTo("jpg")==0) || (fileExtension.compareTo("jpeg")==0)) {
			return "jpeg";
		}
		else if(fileExtension.compareTo("msg")==0) {
			return "msg";
		}
		else if(fileExtension.compareTo("png")==0) {
			return "png";
		}
		else if(fileExtension.compareTo("doc")==0) {
			return "doc";
		}
		else if((fileExtension.compareTo("tif")==0) || (fileExtension.compareTo("tiff")==0)) {
			return "tif";
		}
		else if(fileExtension.compareTo("bmp")==0) {
			return "bmp";
		}
		
		else {
			
			return null;
		}
		
	}
	private static void initializeSipClient() {
		Properties properties = new Properties();
		properties.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, "soap");
		properties.put("siperian-client.orsId", "orcl-TCR_HUB");
		properties.put("siperian-client.username", "admin");
		properties.put("siperian-client.password", "hollyfront@4a4a");
		properties.put("soap.call.url", "https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/services/SifService");
        
        sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(properties);
		
	}

	private static boolean uploadFile(String tempid, File file) throws IOException {
		
				MediaType mediaType = MediaType.parse("application/octet-stream");
				RequestBody body = RequestBody.create(mediaType, file);
				System.out.println(tempid);
				Request request = new Request.Builder()
						  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/file/orcl-TCR_HUB/TEMP/"+tempid+"/content")
						  .method("PUT", body)
						  .addHeader("Content-Type", "application/octet-stream")
						  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
						  .addHeader("Cookie", cookie)
						  .build();
						Response response = client.newCall(request).execute();
				System.out.println("response of upload file method: "+response.message());
				System.out.println("response body: "+response.body());
				
				/*
				 * if (response.message().compareTo("No Content") != 0) { return false; } else {
				 * return true; }
				 */
				
		return true;
	}

	/*private static String getRowid(String sapId, String recType) {
		String rowid = null;
		SearchQueryRequest request = new SearchQueryRequest();
        request.setRecordsToReturn(20);
        request.setSiperianObjectUid("PACKAGE.X_PACKAGE_FILE_UPLOAD");
        if(recType.compareTo("HEP")==0) {
        	request.setFilterCriteria("X_SAP_ID_1=? and PARTY_FK IS NOT NULL");
        }
        else if((recType.compareTo("HFC")==0) || (recType.compareTo("HFLS")==0)) {
        	request.setFilterCriteria("X_SAP_ID=? and PARTY_FK IS NOT NULL");
        }
        else {
        	System.out.println("Invalid Record Type, exiting ...");
        	return "Invalid Record Type";
        }
        
        ArrayList params = new ArrayList(2);
        params.add(new Parameter(sapId));
        request.setFilterParameters(params);
        
        SearchQueryResponse response = (SearchQueryResponse) sipClient.process(request);
        System.out.println(response.getMessage());
        ArrayList<Record> recordList = response.getRecords();
        System.out.println("The number of records are: "+recordList.size());
        for(Record record : recordList) {
        	Field field = record.getField("PARTY_FK");
        	System.out.println(field.getStringValue());
        	if(rowid == null) {
        		rowid = field.getStringValue();
        	}
        	else if(rowid.compareTo(field.getStringValue())!=0) {
        		return "Multiple FKs";
        	}
        }
        
		return rowid;
	}
	*/
	
	private static String getRowid(String sapId, String recType) {
		String rowid = null;
		String busRelId = null;
		SearchQueryRequest request = new SearchQueryRequest();
		request.setRecordsToReturn(20);
		request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_COMM_PREF");
		if (recType.compareTo("HEP") == 0) {
			request.setFilterCriteria("X_SAP_ID_1=?");
		} else if ((recType.compareTo("HFC") == 0) || (recType.compareTo("HFLS") == 0)) {
			request.setFilterCriteria("X_SAP_ID=?");
		} else {
			System.out.println("Invalid Record Type, exiting ...");
			return "Invalid Record Type";
		}

		ArrayList params = new ArrayList(2);
		params.add(new Parameter(sapId));
		request.setFilterParameters(params);

		SearchQueryResponse response = (SearchQueryResponse) sipClient.process(request);
		System.out.println(response.getMessage());
		ArrayList<Record> recordList = response.getRecords();
		System.out.println("The number of records are: " + recordList.size());
		for (Record record : recordList) {
			
			Field field = record.getField("PRTY_FK");
			Field field1 = record.getField("X_BUSINESS_REL_ID");
			
			System.out.println("The Party FK: "+field.getStringValue());
			if (rowid == null) {
				rowid = field.getStringValue();				
			}
			if(busRelId==null) {
				busRelId = field1.getStringValue();
			}
			System.out.println("bus rel ID: "+busRelId);
		}
		if(rowid==null&&busRelId!=null) {
			System.out.println("Searching the Bus Rel table");
			request = new SearchQueryRequest();
			request.setRecordsToReturn(20);
			request.setSiperianObjectUid("BASE_OBJECT.X_BUSINESS_REL_ID");
			request.setFilterCriteria("ROWID_OBJECT = ?");
			params = new ArrayList(2);
			params.add(new Parameter(busRelId));
			request.setFilterParameters(params);
			response = (SearchQueryResponse) sipClient.process(request);
			System.out.println(response.getMessage());
			recordList = response.getRecords();
			System.out.println("The number of records are: " + recordList.size());
			for (Record record : recordList) {
				
				Field field = record.getField("PARTY_FK");
				
				
				System.out.println("The Party FK: "+field.getStringValue());
				if (rowid == null) {
					rowid = field.getStringValue();				
				}
				
				
			}

		}
		return rowid;
	}

	private static String getFileExtension(String fileName) {
		String extension = "";
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		extension = extension.toLowerCase();
		return extension;
	}
	

	private static String getSAPId(String fileName) {
		String[] parts = fileName.split("_");
		return parts[0];
	}

}
