package com.holly.file.upload;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class XMLParser {
	public static void main(String argv[]) {
		try {
//creating a constructor of file class and parsing an XML file  
			//File file = new File("F:\\XMLFile.xml");
//an instance of factory that gives a document builder
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

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//an instance of builder to parse the specified xml file  
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(strResponse);
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nodeList = doc.getElementsByTagName("student");
// nodeList is not iterable, so we are using for loop  
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);
				System.out.println("\nNode Name :" + node.getNodeName());
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node;
					System.out.println("Student id: " + eElement.getElementsByTagName("id").item(0).getTextContent());
					System.out.println(
							"First Name: " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
					System.out.println(
							"Last Name: " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
					System.out.println("Subject: " + eElement.getElementsByTagName("subject").item(0).getTextContent());
					System.out.println("Marks: " + eElement.getElementsByTagName("marks").item(0).getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}