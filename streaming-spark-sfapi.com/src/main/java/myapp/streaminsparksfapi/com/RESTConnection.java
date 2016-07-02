/**
 * 
 */
package myapp.streaminsparksfapi.com;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

/**
 * @author vikaul....
 *
 */

public class RESTConnection {

	public static void CreateRestConnection() {
    HttpClient httpclient = HttpClientBuilder.create().build();
    
    // Assemble the login request URL
    String loginURL = RESTGlobalParamStore.LOGINURL +
    		RESTGlobalParamStore.GRANTSERVICE +
                      "&client_id=" + RESTGlobalParamStore.CLIENTID +
                      "&client_secret=" + RESTGlobalParamStore.CLIENTSECRET +
                      "&username=" + RESTGlobalParamStore.USERNAME +
                      "&password=" + RESTGlobalParamStore.PASSWORD;

    // Login requests must be POSTs
    HttpPost httpPost = new HttpPost(loginURL);
    HttpResponse response = null;

    try {
        // Execute the login POST request
        response = httpclient.execute(httpPost);
    } 
    
    catch (ClientProtocolException cpException) {
        cpException.printStackTrace();
    } 
    
    catch (IOException ioException) {
        ioException.printStackTrace();
    }

    // verify response is HTTP OK
    final int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != HttpStatus.SC_OK) {
        System.out.println("Error authenticating to Force.com: "+statusCode);
        // Error is in EntityUtils.toString(response.getEntity())
        return;
    }
    else
    {
    	String getResult = null;
        try {
            getResult = EntityUtils.toString(response.getEntity());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
 
        JSONObject jsonObject = null;
        String loginAccessToken = null;
        String loginInstanceUrl = null;
 
        try {
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            loginAccessToken = jsonObject.getString("access_token");
            loginInstanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    	RESTGlobalParamStore.baseUri = loginInstanceUrl + RESTGlobalParamStore.REST_ENDPOINT + RESTGlobalParamStore.API_VERSION ;
    	RESTGlobalParamStore.oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken) ;
    	System.out.println(response.getStatusLine());
    	System.out.println("Successful login");
    	System.out.println("  instance URL: "+loginInstanceUrl);
    	System.out.println("  access token/session ID: "+loginAccessToken);
    }
    
    // release connection
    httpPost.releaseConnection();
    
	}
}
