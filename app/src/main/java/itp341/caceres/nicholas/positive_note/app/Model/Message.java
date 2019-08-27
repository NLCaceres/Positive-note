package itp341.caceres.nicholas.positive_note.app.Model;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by NLCaceres on 5/9/2016.
 */

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private String message;
    private String sender;

    public Message() {
        // For Firebase download
    }

    public Message(String sender, String message) {
        this.message = message;
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
