package com.symantec.tree.request.util;

import java.io.IOException;
import java.io.StringReader;

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
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author Sacumen(www.sacumen.com) 
 * @Desription Getting activation code using GetActivationCode request
 *
 */
public class GenerateActivationCode {
	private final static Logger logger = LoggerFactory.getLogger(GenerateActivationCode.class);

	/**
	 * 
	 * @return activation code with status
	 * @throws NodeProcessException
	 */
	public String generateCode(String key_store,String key_store_pass,String url) throws NodeProcessException {
		String activationCode = "";
		HttpPost post = new HttpPost(url);
		String status = null;
		post.setHeader("CONTENT-TYPE", "text/xml; charset=ISO-8859-1");
		String payLoad = createPayload();
		logger.debug("Request Payload: " + payLoad);
		try {
			HttpClient httpClient = HttpClientUtil.getInstance().getHttpClientForgerock(key_store,key_store_pass);
			post.setEntity(new StringEntity(payLoad));
			HttpResponse response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			String body = IOUtils.toString(entity.getContent());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource src = new InputSource();
			src.setCharacterStream(new StringReader(body));
			Document doc = builder.parse(src);
			status = doc.getElementsByTagName("ReasonCode").item(0).getTextContent();
			String statusMessage = doc.getElementsByTagName("StatusMessage").item(0).getTextContent();
			if (doc.getElementsByTagName("ActivationCode").item(0) != null) {
				activationCode = doc.getElementsByTagName("ActivationCode").item(0).getTextContent();
			} else
				activationCode = " ";
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new NodeProcessException(e);
		}
		String code = status + "," + activationCode;
		logger.debug("Status and TransactionId \t" + code);
		return code;
	}

	/**
	 * 
	 * @return GetActivationCode payload
	 */
	public static String createPayload() {
		logger.info("gtting GetActivationCode payload");
		StringBuilder str = new StringBuilder();
		str.append(
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:vip=\"http://www.verisign.com/2006/08/vipservice\">");
		str.append("   <soapenv:Header/>");
		str.append("   <soapenv:Body>");
		str.append(
				"      <vip:GetActivationCode Version=\"1.0\" Id=" + "\"" + Math.round(Math.random() * 100000) + "\">");
		str.append("        <vip:ACProfile>" + "MOBILEPHONE" + "</vip:ACProfile>");
		str.append("      </vip:GetActivationCode>");
		str.append("   </soapenv:Body>");
		str.append("</soapenv:Envelope>");
		return str.toString();

	}

}