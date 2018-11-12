package com.symantec.tree.request.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class VIPGetUser {
	
	static Logger logger = LoggerFactory.getLogger(VIPGetUser.class);
	private final String SymUrl = "https://userservices-auth.vip.symantec.com/vipuserservices/QueryService_1_8";
	
	public boolean viewUserInfo(String userId) {
		boolean isUserExisted = false;
		
        try {
        	HttpClient httpClient =new HttpClientUtil().getHttpClient();
           
        	HttpPost httpPost = new HttpPost(SymUrl);
        	httpPost.addHeader("Accept-Encoding", "gzip,deflate");
     		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
     		httpPost.addHeader("SOAPAction", "");	/* \"\" */
     		httpPost.addHeader("User-Agent","Apache-HttpClient/4.1.1");
        	
        	String userPayload = getViewUserPayload(userId);
    		InputStream is = new ByteArrayInputStream(userPayload.getBytes());
    		
    		InputStreamEntity  reqEntity = new InputStreamEntity(is,-1);
    		reqEntity.setContentType("text/xml");
    		reqEntity.setContentEncoding("charset=utf-8");
            reqEntity.setChunked(true);
    		
            logger.debug("req content length:\t"+reqEntity.getContentLength());
    		httpPost.setEntity(reqEntity);
            
    		logger.info("executing GetUserInfoRequest");
            HttpResponse response = httpClient.execute(httpPost);
            logger.debug("status code is",response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();

            
            logger.info("----------------------------------------");
            logger.debug(response.getStatusLine().toString());
            String body = IOUtils.toString(entity.getContent());
            logger.debug("response body is:\t"+body);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(body));
            Document doc = builder.parse(src);
			String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
            logger.debug("Status is:\t"+statusMessage);
            if("Success".equals(statusMessage)) {
            	isUserExisted = true;
            }
            
        }catch (Exception e) {
			//TODO need to handle this with a Node Process Exception or another exception that we transform upstream.
			// Also should only have try catch where required, not around so much extra code
        	logger.error(e.getMessage());
        	e.printStackTrace();
		}
		return isUserExisted;        
    }
	
	private String getViewUserPayload(String userId) {
		logger.info("getting GetUserInfoRequest payload");
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
				"xmlns:vip=\"https://schemas.symantec.com/vip/2011/04/vipuserservices\">" +
				"<soapenv:Header/>" +
				"<soapenv:Body>" +
				"<vip:GetUserInfoRequest>" +
				"<vip:requestId>" + Math.round(Math.random() * 100000) + "</vip:requestId>" +
				"" +
				"<vip:userId>" + userId + "</vip:userId>" +
				"" +
				"<vip:iaInfo>true</vip:iaInfo>" +
				"<vip:includePushAttributes>true</vip:includePushAttributes>" +
				"<vip:includeTokenInfo>true</vip:includeTokenInfo>" +
				"</vip:GetUserInfoRequest>" +
				"</soapenv:Body>" +
				"</soapenv:Envelope>";
	}

	public String getMobInfo(String userId) throws NullPointerException {
		String phoneNumber=null;

		try {
		HttpClient httpClient =new HttpClientUtil().getHttpClient();

		HttpPost httpPost = new HttpPost(SymUrl);
		httpPost.addHeader("Accept-Encoding", "gzip,deflate");
		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
		httpPost.addHeader("SOAPAction", "");
		httpPost.addHeader("User-Agent","Apache-HttpClient/4.1.1");

		String userPayload = getViewUserPayload(userId);
		InputStream is = new ByteArrayInputStream(userPayload.getBytes());

		InputStreamEntity reqEntity = new InputStreamEntity(is,-1);
		reqEntity.setContentType("text/xml");
		reqEntity.setContentEncoding("charset=utf-8");
		reqEntity.setChunked(true);

		System.out.println("req content length:\t"+reqEntity.getContentLength());
		httpPost.setEntity(reqEntity);

		HttpResponse response = httpClient.execute(httpPost);
		System.out.println(response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();


		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());
		String body = IOUtils.toString(entity.getContent());
		System.out.println("response body is:\t"+body);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(body));
		Document doc = builder.parse(src);

			System.out.println("credentialBindingDetail:" +  doc.getElementsByTagName("credentialBindingDetail"));
		if(doc.getElementsByTagName("credentialBindingDetail") != null) {
			System.out.println("credentialBindingDetail is NOT NULL");
			
			if(doc.getElementsByTagName("credentialBindingDetail").item(0) != null) {
				String credBindingDetail=doc.getElementsByTagName("credentialBindingDetail").item(0).getTextContent();
				
				String credType=doc.getElementsByTagName("credentialType").item(0).getTextContent();
				if (credBindingDetail==null || credType.equalsIgnoreCase("SMS_OTP") || credType.equalsIgnoreCase(
				 		"VOICE_OTP") ) {
				phoneNumber=doc.getElementsByTagName("credentialId").item(0).getTextContent();
				System.out.println("\n\nPhone Number:-->"+phoneNumber);
			}
				else 
				{
					String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
					System.out.println("Status is:\t"+statusMessage);
					if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) 
						{
						return "VIP_CRED_REGISTERED";
						
						}
				}
			}

			else {
					String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
					System.out.println("Status is:\t"+statusMessage);
					if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) {
						return "NO_CRED_REGISTERED";
					}
			}
		}
		else {
				String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
				System.out.println("Status is:\t"+statusMessage);
				if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) {
					return "NO_CRED_REGISTERED";
				}
		}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Phone Number at end--->"+phoneNumber);
		return phoneNumber; 
		}


}
