package com.symantec.tree.nodes;

import static com.symantec.tree.config.Constants.*;

import java.util.List;
import java.util.ResourceBundle;

import com.google.inject.assistedinject.Assisted;
import com.symantec.tree.request.util.VIPGetUser;
import javax.inject.Inject;

import org.forgerock.guava.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.auth.node.api.Action.ActionBuilder;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sacumen(www.sacumen.com)<br> <br>
 * 
 * @category Node
 * 
 * "VIP Search User" node with TRUE,FALSE and ERROR outcome. If TRUE, it will go to "VIP Push Auth User". If False, go to
 * "VIP Register User" and if ERROR, It will go to "VIP Display Error" Page.
 * 
 * It gets user info from VIP Database
 *
 */
@Node.Metadata(outcomeProvider = VIPSearchUser.SymantecOutcomeProvider.class, configClass = VIPSearchUser.Config.class)
public class VIPSearchUser implements Node {
	Logger logger = LoggerFactory.getLogger(VIPSearchUser.class);
	private static final String BUNDLE = "com/symantec/tree/nodes/VIPSearchUser";

	/**
	 * Configuration for the node.
	 */
	 public interface Config {
		@Attribute(order = 100, requiredValue = true)
		String Key_Store_Path();


		@Attribute(order = 200, requiredValue = true)
		String Key_Store_Password();
		
		@Attribute(order = 300, requiredValue = true)
		String Authentication_Service_URL();
		
		@Attribute(order = 400, requiredValue = true)
		String Query_Service_URL();
		
		@Attribute(order = 500, requiredValue = true)
		String Management_Service_URL();
	}
	 
	private VIPGetUser vipSearchUser;
	private final Config config;

	/**
	 * 
	 * @param config Config instance
	 * @param vipSearchUser VIPGetUser instance
	 */
	@Inject
	public VIPSearchUser(@Assisted Config config,VIPGetUser vipSearchUser) {
		this.config = config;
		this.vipSearchUser = vipSearchUser;
	}

	/**
	 * Main logic of the node.
	 * @throws NodeProcessException 
	 */
	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		
		//Getting configured parameters
		String userName = context.sharedState.get(SharedStateConstants.USERNAME).asString();
		context.sharedState.put(KEY_STORE_PATH,config.Key_Store_Path());
		context.sharedState.put(KEY_STORE_PASS,config.Key_Store_Password());
		context.sharedState.put(AUTHENTICATION_SERVICE_URL,config.Authentication_Service_URL());
		context.sharedState.put(QUERY_SERVICE_URL,config.Query_Service_URL());
		context.sharedState.put(MANAGEMENT_SERVICE_URL,config.Management_Service_URL());
		
		//Getting User Info
		String statusCode = vipSearchUser.viewUserInfo(userName,config.Key_Store_Path(),config.Key_Store_Password(),context);
        String mobNum;

			if (statusCode.equalsIgnoreCase(SUCCESS_CODE)) {
				
				//Getting Mobile info of user
				mobNum = vipSearchUser.getMobInfo(userName,config.Key_Store_Path(),config.Key_Store_Password());
				logger.debug("Phone Number " + mobNum);

				if (mobNum != null && mobNum.equalsIgnoreCase(NO_CRED_REGISTERED)) {
					logger.info("No Credential Registered");
					context.transientState.put(NO_CREDENTIALS_REGISTERED, true);
					return goTo(Symantec.FALSE).build();
				} else if (mobNum != null && mobNum.equalsIgnoreCase(VIP_CRED_REGISTERED)) {
					logger.info("VIP Credential Registered");
					return goTo(Symantec.TRUE).build();
				} else {
					context.sharedState.put(MOB_NUM, mobNum);
					return goTo(Symantec.TRUE).build();
				}
			} else if(statusCode.equalsIgnoreCase(USER_DOES_NOT_EXIST)) {
				return goTo(Symantec.FALSE).build();
			}else {
				context.sharedState.put(DISPLAY_ERROR,"User is locked, Please contact to administrator");
				return goTo(Symantec.ERROR).build();
			}
	}
	
	private ActionBuilder goTo(Symantec outcome) {
		return Action.goTo(outcome.name());
	}
	
	/**
	 * The possible outcomes for the VIP Search User.
	 */
	public enum Symantec {
		/**
		 * Successful.
		 */
		TRUE,
		/**
		 * failed.
		 */
		FALSE,
		/**
		 * Locked.
		 */
		ERROR

	}
	
	/**
	 * Defines the possible outcomes from this SymantecOutcomeProvider node.
	 */
	public static class SymantecOutcomeProvider implements OutcomeProvider {
		@Override
		public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
			ResourceBundle bundle = locales.getBundleInPreferredLocale(VIPSearchUser.BUNDLE,
					SymantecOutcomeProvider.class.getClassLoader());
			return ImmutableList.of(new Outcome(Symantec.TRUE.name(), bundle.getString("trueOutcome")),
					new Outcome(Symantec.FALSE.name(), bundle.getString("falseOutcome")),
					new Outcome(Symantec.ERROR.name(), bundle.getString("errorOutcome")));
		}
	}

}