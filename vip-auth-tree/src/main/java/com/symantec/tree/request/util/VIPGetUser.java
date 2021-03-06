package com.symantec.tree.request.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 
 * @author Sacumen (www.sacumen.com)<br> <br>
 * 
 * Get user info from vip data base if user exists, else return
 * false.
 *
 */

public class VIPGetUser {

	static Logger logger = LoggerFactory.getLogger(VIPGetUser.class);

	/**
	 * 
	 * @param userId User Name
	 * @param KEY_STORE_PATH Keystore file path
	 * @param KEY_STORE_PASS Keystore file passwords
	 * @return status code of GetUserInfoRequest response.
	 * @throws NodeProcessException
	 */
	public String viewUserInfo(String userId, String KEY_STORE_PATH, String KEY_STORE_PASS, TreeContext context)
			throws NodeProcessException {
		
		//Constructing GetUserInfoRequest
		GetVIPServiceURL.getInstance().setServiceURL(context);
		HttpPost httpPost = new HttpPost(GetVIPServiceURL.getInstance().getQueryServiceURL());
		httpPost.addHeader("Accept-Encoding", "gzip,deflate");
		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
		httpPost.addHeader("SOAPAction", ""); /* \"\" */
		httpPost.addHeader("User-Agent", "Apache-HttpClient/4.1.1");

		//Getting GetUserInfoRequest payload
		String userPayload = getViewUserPayload(userId);
		InputStream is = new ByteArrayInputStream(userPayload.getBytes());

		InputStreamEntity reqEntity = new InputStreamEntity(is, -1);
		reqEntity.setContentType("text/xml");
		reqEntity.setContentEncoding("charset=utf-8");
		reqEntity.setChunked(true);

		logger.debug("req content length:\t" + reqEntity.getContentLength());
		httpPost.setEntity(reqEntity);
		String status;

		logger.info("executing GetUserInfoRequest");
		try {
			
			//Executing GetUserInfoRequest
			HttpClient httpClient = HttpClientUtil.getInstance().getHttpClientForgerock(KEY_STORE_PATH, KEY_STORE_PASS);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String body = IOUtils.toString(entity.getContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			
			//Getting status of GetUserInfoRequest
			status = doc.getElementsByTagName("status").item(0).getTextContent();
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new NodeProcessException(e);
		}
		return status;
	}

	/**
	 * 
	 * @param userId Username
	 * @return GetUserInfoRequest Payload
	 */
	private String getViewUserPayload(String userId) {
		logger.info("getting GetUserInfoRequest payload");
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:vip=\"https://schemas.symantec.com/vip/2011/04/vipuserservices\">" + "<soapenv:Header/>"
				+ "<soapenv:Body>" + "<vip:GetUserInfoRequest>" + "<vip:requestId>" + Math.round(Math.random() * 100000)
				+ "</vip:requestId>" + "" + "<vip:userId>" + userId + "</vip:userId>" + ""
				+ "<vip:iaInfo>true</vip:iaInfo>" + "<vip:includePushAttributes>true</vip:includePushAttributes>"
				+ "<vip:includeTokenInfo>true</vip:includeTokenInfo>" + "</vip:GetUserInfoRequest>" + "</soapenv:Body>"
				+ "</soapenv:Envelope>";
	}

	/**
	 * 
	 * @param userId User Name
	 * @param KEY_STORE_PATH Keystore file path
	 * @param KEY_STORE_PASS Keystore file passwords
	 * @return User's Mobile information, If user exists in vip database.
	 * @throws NullPointerException
	 * @throws NodeProcessException
	 */
	public String getMobInfo(String userId, String KEY_STORE_PATH, String KEY_STORE_PASS)
			throws NullPointerException, NodeProcessException {
		String phoneNumber = null;
		
		//Constructing GetUserInfoRequest
		HttpPost httpPost = new HttpPost(GetVIPServiceURL.getInstance().getQueryServiceURL());
		httpPost.addHeader("Accept-Encoding", "gzip,deflate");
		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
		httpPost.addHeader("SOAPAction", "");
		httpPost.addHeader("User-Agent", "Apache-HttpClient/4.1.1");

		//Getting GetUserInfoRequest payload
		String userPayload = getViewUserPayload(userId);
		InputStream is = new ByteArrayInputStream(userPayload.getBytes());

		InputStreamEntity reqEntity = new InputStreamEntity(is, -1);
		reqEntity.setContentType("text/xml");
		reqEntity.setContentEncoding("charset=utf-8");
		reqEntity.setChunked(true);

		logger.debug("req content length:\t" + reqEntity.getContentLength());
		httpPost.setEntity(reqEntity);

		try {
			
			//Executing GetUserInfoRequest
			HttpClient httpClient = HttpClientUtil.getInstance().getHttpClientForgerock(KEY_STORE_PATH, KEY_STORE_PASS);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String body = IOUtils.toString(entity.getContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			
			// Getting credentialBindingDetail for registered phone number
			if (doc.getElementsByTagName("credentialBindingDetail") != null) {
				if (doc.getElementsByTagName("credentialBindingDetail").item(0) != null) {
					String credBindingDetail = doc.getElementsByTagName("credentialBindingDetail").item(0)
							.getTextContent();

					String credType = doc.getElementsByTagName("credentialType").item(0).getTextContent();
					if (credBindingDetail == null || credType.equalsIgnoreCase("SMS_OTP")
							|| credType.equalsIgnoreCase("VOICE_OTP")) {
						phoneNumber = doc.getElementsByTagName("credentialId").item(0).getTextContent();
					} else {
						String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
						if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) {
							return "VIP_CRED_REGISTERED";

						}
					}
				}

				else {
					String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
					if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) {
						return "NO_CRED_REGISTERED";
					}
				}
			} else {
				String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
				logger.debug("Status is:\t" + statusMessage);
				if (statusMessage != null && statusMessage.equalsIgnoreCase("Success")) {
					return "NO_CRED_REGISTERED";
				}
			}

		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new NodeProcessException(e);
		}
		return phoneNumber;
	}


	/**
	 * 
	 * @param userId User Name
	 * @param KEY_STORE_PATH Keystore file path
	 * @param KEY_STORE_PASS Keystore file passwords
	 * @param context TreeContext instance
	 * @return CredentialBindingDetail
	 * @throws NodeProcessException
	 */
	public HashMap<String, String> getCredentialBindingDetail(String userId, String KEY_STORE_PATH, String KEY_STORE_PASS, TreeContext context) throws NodeProcessException {
		HashMap<String, String> credentialBindingDetail = new HashMap<>();
		Document doc;
		HttpPost httpPost = new HttpPost(GetVIPServiceURL.getInstance().getQueryServiceURL());
		httpPost.addHeader("Accept-Encoding", "gzip,deflate");
		httpPost.addHeader("Content-Type", "text/xml;charset=utf-8");
		httpPost.addHeader("SOAPAction", "");
		httpPost.addHeader("User-Agent", "Apache-HttpClient/4.1.1");

		String userPayload = getViewUserPayload(userId);
		InputStream is = new ByteArrayInputStream(userPayload.getBytes());

		InputStreamEntity reqEntity = new InputStreamEntity(is, -1);
		reqEntity.setContentType("text/xml");
		reqEntity.setContentEncoding("charset=utf-8");
		reqEntity.setChunked(true);

		logger.debug("req content length:\t" + reqEntity.getContentLength());
		httpPost.setEntity(reqEntity);
		
		logger.info("executing GetUserInfoRequest");
		try {
			HttpClient httpClient = HttpClientUtil.getInstance().getHttpClientForgerock(KEY_STORE_PATH, KEY_STORE_PASS);
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String body = IOUtils.toString(entity.getContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			doc = builder.parse(src);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new NodeProcessException(e);
		}
		org.w3c.dom.NodeList nList = doc.getElementsByTagName("credentialBindingDetail");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				String credentialId = eElement.getElementsByTagName("TokenId").item(0).getTextContent();
				String credentialType = eElement.getElementsByTagName("Adapter").item(0).getTextContent();
				credentialBindingDetail.put(credentialType, credentialId);
			}
		}
		return credentialBindingDetail;
	}
	
}
