package com.symantec.tree.request.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

public class HttpClientUtil2 {

	private final String KEYSTOREPASS = "Work12345#";
    private final String KEYPASS = "Work12345#";
    
    char SEP = File.separatorChar;
	String KEYSTOREPATH = System.getProperty("java.home") + SEP + "lib" + SEP + "security"+SEP+"vip.jks";
	

    public KeyStore readStore() throws Exception {
        try (InputStream keyStoreStream = new FileInputStream(KEYSTOREPATH)) {
            KeyStore keyStore = KeyStore.getInstance("JKS"); // or "PKCS12"
            keyStore.load(keyStoreStream, KEYSTOREPASS.toCharArray());
            return keyStore;
        }
    }
	
	public HttpClient  getHttpClient() {
		HttpClient httpClient = null;
		try {
			SSLContext sslContext = SSLContexts.custom()
	                .loadKeyMaterial(readStore(), KEYPASS.toCharArray())
	                .build();
			httpClient = HttpClients.custom().setSSLContext(sslContext).build();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
        return httpClient;
	}
}
