package URLShortner.app.controller;

import org.bson.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.Iterator;

@RestController
public class UrlController {
	
	@RequestMapping(value = "/shortener", method=RequestMethod.POST, consumes = {"application/json"})
	public String shortenUrl(@RequestBody @Valid final ShortenRequest shortenRequest, HttpServletRequest request) {

		// Connecting to database and getting the collection. They get created automatically if they doesn't exist
		MongoClient mongo1 = new MongoClient("localhost", 27017);
		MongoDatabase database = mongo1.getDatabase("myDb6");
		MongoCollection<Document> collection = database.getCollection("urlCollection");
		
		String longUrl = shortenRequest.getUrl();  // Url that needs to be shortened
		
		// Finding if the url already exists
		BasicDBObject whereQuery = new BasicDBObject();
	    whereQuery.put("url", longUrl);
	    Iterator<Document> cursor = collection.find(whereQuery).iterator();
	    String id;
		if(cursor.hasNext()) { // if url exists just get its id
			id = cursor.next().get("id").toString();
		}
		else { // if url doesn't exist
			// This try is block is needed because without the first insertion mongo actually doesn't create the database, so stats gives error
			try { 
				Document stats = database.runCommand(new Document("collStats", "urlCollection"));
				int len1 = stats.getInteger("count");
				id =  Integer.toString(len1+1);
			}
			catch(Exception e){ // First insertion.
				id = Integer.toString(1);
			}
			Document document = new Document("url", longUrl).append("id", id); //inserting into database
			collection.insertOne(document);
		}
		mongo1.close();
		String ans = "http://localhost:8080/" + id;
		return ans;
	}
	
	@RequestMapping(value = "/{id}", method=RequestMethod.GET)
    public RedirectView redirectUrl(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException, Exception {
		
		// Connecting to database
		MongoClient mongo = new MongoClient("localhost", 27017);
		MongoDatabase database = mongo.getDatabase("myDb6");
		MongoCollection<Document> collection = database.getCollection("urlCollection");
		
		// Finding the url for the given id.
		BasicDBObject whereQuery = new BasicDBObject();
	    whereQuery.put("id", id);
	    MongoCursor<Document> cursor = collection.find(whereQuery).iterator();
		String url = cursor.next().getString("url");
		mongo.close();
		
		// Redirecting to the url
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://" + url);
        return redirectView;
    }

}

//This class is created to take the incoming json object for the above original url request map.
class ShortenRequest{
 private String url;

 @JsonCreator
 public ShortenRequest() {
 }

 @JsonCreator
 public ShortenRequest(@JsonProperty("url") String url) {
     this.url = url;
 }

 public String getUrl() {
     return url;
 }

 public void setUrl(String url) {
     this.url = url;
 }
}
