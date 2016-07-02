/**
 * 
 */
package myapp.streaminsparksfapi.com;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * @author vikaul...
 *
 */
public class RESTGlobalParamStore {

	public static String USERNAME = "vineet.kaul@bhaskara.com";
	public static String PASSWORD = "Indian@1234kvLmTXHtr7cvHyYCtrwg20n2v";
	public static  String LOGINURL = "https://login.salesforce.com";
	public static  String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	public static  String CLIENTID = "3MVG9uudbyLbNPZPlpHPJabzjjc9lSIuCS_kjFK84h46edPu.M90Eo.lrEodTQWb5x5y9cdob5QVYtTXHXMa4";
	public static  String CLIENTSECRET = "2214130935665545719";
	
	public static String REST_ENDPOINT = "/services/data" ;
    public static String API_VERSION = "/v32.0" ;
    public static String baseUri;
    public static Header oauthHeader;
    public static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
    
}
