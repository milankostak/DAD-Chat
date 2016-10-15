package my.edu.taylors.dad.chat.entity;

public class Flags {

	public static final String CUSTOMER_AUTHENTICATED = "0";
	public static final int CUSTOMER_AUTHENTICATED_I = 0;

	public static final String AGENT_AUTHENTICATED = "1";
	public static final int AGENT_AUTHENTICATED_I = 1;

	public static final String AUTHENTICATICATION_ERROR = "-1";
	public static final int AUTHENTICATICATION_ERROR_I = -1;

	public static final String AUTHENTICATICATION_ATTEMTPS = "-2";
	public static final int AUTHENTICATICATION_ATTEMTPS_I = -2;

	public static final String SENDING_CUSTOMER_TO_AGENT = "-3";

	public static final String LOGOUT = "-4";
	public static final String NOTHING = "100";

	private Flags() { }
}
