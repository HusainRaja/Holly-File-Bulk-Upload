package com.holly.file.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import com.siperian.sif.client.SiperianClient;
import com.siperian.sif.client.SoapSiperianClient;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.RecordKey;
import com.siperian.sif.message.mrm.GetRequest;
import com.siperian.sif.message.mrm.GetResponse;
import com.siperian.sif.message.mrm.PromotePendingXrefsRequest;
import com.siperian.sif.message.mrm.PromotePendingXrefsResponse;

public class GetCall {
	public static void main(String args[]) {
		File file = new File("C:\\Users\\hraja\\Downloads\\siperian-client.properties");
        System.out.println("Reading File:" + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("***ERROR -> Properties File does not exist in location - ");
            
        }
        
        Properties properties = new Properties();
		properties.put(SiperianClient.SIPERIANCLIENT_PROTOCOL, "soap");
		properties.put("siperian-client.orsId", "orcl-TCR_HUB");
		properties.put("siperian-client.username", "admin");
		properties.put("siperian-client.password", "hollyfront@4a4a");
		properties.put("soap.call.url", "https://hollyfrontier-devint.mdm.informaticahostednp.com/cmx/services/SifService");
        
        
        SoapSiperianClient sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(properties);
        GetRequest request = new GetRequest();
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_DOCS");
        RecordKey recKey = new RecordKey();
        recKey.setRowid("25");
        request.setRecordKey(recKey);
        GetResponse response = (GetResponse) sipClient.process(request);
        ArrayList<Record> recList = response.getRecords();
        Record record = recList.get(0);
        System.out.println("Interaction ID = "+record.getField("INTERACTION_ID").getBigIntegerValue());
        String docRowid = "26";
        String interactionId = record.getField("INTERACTION_ID").getBigIntegerValue().toString();
        System.out.println("The Hub State Ind = "+record.getField("HUB_STATE_IND").getBigIntegerValue());
       //promoteDoc(docRowid,interactionId);
        //System.out.println(response.getInteractionId());
	}

	private static void promoteDoc(String docRowid, String interactionId) {
		File file = new File("C:\\Users\\hraja\\Downloads\\siperian-client.properties");
        System.out.println("Reading File:" + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("***ERROR -> Properties File does not exist in location - ");
            
        }
        SoapSiperianClient sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(file);
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

}
