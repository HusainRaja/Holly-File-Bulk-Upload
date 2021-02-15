package com.holly.file.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.siperian.sif.client.SiperianClient;
import com.siperian.sif.client.SoapSiperianClient;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Parameter;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.mrm.GetRequest;
import com.siperian.sif.message.mrm.GetResponse;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http2.Header;

public class BESFileUpload {

	static String cookie = null;
	
	public static void main(String args[]) throws IOException {
		String tempid = createFileMetadata();
		uploadFile(tempid);
		String strResponse = addFileToRecord(tempid);
		String docRowid = extractDocRowid(strResponse.toString());
		String interactionId = getInteractionID(docRowid);
		promoteDoc(docRowid,interactionId);
		System.out.println("Printing the reponse in the main method :"+strResponse);
		
//		testExtract();
	}

	private static void promoteDoc(String docRowid, String interactionId) {
		File file = new File("C:\\Users\\hraja\\Downloads\\siperian-client.properties");
        System.out.println("Reading File:" + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("***ERROR -> Properties File does not exist in location - ");
            return;
        }
        SoapSiperianClient sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(file);
        
        
	}
		
	

	private static String getInteractionID(String docRowid) {
		File file = new File("C:\\Users\\hraja\\Downloads\\siperian-client.properties");
        System.out.println("Reading File:" + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("***ERROR -> Properties File does not exist in location - ");
            
        }
        SoapSiperianClient sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(file);
        GetRequest request = new GetRequest();
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_DOCS");
        RecordKey recKey = new RecordKey();
        recKey.setRowid(docRowid);
        GetResponse response = (GetResponse) sipClient.process(request);
        System.out.println(response.getInteractionId());
        
		return response.getInteractionId();
	}

	private static void testExtract() throws FileNotFoundException {
		String strResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<ns6:ExHFC_HEP_CustomerOrg xmlns:ns5=\"urn:co-types.informatica.mdm\" xmlns:ns0=\"urn:cs-rest.informatica.mdm\" xmlns:ns1=\"urn:cs-base.informatica.mdm\" xmlns:ns2=\"urn:co-base.informatica.mdm\" xmlns:ns6=\"urn:co-ors.informatica.mdm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns6:ExHFC_HEP_CustomerOrg\">\r\n"
				+ "   <ns6:ExHFC_HEP_CustomerOrg>\r\n"
				+ "      <ns2:key>\r\n"
				+ "         <ns1:rowid>20002</ns1:rowid>\r\n"
				+ "      </ns2:key>\r\n"
				+ "      <ns2:rowidObject>20002</ns2:rowidObject>\r\n"
				+ "      <ns6:ExDocuments xmlns:ns7=\"urn:co-meta.informatica.mdm\" xmlns:ns8=\"urn:task-base.informatica.mdm\">\r\n"
				+ "         <ns6:item>\r\n"
				+ "            <ns2:key>\r\n"
				+ "               <ns1:rowid>21</ns1:rowid>\r\n"
				+ "               <ns1:sourceKey>860019163000</ns1:sourceKey>\r\n"
				+ "            </ns2:key>\r\n"
				+ "            <ns2:rowidObject>21</ns2:rowidObject>\r\n"
				+ "         </ns6:item>\r\n"
				+ "         <ns6:item>\r\n"
				+ "            <ns2:key/>\r\n"
				+ "         </ns6:item>\r\n"
				+ "      </ns6:ExDocuments>\r\n"
				+ "   </ns6:ExHFC_HEP_CustomerOrg>\r\n"
				+ "   <ns6:changeSummary logging=\"false\" xmlns:sdo=\"commonj.sdo\"/>\r\n"
				+ "</ns6:ExHFC_HEP_CustomerOrg>";
		extractDocRowid(strResponse);
		
	}

	private static String addFileToRecord(String tempid) throws IOException {
		System.out.println("Adding the file to record");
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, "{\r\n    \"ExDocuments\": {\r\n        \"item\": [\r\n           \r\n            {\r\n                \"ExFileName\": \""+tempid+"\",\r\n                \"ExDisclaimer\": \"2\"\r\n            }\r\n        ]\r\n    }\r\n}");
				Request request = new Request.Builder()
				  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/cs/orcl-TCR_HUB/ExHFC_HEP_CustomerOrg/20002?systemName=Admin")
				  .method("POST", body)
				  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
				  .addHeader("Content-Type", "application/json")
				  .addHeader("Cookie", cookie)
				  .build();
				Response response = client.newCall(request).execute();
				ResponseBody responseBody = response.body();
				String strResponse = responseBody.string();
				
				System.out.println("response: "+ strResponse);
				System.out.println("printing the response again: "+strResponse);
				return strResponse;
				
				
		
	}

	private static String extractDocRowid(String strResponse) throws FileNotFoundException {
		System.out.println("Extracting the rowid");
		System.out.println("This is the response body in the extract method: " + strResponse);
		PrintWriter out = new PrintWriter("C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Documents\\Holly Project\\Project Files\\File Upload\\responseString_actual.txt");
		out.print(strResponse);
		out.close();
		String item = StringUtils.substringBetween(strResponse, "<ns6:item>", "<ns6:item>");
		String rowid = StringUtils.substringBetween(item, "<ns1:rowid>", "</ns1:rowid>");
		System.out.println("\n\n The rowid of the Doc is: "+rowid);
		return rowid;
		
	}

	private static void uploadFile(String tempid) throws IOException {
		System.out.println("uploading file");
		File directoryPath = new File("C:\\Users\\hraja\\OneDrive - Huron Consulting Group\\Pictures");
	    File filesList[] = directoryPath.listFiles();
	    File file = filesList[2];
	    System.out.println("file name" + file.getName());
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/octet-stream");
				RequestBody body = RequestBody.create(mediaType, file);
				Request request = new Request.Builder()
						  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/file/orcl-TCR_HUB/TEMP/"+tempid+"/content")
						  .method("PUT", body)
						  .addHeader("Content-Type", "application/octet-stream")
						  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
						  .addHeader("Cookie", cookie)
						  .build();
						Response response = client.newCall(request).execute();
				System.out.println("response: "+response.message());
		
	}

	private static String createFileMetadata() throws IOException {
		System.out.println("Creating the file Metadata");
		
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType, "{\r\n    \"fileName\": \"Matrix_wallapaper.jpg\",\r\n    \"fileType\": \"image\",\r\n    \"fileContentType\": \"image/jpeg\"\r\n}");
				Request request = new Request.Builder()
				  .url("https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/file/orcl-TCR_HUB/TEMP")
				  .method("POST", body)
				  .addHeader("Authorization", "Basic YWRtaW46aG9sbHlmcm9udEA0YTRh")
				  .addHeader("Content-Type", "application/json")
				  
				  .build();
				Response response = client.newCall(request).execute();
				ResponseBody responseBody = response.body();
				
				String tempId = responseBody.string();
				System.out.println("TempID = " + tempId);
				Headers headers = response.headers();
				System.out.println(headers.value(5));
				System.out.println(headers.value(4));
				String[] parts = headers.value(4).split(";");
				String JsessionCookie = parts[0];
				parts = headers.value(5).split(";");
				String Awselb = parts[0];
				cookie =  Awselb + "; " + JsessionCookie;
				System.out.println(cookie);
				return tempId;
	}

}
