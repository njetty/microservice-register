package edu.iu.uits.catalog.register;


import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by naveenjetty on 3/31/17.
 */
public class Register {

    static String url;

    /**
     *
     * @param title: String, name of the service exactly as it appears
     * @param details: HashMap<String, String>
     *               This is dynamic and builds the json payload based
     *               on key-value pairs provided by the user.
     *               Should contain all the required keys from the service.
     *               Title, Description etc..,
     *               For details on keys to be sent, refer the documentation
     *               of Micro Service Catalog Rest Service.
     */
    public static void registerService(String title, HashMap<String,String> details){
        String resourceName = "config.properties"; // could also be a constant
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            props.load(resourceStream);
        } catch (Exception e){
            //Something happened
            e.printStackTrace();
        }

        url = props.getProperty("BASE.URL");
        System.out.println(url);
        Response responseEntity = ClientBuilder.newClient()
                .target(url).path("/catalog/search/findByTitle").queryParam("title",title)
                .request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        if (responseEntity.getStatus() != 200){
            System.out.println(responseEntity.toString());
            // The service is not present in the database
            // Register the service with the data.
            createNewService(details);

        } else {
            System.out.println("Already a microservice exists with the name");
            System.out.println(responseEntity.readEntity(String.class));
        }
    }

    private static void createNewService(HashMap<String,String> details){
        if (details == null || details.size() == 0){
            System.out.println("No details are recieved to create a record");
            return;
        }
        StringBuilder payload = new StringBuilder();
        payload.append("{");
        for (Map.Entry<String,String> entry : details.entrySet()){
            payload.append("\"");
            payload.append(entry.getKey());
            payload.append("\":\"");
            payload.append(entry.getValue());
            payload.append("\",");
        }
        payload.deleteCharAt(payload.length()-1);
        payload.append("}");

        Response response = ClientBuilder.newClient()
                .target(url).path("/catalog").request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(payload.toString(), MediaType.APPLICATION_JSON));
        if (response.getStatus() != 200){
            System.out.println("Due to the following errors, the service failed to register");
            System.out.println(response.readEntity(String.class));
        } else {
            System.out.println("Service is successfully registered");
            System.out.println("Response from the server:");
            System.out.println(response.readEntity(String.class));
        }
    }

    public static void main(String[] args) {
        HashMap<String, String> details= new HashMap<>();
        details.put("title","test 2lm");
        details.put("description","Some description");
        registerService("test 2lm", details);
    }
}
