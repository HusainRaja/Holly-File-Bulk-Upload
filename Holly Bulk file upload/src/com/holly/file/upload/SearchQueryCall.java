package com.holly.file.upload;

import java.io.File;
import java.util.ArrayList;

import com.siperian.sif.client.SiperianClient;
import com.siperian.sif.client.SoapSiperianClient;
import com.siperian.sif.message.Field;
import com.siperian.sif.message.Parameter;
import com.siperian.sif.message.Record;
import com.siperian.sif.message.mrm.SearchQueryRequest;
import com.siperian.sif.message.mrm.SearchQueryResponse;

public class SearchQueryCall {
	public static void main(String[] args) {
        File file = new File("C:\\Users\\hraja\\Downloads\\siperian-client.properties");
        System.out.println("Reading File:" + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("***ERROR -> Properties File does not exist in location - ");
            return;
        }
        SoapSiperianClient sipClient = (SoapSiperianClient) SiperianClient.newSiperianClient(file);
        SearchQueryRequest request = new SearchQueryRequest();
        request.setRecordsToReturn(20);
        request.setSiperianObjectUid("BASE_OBJECT.C_BO_PRTY_RLE_COMM_PREF");
        request.setFilterCriteria("X_SAP_ID=? and PRTY_FK IS NOT NULL");
        ArrayList params = new ArrayList(2);
        params.add(new Parameter("0005151515"));
        request.setFilterParameters(params);
        
        SearchQueryResponse response = (SearchQueryResponse) sipClient.process(request);
        System.out.println(response.getMessage());
        ArrayList<Record> recordList = response.getRecords();
        System.out.println("The number of records are: "+recordList.size());
        boolean flag=false;
        String rowid=null;
        for(Record record : recordList) {
        	Field field = record.getField("PARTY_FK");
        	System.out.println(field.getStringValue());
        	if(rowid == null) {
        		rowid = field.getStringValue();
        	}
        	else if(rowid.compareTo(field.getStringValue())!=0) {
        		flag = true;
        		break;
        	}
        }
        if(flag) {
        	System.out.println("Not a unique SAP ID");
        }
        else {
        	System.out.println("Rowid = "+rowid);
        }
	}
}
