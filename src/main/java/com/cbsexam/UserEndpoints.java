package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.ProductController;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.Product;
import model.User;
import utils.Encryption;
import utils.Hashing;
import utils.Log;

@Path("user")
public class UserEndpoints {

  private static UserCache userCache = new UserCache();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser, String body) {

    try {
      //Emil - Validating the user to make sure the data isn't readable for everyone
      User validateUser = new Gson().fromJson(body, User.class);

      //Emil - Saving decoded token in jwt
      DecodedJWT jwt = JWT.decode(validateUser.getToken());

      //Emil - Adding a boolean to use further down
      boolean enableEncryption = true;

      //Emil -  Get a list of users from UserCache
      ArrayList<User> users = userCache.getUsers(false);

      //Emil - Checking that token matches
      for (User user : users){
        if (jwt!= null && jwt.getClaim("ID").asInt() == user.getId()){
          //Emil - If token matches encryption disables
          enableEncryption = false;
        }
      }
      // Use the ID to get the user from the controller.
      User user = UserController.getUser(idUser);

      // TODO: Add Encryption to JSON : Fixed
      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(user);

      //Emil - If token doesn't match, encryption enables
      if (enableEncryption) {
        json = Encryption.encryptDecryptXOR(json);
      }

      // Return the user with the status code 200
      // TODO: What should happen if something breaks down? - FIXED
      //Emil - Throws exception
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());

      User user = UserController.getUser(idUser);
      String json = new Gson().toJson(user);

      json = Encryption.encryptDecryptXOR(json);

      return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getUsers(String body) {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    try {
      //Emil -  Get a list of users from UserCache
      ArrayList<User> users = userCache.getUsers(false);

      // TODO: Add Encryption to JSON : Fixed
      // Transfer users to json in order to return it to the user
      String json = new Gson().toJson(users);

      json = Encryption.encryptDecryptXOR(json);

      // Return the users with the status code 200
      return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return Response.status(400).entity("Couldn't get all users").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. Fixed
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    User userSignedIn = new Gson().fromJson(body, User.class);
    Hashing hashing = new Hashing();

    //Emil - Getting
    User userInDB = UserController.getUserByEmail(userSignedIn.getEmail());
    String json = new Gson().toJson(userInDB);

    //Emil - Generating unique timestamp
    hashing.generateSalt(String.valueOf(userInDB.getCreatedTime()));

    //Emil - Validating email and password
    if (userInDB.getEmail() != null && userSignedIn.getEmail().equals(userInDB.getEmail()) && hashing.hashWithSalt(userSignedIn.getPassword()).equals((userInDB.getPassword()))) {

      // Return a response with status 200 and JSON as type
      return Response.status(200).entity("Login succesfull" + json).build();
    } else {
      return Response.status(400).entity("Login failed. Try again").build();
    }
  }

  // TODO: Make the system able to delete users - Fixed
  @POST
  @Path("/delete/{delete}")
  public Response deleteUser(String userDelete) {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(),userDelete, "Ready to delete user", 0);

    try {
      User userToDelete = new Gson().fromJson(userDelete, User.class);

      //Emil - Saving decoded token in jwt
      DecodedJWT jwt = JWT.decode(userToDelete.getToken());

      // Get a list of users from UserCache
      ArrayList<User> users = userCache.getUsers(false);

      for (User user : users) {
        //Emil - Checking that the user is who he/she claims to be
        if (jwt !=null && jwt.getClaim("ID").asInt() == user.getId()) {

          //Emil - Actually deleting user
          UserController.deleteUser(user.getId());

          //Emil - Makes sure to update in DB
          userCache.getUsers(true);

          // Return a response with status 200 and JSON as type
          return Response.status(200).entity("User with specified ID " + user.getId() + " has been deleted").build();
      }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return Response.status(400).entity("Delete of user failed").build();
    }

    //Emil - If the user doesn't exist
    return null;
  }

  @POST
  @Path("/update/")
  @Consumes(MediaType.APPLICATION_JSON)
  // TODO: Make the system able to update users - Fixed
  public Response updateUser(String userUpdate) {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), userUpdate, "Ready to update user", 0);

    try {
      User userToUpdate = new Gson().fromJson(userUpdate, User.class);

      //Emil - Saving decoded token in jwt
      DecodedJWT jwt = JWT.decode(userToUpdate.getToken());

      // Get a list of users from UserCache
      ArrayList<User> users = userCache.getUsers(false);

      for (User user : users) {
        //Emil - Checking that the user is who he/she claims to be
        if (jwt != null && jwt.getClaim("ID").asInt() == user.getId()) {

          //Emil - Actually updating user
          UserController.updateUser(user.getId(), userToUpdate);

          //Emil - Makes sure to update in DB
          userCache.getUsers(true);

          // Return a response with status 200 and JSON as type
          return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User with specified ID " + user.getId() + " has been succesfully updated").build();
        }
        }
      } catch(Exception e){
        System.out.println(e.getMessage());
        return Response.status(400).entity("User update failed because of missing token").build();
      }
      return null;
    }
}
