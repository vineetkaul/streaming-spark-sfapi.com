/**
 * 
 */
package myapp.streaminsparksfapi.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
 

import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;


/**
 * @author vikaul
 *
 */
public class RESTWhatToDo {

	private static String leadId ;
    private static Double RetweetCount;
    private static String leadLastName;
    private static String leadCompany;
    
    
  //*************************************************************************************
    // This method queries the lead object to see if the lead exists.
    // If the lead exists, then the lead ID is passed to the Event method to create an 
    // event for a tweet.
    //*************************************************************************************
	public static String queryLeads(String LeadName) {
        System.out.println("\n_______________ Lead QUERY _______________");
        try {
 
            //Set up the HTTP objects needed to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            String uri = RESTGlobalParamStore.baseUri + "/query?q=Select+Id+,+RetweetCounts__c+From+Lead+where+FirstName+=+"+"'"+LeadName+"'";
            System.out.println("Query URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            System.out.println("oauthHeader2: " + RESTGlobalParamStore.oauthHeader);
            httpGet.addHeader(RESTGlobalParamStore.oauthHeader);
            httpGet.addHeader(RESTGlobalParamStore.prettyPrintHeader);
 
            // Make the request.
            HttpResponse response = httpClient.execute(httpGet);
 
            // Process the result
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    System.out.println("JSON result of Query:\n" + json.toString(1));
                    JSONArray j = json.getJSONArray("records");
                    for (int i = 0; i < j.length(); i++){
                        leadId = json.getJSONArray("records").getJSONObject(i).getString("Id");
                        RetweetCount = (Double) json.getJSONArray("records").getJSONObject(i).get("RetweetCounts__c");
                        //leadLastName = json.getJSONArray("records").getJSONObject(i).getString("LastName");
                        //leadCompany = json.getJSONArray("records").getJSONObject(i).getString("Company");
                        System.out.println("Lead record is: " + leadId + " with re tweet" + RetweetCount + " " + leadLastName + "(" + leadCompany + ")");
                    }
                
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            } else {
                System.out.println("Query was unsuccessful. Status code returned is " + statusCode);
                System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
                System.out.println(getBody(response.getEntity().getContent()));
                System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
		return leadId;
    }
 
    //*************************************************************************************
    // This method creates a lead for each new Tweet user.
    // This method is called to check if the lead exists and create it if it does not. 
    // by the program if it did not exist.
    // After creating Lead, the program creates a Lead Event for the tweet.
	//*************************************************************************************
    public static void createLeads(ArrayList<String> StreaminRequest) {
        System.out.println("\n_______________ Lead INSERT _______________");
 
        String uri = RESTGlobalParamStore.baseUri + "/sobjects/Lead/";
        try {
        	System.out.println("****************** New Dstream for Lead STARTS *************");
        	leadId = "";
        	RetweetCount = (double) 0;
        	leadId = queryLeads(StreaminRequest.get(0));
        	if (RetweetCount == null)
        	{
        		RetweetCount = (double) 0;
        	}

        	JSONObject lead = new JSONObject();
        	if (leadId == "") {
        	//create the JSON object containing the new lead details.
	            lead.put("FirstName", StreaminRequest.get(0));
	            lead.put("LastName", "[Twitter User] ");
	            lead.put("Company", "[API]");
	            lead.put("SICCode__c", StreaminRequest.get(3));
	            if ( StreaminRequest.get(2) == "true")
	            {
	            	System.out.println("This is a new re tweet " +  RetweetCount  + 1);
	            	lead.put("RetweetCounts__c", RetweetCount  + 1);
	            }
	            else
	            {
	            	System.out.println("This is NOT a new re tweet " +  RetweetCount);
	            	lead.put("RetweetCounts__c", 0);
	            }
	            System.out.println("JSON for lead record to be inserted:\n" + lead.toString(1));
 
	            //Construct the objects needed for the request
	            HttpClient httpClient = HttpClientBuilder.create().build();
	 
	            HttpPost httpPost = new HttpPost(uri);
	            httpPost.addHeader(RESTGlobalParamStore.oauthHeader);
	            httpPost.addHeader(RESTGlobalParamStore.prettyPrintHeader);
	            // The message we are going to post
	            StringEntity body = new StringEntity(lead.toString(1));
	            body.setContentType("application/json");
	            httpPost.setEntity(body);
	 
	            //Make the request
	            HttpResponse response = httpClient.execute(httpPost);
	 
	            //Process the results
	            int statusCode = response.getStatusLine().getStatusCode();
	            
	            if (statusCode == 201) 
	            {
	                String response_string = EntityUtils.toString(response.getEntity());
	                JSONObject json = new JSONObject(response_string);
	                // Store the retrieved lead id to use when we update the lead.
	                leadId = json.getString("id");
	                System.out.println("New Lead id from response: " + leadId);
	                if ( StreaminRequest.get(2) == "true")
		            {
	                	RESTWhatToDo.CreateLeadEvent(StreaminRequest, leadId, "RETWEET");
		            }
		            else
		            {
		            	RESTWhatToDo.CreateLeadEvent(StreaminRequest, leadId, "TWEET");
		            }
	            } 
	            else {
	            	System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
        		} 
        	}
        	else {
        		if ( StreaminRequest.get(2) == "true")
	            {
	            	RESTWhatToDo.updateLeads();
	            	RESTWhatToDo.CreateLeadEvent(StreaminRequest, leadId, "RETWEET");
	            }
        		else
        		{
        			RESTWhatToDo.CreateLeadEvent(StreaminRequest, leadId, "TWEET");
        		}
        	}

        	System.out.println("****************** New Dstream for Lead ENDS *************");
        	}
	            catch (JSONException e) {
        			System.out.println("Issue creating JSON or processing results");
        			e.printStackTrace();
        		} catch (IOException ioe) {
        			ioe.printStackTrace();
        		} catch (NullPointerException npe) {
        			npe.printStackTrace();
        		}
    }
 
    // Update Leads using REST HttpPatch. We have to create the HTTPPatch, as it does not exist in the standard library
    // Since the PATCH method was only recently standardized and is not yet implemented in Apache HttpClient
    public static void updateLeads() {
        System.out.println("\n_______________ Lead UPDATE _______________");
 
        //Notice, the id for the record to update is part of the URI, not part of the JSON
        String uri = RESTGlobalParamStore.baseUri + "/sobjects/Lead/" + leadId;
        try {
            //Create the JSON object containing the updated lead last name
            //and the id of the lead we are updating.
            JSONObject lead = new JSONObject();
            lead.put("RetweetCounts__c", RetweetCount + 1);
            System.out.println("JSON for update of lead record:\n" + lead.toString(1));
 
            //Set up the objects necessary to make the request.
            //DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            HttpPatch httpPatch = new HttpPatch(uri);
            httpPatch.addHeader(RESTGlobalParamStore.oauthHeader);
            httpPatch.addHeader(RESTGlobalParamStore.prettyPrintHeader);
            StringEntity body = new StringEntity(lead.toString(1));
            body.setContentType("application/json");
            httpPatch.setEntity(body);
 
            //Make the request
            HttpResponse response = httpClient.execute(httpPatch);
 
            //Process the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                System.out.println("Updated the lead successfully.");
            } else {
                System.out.println("Lead update NOT successfully. Status code is " + statusCode);
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
 
    // Extend the Apache HttpPost method to implement an HttpPatch
    private static class HttpPatch extends HttpPost {
        public HttpPatch(String uri) {
            super(uri);
        }
 
        public String getMethod() {
            return "PATCH";
        }
    }
 
    // Update Leads using REST HttpDelete (We have to create the HTTPDelete, as it does not exist in the standard library.)
    public static void deleteLeads() {
        System.out.println("\n_______________ Lead DELETE _______________");
 
        //Notice, the id for the record to update is part of the URI, not part of the JSON
        String uri = RESTGlobalParamStore.baseUri + "/sobjects/Lead/" + leadId;
        try {
            //Set up the objects necessary to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            HttpDelete httpDelete = new HttpDelete(uri);
            httpDelete.addHeader(RESTGlobalParamStore.oauthHeader);
            httpDelete.addHeader(RESTGlobalParamStore.prettyPrintHeader);
 
            //Make the request
            HttpResponse response = httpClient.execute(httpDelete);
 
            //Process the response
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                System.out.println("Deleted the lead successfully.");
            } else {
                System.out.println("Lead delete NOT successful. Status code is " + statusCode);
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
 
    private static String getBody(InputStream inputStream) {
        String result = "";
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream)
            );
            String inputLine;
            while ( (inputLine = in.readLine() ) != null ) {
                result += inputLine;
                result += "\n";
            }
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    //*************************************************************************************
    // This method creates a Event each time user / lead tweets about the company
    // or the product. This method is called only if the lead exists or is created 
    // by the program if it did not exist.
    //*************************************************************************************
    public static void CreateLeadEvent(ArrayList<String> StreaminRequest, String LeadID, String TweetType) {
        System.out.println("\n_______________ Lead INSERT _______________");
 
        String uri = RESTGlobalParamStore.baseUri + "/sobjects/Event/";
        try {
 
            //create the JSON object containing the new lead details.
            JSONObject leadEvent = new JSONObject();
            leadEvent.put("WhoId", LeadID);
            leadEvent.put("Subject", TweetType);
            leadEvent.put("ActivityDate", "2016-4-4");
            leadEvent.put("Description", StreaminRequest.get(1));
            leadEvent.put("IsAllDayEvent", "true");
            
            switch(StreaminRequest.get(4)){
            case "1" :
            	leadEvent.put("Event_Sentiment__c", "COLD");
               break; //optional
            case "2" :
            	leadEvent.put("Event_Sentiment__c", "WARM");
               break; //optional
            //You can have any number of case statements.
            case "3" :
            	leadEvent.put("Event_Sentiment__c", "HOT");
                break; //optional
        }
            System.out.println("JSON for lead event record to be inserted:\n" + leadEvent.toString(1));
 
            //Construct the objects needed for the request
            HttpClient httpClient = HttpClientBuilder.create().build();
 
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader(RESTGlobalParamStore.oauthHeader);
            httpPost.addHeader(RESTGlobalParamStore.prettyPrintHeader);
            // The message we are going to post
            StringEntity body = new StringEntity(leadEvent.toString(1));
            body.setContentType("application/json");
            httpPost.setEntity(body);
 
            //Make the request
            HttpResponse response = httpClient.execute(httpPost);
 
            //Process the results
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 201) {
                String response_string = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(response_string);
                // Store the retrieved lead id to use when we update the lead.
                leadId = json.getString("id");
                System.out.println("New Lead Event id from response: " + leadId);
            } else {
                System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
            }
        } catch (JSONException e) {
            System.out.println("Issue creating JSON or processing results");
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
    
}
