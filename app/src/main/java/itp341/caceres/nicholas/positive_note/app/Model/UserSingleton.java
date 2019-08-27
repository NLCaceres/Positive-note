package itp341.caceres.nicholas.positive_note.app.Model;

import java.util.ArrayList;

/**
 * Created by NLCaceres on 5/3/2016.
 */
public class UserSingleton {
    private static UserSingleton ourInstance = new UserSingleton();

    public static UserSingleton getInstance() {
        return ourInstance;
    }

    private ArrayList<UserProfile> userList;

    private UserSingleton() {
        userList = new ArrayList<>();
        userList.add(new UserProfile("cgsazon", "Hi I'm Catherine", true, false, true, true, true, true, true));
        userList.add(new UserProfile("jychung", "Hi I'm Jean", false, true, true, true, true, true, true));
        userList.get(1).setUserLocation("Anaheim");
    }
    public ArrayList<UserProfile> getUsers() {
        return userList;
    }

}
