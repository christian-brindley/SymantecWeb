package com.symantec.tree.request.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Sacumen(www.sacumen.com)
 * @Description: Checking OTP with CheckOtpRequest.
 *
 */
public class CheckVIPOtp {

	static Logger logger = LoggerFactory.getLogger(CheckVIPOtp.class);

	/**
	 * 
	 * @param userName
	 * @param otpValue
	 * @return true if OTP is correct else false
	 * @throws NodeProcessException
	 */
	public Boolean checkOtp(String userName, String otpValue,String key_store,String key_store_pass) throws NodeProcessException {

		HttpClientUtil clientUtil = HttpClientUtil.getInstance();
		HttpPost post = new HttpPost(getURL());
		post.setHeader("CONTENT-TYPE", "text/xml; charset=ISO-8859-1");
		String payLoad = getViewUserPayload(userName, otpValue);
		logger.debug("Request Payload: " + payLoad);
		try {
			HttpClient httpClient = clientUtil.getHttpClientForgerock(key_store,key_store_pass);
			post.setEntity(new StringEntity(payLoad));
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			String body = IOUtils.toString(entity.getContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			String status = doc.getElementsByTagName("status").item(0).getTextContent();
			String statusMessage = doc.getElementsByTagName("statusMessage").item(0).getTextContent();
			if ("success".equalsIgnoreCase(statusMessage)) {
				return true;

			}

		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new NodeProcessException(e);
		}
		return false;

	}

	/**
	 * 
	 * @param userName
	 * @param otpValue
	 * @return CheckOtpRequest payload
	 */
	private static String getViewUserPayload(String userName, String otpValue) {
		logger.info("getting CheckOtpRequest payload");
		return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:vip=\"https://schemas.symantec.com/vip/2011/04/vipuserservices\">" + "   <soapenv:Header/>"
				+ "   <soapenv:Body>" + "      <vip:CheckOtpRequest>" + "<vip:requestId>" + new Random().nextInt(10)
				+ 11111 + "</vip:requestId>" + "<vip:userId>" + userName + "</vip:userId>"
				+ "         <vip:otpAuthData>" + "            <vip:otp>" + otpValue + "</vip:otp>           "
				+ "         </vip:otpAuthData>        " + "      </vip:CheckOtpRequest>" + "   </soapenv:Body>"
				+ "</soapenv:Envelope>";
	}

	/**
	 * 
	 * @return AuthenticationServiceURL
	 * @throws NodeProcessException 
	 */
	private String getURL() throws NodeProcessException {
		return GetVIPServiceURL.getInstance().serviceUrls.get("AuthenticationServiceURL");
	}

}
