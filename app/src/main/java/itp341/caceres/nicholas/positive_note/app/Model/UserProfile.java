package itp341.caceres.nicholas.positive_note.app.Model;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by NLCaceres on 5/3/2016.
 */
//@JsonIgnoreProperties(ignoreUnknown = true)

public class UserProfile {
    private String userName;
    private String userBio;
    private boolean isPrivate;
    private boolean isHelper;
    private boolean isSelf;
    private boolean isBlue;
    private boolean isEmotional;
    private boolean isRelationship;
    private boolean isLooks;
    private String userLocation;


    public UserProfile() {
        // For firebase download
    }
    public UserProfile(String userName, String userBio, boolean isPrivate, boolean isHelper, boolean isBlue, boolean isRelationship, boolean isLooks, boolean isEmotional, boolean isSelf) {
        this.userName = userName;
        this.userBio = userBio;
        this.isPrivate = isPrivate;
        this.isHelper = isHelper;
        this.isBlue = isBlue;
        this.isRelationship = isRelationship;
        this.isLooks = isLooks;
        this.isEmotional = isEmotional;
        this.isSelf = isSelf;
    }
    public UserProfile(String userName, String userBio, boolean isHelper, boolean isBlue, boolean isRelationship, boolean isLooks, boolean isEmotional, boolean isSelf) {
        this.userName = userName;
        this.userBio = userBio;
        this.isHelper = isHelper;
        this.isBlue = isBlue;
        this.isRelationship = isRelationship;
        this.isLooks = isLooks;
        this.isEmotional = isEmotional;
        this.isSelf = isSelf;
    }
    public UserProfile(String userName, boolean isPrivate, boolean isHelper, boolean isBlue, boolean isRelationship, boolean isLooks, boolean isEmotional, boolean isSelf) {
        this.userName = userName;
        this.isPrivate = isPrivate;
        this.isHelper = isHelper;
        this.isBlue = isBlue;
        this.isRelationship = isRelationship;
        this.isLooks = isLooks;
        this.isEmotional = isEmotional;
        this.isSelf = isSelf;
    }
    public UserProfile(String userName, boolean isHelper, boolean isBlue, boolean isRelationship, boolean isLooks, boolean isEmotional, boolean isSelf) {
        this.userName = userName;
        this.isHelper = isHelper;
        this.isBlue = isBlue;
        this.isRelationship = isRelationship;
        this.isLooks = isLooks;
        this.isEmotional = isEmotional;
        this.isSelf = isSelf;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserBio() {
        return userBio;
    }
    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }
    public boolean getIsPrivate() {
        return isPrivate;
    }
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    public boolean getIsHelper() {
        return isHelper;
    }
    public void setIsHelper(boolean isHelper) {
        this.isHelper = isHelper;
    }
    public boolean getIsBlue() {
        return isBlue;
    }
    public void setIsBlue(boolean isBlue) {
        this.isBlue = isBlue;
    }
    public boolean getIsRelationship() {
        return isRelationship;
    }
    public void setIsRelationship(boolean isRelationship) {
        this.isRelationship = isRelationship;
    }
    public boolean getIsLooks() {
        return isLooks;
    }
    public void setLooks(boolean isLooks) {
        this.isLooks = isLooks;
    }
    public boolean getIsEmotional() {
        return isEmotional;
    }
    public void setIsEmotional(boolean isEmotional) {
        this.isEmotional = isEmotional;
    }
    public boolean getIsSelf() {
        return isSelf;
    }
    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }
    public String getUserLocation() {
        return userLocation;
    }
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
}
