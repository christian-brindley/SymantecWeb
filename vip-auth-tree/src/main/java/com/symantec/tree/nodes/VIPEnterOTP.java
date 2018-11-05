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
import static com.symantec.tree.config.Constants.SECURECODE;
import static com.symantec.tree.config.Constants.MOBNUM;

/**
 * 
 * @author Symantec
 * @category Node
 * @Descrition "VIP Enter SecurityCode/OTP" node with single outcome. This node will redirect to "VIP Check Symantec OTP".
 *
 */
@Node.Metadata(outcomeProvider  = SingleOutcomeNode.OutcomeProvider.class,
               configClass      = VIPEnterOTP.Config.class)
public class VIPEnterOTP extends SingleOutcomeNode {

    private static final String BUNDLE = "com/symantec/tree/nodes/VIPEnterOTP";
    private final Logger logger = LoggerFactory.getLogger(VIPEnterOTP.class);

    /**
     * Configuration for the node.
     */
    public interface Config {}
    

    /**
     * Create the node.
     */
    @Inject
    public VIPEnterOTP() {

    }
    
    /**
     * 
     * @param context
     * @return password callback
     */
	private Action collectOTP(TreeContext context) {
		ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
		return send(new PasswordCallback(bundle.getString("callback.securecode"), false)).build();
	}

	/**
	 * Main logic of the node
	 */
    @Override
    public Action process(TreeContext context) {
    	logger.info("Collect SecurityCode started");
        JsonValue sharedState = context.sharedState;
        
        return context.getCallback(PasswordCallback.class)
                .map(PasswordCallback::getPassword)
                .map(String::new)
                .filter(password -> !Strings.isNullOrEmpty(password))
                .map(password -> {
                	logger.info("SecureCode has been collected and placed  into the Shared State");
                	logger.debug("mobile number in vip rnter otp is "+context.sharedState.get(MOBNUM));
                	
                    return goToNext()
                        .replaceSharedState(sharedState.copy().put(SECURECODE, password)).build();
                })
                .orElseGet(() -> {
                	logger.info("Enter Credential ID");
                    return collectOTP(context);
                });
    }
}