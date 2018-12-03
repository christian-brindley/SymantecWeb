package com.symantec.tree.nodes;

import static org.forgerock.openam.auth.node.api.Action.send;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.security.auth.callback.PasswordCallback;

import org.forgerock.guava.common.base.Strings;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.symantec.tree.config.Constants.CRED_ID;

/**
 * 
 * @author Symantec
 * @category Node
 * @Descrition "VIP SDK Enter CredentialID" node with single outcome.
 *
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VIPSDKEnterCredentialId.Config.class)
public class VIPSDKEnterCredentialId extends SingleOutcomeNode {

	private static final String BUNDLE = "com/symantec/tree/nodes/VIPEnterCredentialId";
	private final Logger logger = LoggerFactory.getLogger(VIPSDKEnterCredentialId.class);

	/**
	 * Configuration for the node.
	 */
	public interface Config {
	}

	/**
	 * Create the node.
	 */
	@Inject
	public VIPSDKEnterCredentialId() {

	}

	/**
     * 
     * @param context
     * @return password callback
     */
	private Action collectCredentialId(TreeContext context) {
		ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
		return send(new PasswordCallback(bundle.getString("callback.credId"), false)).build();
	}

	/**
	 * Main logic of the node
	 */
    @Override
    public Action process(TreeContext context) {
    	logger.info("Collecting Credential id");
        JsonValue sharedState = context.sharedState;
        
        return context.getCallback(PasswordCallback.class)
                .map(PasswordCallback::getPassword)
                .map(String::new)
                .filter(password -> !Strings.isNullOrEmpty(password))
                .map(password -> goToNext()
					.replaceSharedState(sharedState.copy().put(CRED_ID, password)).build())
                .orElseGet(() -> {
                	logger.info("Enter Credential ID");
                    return collectCredentialId(context);
                });
    }
}