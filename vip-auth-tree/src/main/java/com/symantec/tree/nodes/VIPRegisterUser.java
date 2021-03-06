package com.symantec.tree.nodes;

import static com.symantec.tree.config.Constants.*;
import com.symantec.tree.request.util.VIPCreateUser;
import javax.inject.Inject;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sacumen(www.sacumen.com)<br> <br>
 * 
 * @category Node
 * 
 * "VIP Register User" node with TRUE,FALSE outcome. If TRUE, it will go to "VIP Display Creds". If False, go to
 * "Failure".
 * 
 * It register user to VIP Database
 *
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class, configClass = VIPRegisterUser.Config.class)
public class VIPRegisterUser extends AbstractDecisionNode {

	public static final Logger logger = LoggerFactory.getLogger(VIPRegisterUser.class);

	private VIPCreateUser vIPCreateUser;

	/**
	 * Configuration for the node.
	 */
	public interface Config {

	}

	/**
	 * 
	 * @param vIPCreateUser VIPCreateUser instance
	 */
	@Inject
	public VIPRegisterUser(VIPCreateUser vIPCreateUser) {
		this.vIPCreateUser = vIPCreateUser;
	}

	/**
	 * Main logic of the node.
	 * @throws NodeProcessException 
	 */
	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		
		//Getting configured parameters
		String userName = context.sharedState.get(SharedStateConstants.USERNAME).asString();
		String credRegistrationStatus = context.transientState.get(NO_CREDENTIALS_REGISTERED).toString();
		String key_store = context.sharedState.get(KEY_STORE_PATH).asString();
		String key_store_pass = context.sharedState.get(KEY_STORE_PASS).asString();
		boolean isVIPProfileRegistered;

		logger.info("credRegistrationStatus:" + credRegistrationStatus);

		if (credRegistrationStatus != null && credRegistrationStatus.equalsIgnoreCase("true")) {
			logger.info("User already registered and hence not making user registration call");
			return goTo(true).build();
		} 
		
		//Calling CreateUserRequest
		else {
			logger.info("User not registered and hence making user registration call");
			isVIPProfileRegistered = vIPCreateUser.createVIPUser(userName,key_store,key_store_pass);
			return goTo(isVIPProfileRegistered).build();
		}
	}
}