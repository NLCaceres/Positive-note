package itp341.caceres.nicholas.positive_note.app.modelClasses;

import java.util.ArrayList;

/**
 * Created by NLCaceres on 5/3/2016.
 */
public class UserSingleton {
  private static UserSingleton ourInstance = new UserSingleton();
  private ArrayList<UserProfile> userList;

  private UserSingleton() {
    userList = new ArrayList<>();
    userList.add(new UserProfile("cgsazon", "Hi I'm Catherine", true, false, true, true, true, true, true));
    userList.add(new UserProfile("jychung", "Hi I'm Jean", false, true, true, true, true, true, true));
    userList.get(1).setUserLocation("Anaheim");
  }

  public static UserSingleton getInstance() {
    return ourInstance;
  }

  public ArrayList<UserProfile> getUsers() {
    return userList;
  }

}
