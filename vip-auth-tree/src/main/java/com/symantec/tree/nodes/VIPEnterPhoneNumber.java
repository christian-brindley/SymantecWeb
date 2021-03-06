package com.symantec.tree.nodes;

import static org.forgerock.openam.auth.node.api.Action.send;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextOutputCallback;
import org.forgerock.guava.common.base.Strings;
import org.forgerock.guava.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.OutcomeProvider;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.node.api.Action.ActionBuilder;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.symantec.tree.request.util.SMSVoiceRegister;
import static com.symantec.tree.config.Constants.*;

/**
 * 
 * @author Sacumen(www.sacumen.com) <br> <br>
 * 
 * @category Node
 * 
 * "VIP Enter Phone Number" node with true, false and error outcome. If true, node will
 *  redirect to "VIP Enter SecurityCode/OTP". If false. node will redirect to same node and if error, it will 
 *  redirect to "VIP Display Error".
 *
 * It displays textbox to user to enter phone number.
 */
@Node.Metadata(outcomeProvider = VIPEnterPhoneNumber.SymantecOutcomeProvider.class, configClass = VIPEnterPhoneNumber.Config.class)
public class VIPEnterPhoneNumber implements Node {

	private static final String BUNDLE = "com/symantec/tree/nodes/VIPEnterPhoneNumber";
	Logger logger = LoggerFactory.getLogger(VIPEnterPhoneNumber.class);
	private SMSVoiceRegister svRegister;

	/**
	 * Configuration for the node.
	 */
	public interface Config {
	}

	/**
	 * 
	 * @param svRegister SMSVoiceRegister instance
	 */
	@Inject
	public VIPEnterPhoneNumber(SMSVoiceRegister svRegister) {
		this.svRegister = svRegister;
	}

	/**
	 * Main logic of the node.
	 */
	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		logger.info("Collect PhoneNumber started");
		
		//Getting configured parameters
		JsonValue sharedState = context.sharedState;
		String key_store = context.sharedState.get(KEY_STORE_PATH).asString();
		String key_store_pass = context.sharedState.get(KEY_STORE_PASS).asString();
		
		// Getting Phone number from the user
		return context.getCallback(NameCallback.class).map(NameCallback::getName).map(String::new)
				.filter(name -> !Strings.isNullOrEmpty(name)).map(name -> {
					logger.info("CredID has been collected and placed  into the Shared State");
					String credType = context.sharedState.get(CRED_CHOICE).asString();
					
					// Registering entered phone number for vip:smsDeliveryInfo 
					if (credType.equalsIgnoreCase(SMS)) {
						logger.info("calling sms register method");
						String status = null;
						try {
							status = svRegister.smsRegister(name, key_store, key_store_pass);
							sharedState.put(MOB_NUM, name);
						} catch (NodeProcessException e) {
							e.printStackTrace();
						}
						return sendOutput(status, context);

					} 
					
					// Registering entered phone number for vip:voiceDeliveryInfo
					else if (credType.equalsIgnoreCase(VOICE)) {
						String status = null;
						logger.info("calling voice register method");
						try {
							status = svRegister.voiceRegister(name, key_store, key_store_pass);
							sharedState.put(MOB_NUM, name);
						} catch (NodeProcessException e) {
							e.printStackTrace();
						}
						return sendOutput(status, context);

					}
					
					// Displaying Error, If occurs.
					else {
						context.sharedState.put(DISPLAY_ERROR, "Not able to send OTP on given Phone Number, Please contact to your administrator.");
						return goTo(Symantec.ERROR).build(); 
					}
				
			     }).orElseGet(() -> {
					logger.info("Enter Credential ID");
					return collectOTP(context);
				});
	}

	/**
	 * The possible outcomes for the VIP Enter Phone Number.
	 */
	private enum Symantec {
		/**
		 * Successful.
		 */
		TRUE,
		/**
		 * failed.
		 */
		FALSE,
		/**
		 * Disabled.
		 */
		ERROR

	}

	private ActionBuilder goTo(Symantec outcome) {
		return Action.goTo(outcome.name());
	}

	/**
	 * Defines the possible outcomes from this SymantecOutcomeProvider node.
	 */
	public static class SymantecOutcomeProvider implements OutcomeProvider {
		@Override
		public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
			ResourceBundle bundle = locales.getBundleInPreferredLocale(VIPEnterPhoneNumber.BUNDLE,
					SymantecOutcomeProvider.class.getClassLoader());
			return ImmutableList.of(new Outcome(Symantec.TRUE.name(), bundle.getString("trueOutcome")),
					new Outcome(Symantec.FALSE.name(), bundle.getString("falseOutcome")),
					new Outcome(Symantec.ERROR.name(), bundle.getString("errorOutcome")));
		}
	}

	/**
	 * 
	 * @param statusCode response status code of RegisterRequest
	 * @param context TreeContext instance
	 * @return Action instance
	 * 
	 * It makes decision for outcome according to response status code of RegisterRequest.
	 */
	private Action sendOutput(String statusCode, TreeContext context) {
		if (statusCode.equalsIgnoreCase(SUCCESS_CODE)) {
			return goTo(Symantec.TRUE).build();
		} 
		else if(statusCode.equalsIgnoreCase(CREDENTIALS_ALREADY_REGISTERED)) {
			context.sharedState.put(PHONE_NUMBER_ERROR, "Entered phone number is already registered,Please enter valid Phone Number");
			return goTo(Symantec.FALSE).build();
		}
		else if (statusCode.equalsIgnoreCase(INVALID_PHONE_NUMBER)) {
			context.sharedState.put(PHONE_NUMBER_ERROR, "Entered phone number is Invalid,Please enter valid Phone Number");
			return goTo(Symantec.FALSE).build();
		} else {
			context.sharedState.put(DISPLAY_ERROR, "Not able to send OTP on given Phone Number, Please contact to your administrator.");
			return goTo(Symantec.ERROR).build();
		}
	}

	/**
	 *
	 * @param context TreeContext instance
	 * @return Action instance
	 */
	public Action collectOTP(TreeContext context) {
		String outputError = context.sharedState.get(PHONE_NUMBER_ERROR).asString();
		List<Callback> cbList = new ArrayList<>(2);
		logger.debug("output error is "+outputError);
		
		// Fetching only textbox when there is no error.
		if(outputError==null) {
			ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
			NameCallback nameCallback = new NameCallback(bundle.getString("callback.phoneNumber"), "Enter PhoneNumber");
			cbList.add(nameCallback);
		}
		
		// Fetching textbox with error.
		else {
			ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
			NameCallback nameCallback = new NameCallback(bundle.getString("callback.phoneNumber"), "Enter PhoneNumber");
			TextOutputCallback tcb = new TextOutputCallback(0, outputError);
			cbList.add(tcb);
			cbList.add(nameCallback);
		}
		return send(ImmutableList.copyOf(cbList)).build();
	}
}