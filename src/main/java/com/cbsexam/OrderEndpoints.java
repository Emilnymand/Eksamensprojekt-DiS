package com.cbsexam;

import cache.OrderCache;
import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Order;
import model.User;
import utils.Encryption;

@Path("order")
public class OrderEndpoints {

  private static OrderCache orderCache = new OrderCache();
  private static UserCache userCache = new UserCache();

  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}")
  public Response getOrder(@PathParam("idOrder") int idOrder, String body) {

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
      for (User user : users) {
        if (jwt != null && jwt.getClaim("ID").asInt() == user.getId()) {
          //Emil - If token matches encryption disables
          enableEncryption = false;
        }
      }

      // Call our controller-layer in order to get the order from the DB
      Order order = OrderController.getOrder(idOrder);

      // TODO: Add Encryption to JSON : Fixed
      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(order);

      //Emil - If token doesn't match, encryption enables
      if (enableEncryption){
        json = Encryption.encryptDecryptXOR(json);
      }

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());

      Order order = OrderController.getOrder(idOrder);
      String json = new Gson().toJson(order);

      json = Encryption.encryptDecryptXOR(json);

      return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getOrders() {

    try {
      // Call our controller-layer in order to get the order from the DB
      ArrayList<Order> orders = orderCache.getOrders(false);

      // TODO: Add Encryption to JSON : Fixed
      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(orders);
      //Emil - added encryption
      json = Encryption.encryptDecryptXOR(json);

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return Response.status(400).entity("Couldn't get all orders").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the user
    Order createdOrder = OrderController.createOrder(newOrder);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(400).entity("Could not create user").build();
    }
  }
}