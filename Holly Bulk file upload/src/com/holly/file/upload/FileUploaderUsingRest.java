package com.holly.file.upload;

import java.io.File;
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
	
	public static void main(String args[]) throws IOException {
	      String rowid = null;
	      String SAPId = null;
	      String fileName = null;
	      Scanner s = new Scanner(System.in);
	      System.out.println("Enter the dir path for the files: ");
	      String dirPath = s.nextLine();
	      System.out.println("path is:" + dirPath);
	      System.out.println("Enter doc type, Either 'VAF' or 'W9': ");
	      String docType = s.nextLine();
	      docType = docType.toUpperCase();
	      //dirPath = "C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Documents\\Holly Project\\Project Files\\File Upload\\Customer_Files\\HFC";
	      File directoryPath = new File(dirPath);
	      
	      //Getting the list of files
	      
	      File filesList[] = directoryPath.listFiles();
	      
	      //Initializing the Siperian Client for the SOAP calls
	      
	      initializeSipClient();
	      
	      //Iterating for every file
	      
	      for(File file : filesList) {
	         
	    	  fileName = file.getName();
	    	  System.out.println("\n\n****** Uploading the File : "+fileName+" ******\n\n");
	         
	         //Fetching the SAP ID from the file name
	         
	         SAPId = getSAPId(fileName);
	         System.out.println("The SAP ID is: " + SAPId);
	         
	         
	         //Fetching the Party rowid using the SAP ID
	         
	         rowid = getRowid(SAPId);
	         System.out.println("Rowid is: " + rowid);
	         if(rowid == null) {
	        	 System.out.println("Party FK not found, not processing this file.");
	        	 continue;
	         }
	         
	        //Creating the file Metadata and uploading the file
	         
	        String tempid = createFileMetadata(fileName);
	 		if(!uploadFile(tempid,file)) {
	 			System.out.println("File upload failed, skipping this file");
	 			continue;
	 		}
	 		
	 		//Adding the file attachment to the BE record
	 		
	 		String strResponse = addFileToRecord(tempid,rowid,docType);
	 		if(strResponse == null) {
	 			System.out.println("Breaking out of the loop as the doc type entered is invalid");
	 			break;
	 		}
	 		
	 		//Extracting the rowid of the doc from the response of the BES call to add the attachement to the record
	        
	 		String docRowid = extractDocRowid(strResponse);
	 		
	 		//Getting the Interaction from the document record for performing the promote call
	 		
	 		String interactionId = getInteractionID(docRowid);
	 		
	 		//Promoting the doc record that was created
	 		
	 		promoteDoc(docRowid,interactionId);
	 		
	        System.out.println("\n\n****** Successfully uploaded the File : "+file.getName()+" ******\n\n");
	         
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
		return strResponse;
	}

	private static String getDocTypDesc(String docType) {
		if(docType.compareTo("VAF")==0) {
			return "VAF";
		}
		else if(docType.compareTo("W9")==0) {
			return "W9 FEIN";
		}
		else {
			return null;
		}
	}

	private static String createFileMetadata(String fileName) throws IOException {
		MediaType mediaType = MediaType.parse("application/json");
		String fileType, fileContentType;
		fileType = getFileType(getFileExtension(fileName));
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
		
		String[] parts = headers.value(4).split(";");
		String JsessionCookie = parts[0];
		parts = headers.value(5).split(";");
		String Awselb = parts[0];
		cookie =  Awselb + "; " + JsessionCookie;
		
		String tempId = responseBody.string();
		System.out.println("TempID = " + tempId);
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
			return "image";
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
				System.out.println("response: "+response.message());
				if(response.message().compareTo("No Content")!=0) {
					return false;
				}
				else {
					return true;
				}
		
	}

	private static String getRowid(String sapId) {
		String rowid = null;
		SearchQueryRequest request = new SearchQueryRequest();
        request.setRecordsToReturn(5);
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_COMM_PREF");
        request.setFilterCriteria("c_bo_prty_rle_comm_pref.X_SAP_ID=? AND c_bo_prty_rle_comm_pref.prty_fk IS NOT NULL");
        ArrayList params = new ArrayList(2);
        params.add(new Parameter(sapId));
        request.setFilterParameters(params);
        
        SearchQueryResponse response = (SearchQueryResponse) sipClient.process(request);
        System.out.println(response.getMessage());
        ArrayList<Record> recordList = response.getRecords();
        System.out.println("The number of records are: "+recordList.size());
        for(Record record : recordList) {
        	Field field = record.getField("PRTY_FK");
        	System.out.println(field.getStringValue());
        	if(field.getStringValue()!=null) {
        		rowid = field.getStringValue();
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
		return extension;
	}
	

	private static String getSAPId(String fileName) {
		String[] parts = fileName.split("-");
		return parts[0];
	}

}
