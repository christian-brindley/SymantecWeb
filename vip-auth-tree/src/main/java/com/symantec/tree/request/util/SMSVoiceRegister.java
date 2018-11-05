package com.symantec.tree.request.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 * @author Symatec
 * Executing RegisterRequest for SMS and Voice
 */
public class SMSVoiceRegister {
	static Logger logger = LoggerFactory.getLogger(SMSVoiceRegister.class);

	/**
	 * 
	 * @param credValue
	 * register SMS
	 */
	public void smsRegister(String credValue) {

		HttpClientUtil clientUtil = new HttpClientUtil();
		HttpClient httpClient = clientUtil.getHttpClient();

		HttpPost post = new HttpPost(getURL());

		post.setHeader("CONTENT-TYPE", "text/xml; charset=ISO-8859-1");
		String payLoad = getSmsPayload(credValue);
		logger.debug("Request Payload: " + payLoad);
		try {
			post.setEntity(new StringEntity(payLoad));

			logger.info("executing RegisterRequest for SMS");
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();

			logger.debug("Response Code : " + response.getStatusLine().getStatusCode());

			logger.debug(response.getStatusLine().toString());
			String body = IOUtils.toString(entity.getContent());
			logger.debug("response body is:\t" + body);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			String status = doc.getElementsByTagName("status").item(0).getTextContent();
			String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
			logger.debug("Status is:\t" + statusMessage);
			logger.debug("SMS Register call:" + status);

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param credValue
	 * register voice
	 */
	public void voiceRegister(String credValue) {

		HttpClientUtil clientUtil = new HttpClientUtil();
		HttpClient httpClient = clientUtil.getHttpClient();

		HttpPost post = new HttpPost(getURL());

		post.setHeader("CONTENT-TYPE", "text/xml; charset=ISO-8859-1");
		String payLoad = getVoicePayload(credValue);
		logger.debug("Request Payload: " + payLoad);
		try {
			post.setEntity(new StringEntity(payLoad));

			logger.info("executing RegisterRequest for voice");
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();

			logger.debug("Response Code : " + response.getStatusLine().getStatusCode());
			logger.debug(response.getStatusLine().toString());
			String body = IOUtils.toString(entity.getContent());
			logger.debug("response body is:\t" + body);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			String status = doc.getElementsByTagName("status").item(0).getTextContent();
			String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
			logger.debug("Status is:\t" + statusMessage);
			logger.debug("SMSVOICERegister call:" + status);

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param credValue
	 * @return RegisterRequest payload
	 */
	public static String getSmsPayload(String credValue) {
		logger.info("getting RegisterRequest payload for SMS");
		StringBuilder str = new StringBuilder();
		str.append(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:vip=\"https://schemas.symantec.com/vip/2011/04/vipuserservices\">");
		str.append("<soapenv:Header/>");
		str.append("<soapenv:Body>");
		str.append("<vip:RegisterRequest>");
		str.append("<vip:requestId>" + new Random().nextInt(10) + 11111 + "</vip:requestId>");
		str.append("");
		str.append("<vip:smsDeliveryInfo>");
		str.append("<vip:phoneNumber>" + credValue + "</vip:phoneNumber> ");
		str.append("</vip:smsDeliveryInfo> ");
		str.append("</vip:RegisterRequest>");
		str.append("</soapenv:Body>");
		str.append("</soapenv:Envelope>");
		return str.toString();

	}

	/**
	 * 
	 * @param credValue
	 * @return RegisterRequest payload for voice
	 */
	public static String getVoicePayload(String credValue) {
		logger.info("getting RegisterRequest payload for voice");
		StringBuilder str = new StringBuilder();
		str.append(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:vip=\"https://schemas.symantec.com/vip/2011/04/vipuserservices\">");
		str.append("<soapenv:Header/>");
		str.append("<soapenv:Body>");
		str.append("<vip:RegisterRequest>");
		str.append("<vip:requestId>" + new Random().nextInt(10) + 11111 + "</vip:requestId>");
		str.append("");
		str.append("<vip:voiceDeliveryInfo>");
		str.append("<vip:phoneNumber>" + credValue + "</vip:phoneNumber> ");
		str.append("</vip:voiceDeliveryInfo> ");
		str.append("</vip:RegisterRequest>");
		str.append("</soapenv:Body>");
		str.append("</soapenv:Envelope>");
		return str.toString();

	}

	/**
	 * 
	 * @return ManagementServiceURL
	 */
	private String getURL() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("src/main/resources/vip.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty("ManagementServiceURL");
	}

}