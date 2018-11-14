package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);
    Hashing hashing = new Hashing();

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it.
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + hashing.hashWithSalt(user.getPassword())
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  // Emil - Deleting specific user by ID
  public static void deleteUser(int userId) {

    Log.writeLog(UserController.class.getName(), userId,"Deleting user in DB",0);

    //Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Constructing our SQL
    String sql = "DELETE FROM user where id=" + userId;

    dbCon.updateDB(sql);
  }

  // Emil - Updating specific user by ID
  public static void updateUser(int userIdToUpdate , User userUpdate) {

      Log.writeLog(UserController.class.getName(), userIdToUpdate, "Updating user in DB", 0);
      Hashing hashing = new Hashing();

      //Check for DB Connection
      if (dbCon == null) {
          dbCon = new DatabaseController();
      }

      User currentUser = UserController.getUser(userIdToUpdate);

      if (userUpdate.getFirstname() == null) {
        userUpdate.setFirstname(currentUser.getFirstname());
      }
      if (userUpdate.getLastname() == null) {
        userUpdate.setLastname(currentUser.getLastname());
      }
      if (userUpdate.getPassword() == null) {
        userUpdate.setPassword(currentUser.getPassword());
      }
      if (userUpdate.getEmail() == null) {
        userUpdate.setEmail(currentUser.getEmail());
      }

      //constructing our SQL
    String sql = "UPDATE FROM user(first_name, last_name, password, email) VALUES('"
            + currentUser.getFirstname()
            + "', '"
            + currentUser.getLastname()
            + "', '"
            + hashing.hashWithSalt(currentUser.getPassword())
            + "', '"
            + currentUser.getEmail()
            + ")";

      dbCon.updateDB(sql);

  }

}
