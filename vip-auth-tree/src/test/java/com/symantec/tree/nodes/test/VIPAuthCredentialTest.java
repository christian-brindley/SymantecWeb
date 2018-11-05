package com.symantec.tree.nodes.test;

import static com.symantec.tree.config.Constants.CREDCHOICE;
import static com.symantec.tree.config.Constants.CREDID;
import static com.symantec.tree.config.Constants.SECURECODE;
import static java.util.Collections.emptyList;
import static org.forgerock.json.JsonValue.object;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.SharedStateConstants;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.identity.authentication.spi.AuthLoginException;
import com.symantec.tree.nodes.VIPAuthCredential;

/**
 * 
 * @author Symantec
 * @category test
 * test class for "VIPAuthCredentials"
 *
 */
@Test
public class VIPAuthCredentialTest {

	@Mock
	private VIPAuthCredential.Config config;
	@Mock
	private CoreWrapper coreWrapper;

	@BeforeMethod
	public void before() throws URISyntaxException, AuthLoginException {

		initMocks(this);
		when(config.displayMsgText()).thenReturn("Sacumen Push");
		when(config.displayMsgTitle()).thenReturn("Sacumen Push");
		when(config.displayMsgTitle()).thenReturn("www.sacumen.com");


	}
	@Test
	public void proces() throws Exception {
        TreeContext context = getTreeContext(new HashMap<String, String[]>());

		context.sharedState.put(SharedStateConstants.USERNAME,"ruchika");
		context.sharedState.put(CREDID,"SYMC87283752");
		
		VIPAuthCredential node = new VIPAuthCredential(config,coreWrapper);

		// WHEN
		Action action = node.process(context);

	}

	private TreeContext getTreeContext(Map<String, String[]> parameters) {
		return new TreeContext(JsonValue.json(object(1)),
				new ExternalRequestContext.Builder().parameters(parameters).build(), emptyList());
	}
}
