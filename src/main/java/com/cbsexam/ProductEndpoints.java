package com.cbsexam;

import cache.ProductCache;
import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.org.apache.regexp.internal.RE;
import controllers.ProductController;
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

@Path("product")
public class ProductEndpoints {

  ProductCache productCache = new ProductCache();
  private static UserCache userCache = new UserCache();

  /**
   * @param idProduct
   * @return Responses
   */
  @GET
  @Path("/{idProduct}")
  public Response getProduct(@PathParam("idProduct") int idProduct, String body) {

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
      Product product = ProductController.getProduct(idProduct);

      // TODO: Add Encryption to JSON : Fixed
      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(product);

      //Emil - If token doesn't match, encryption enables
      if (enableEncryption) {
        json = Encryption.encryptDecryptXOR(json);
      }

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());

      Product product = ProductController.getProduct(idProduct);
      String json = new Gson().toJson(product);

      json = Encryption.encryptDecryptXOR(json);

      return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getProducts(String body) {

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
      ArrayList<Product> products = productCache.getProducts(false);

      // TODO: Add Encryption to JSON : Fixed
      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(products);

      //Emil - If token doesn't match, encryption enables
      if (enableEncryption){
        json = Encryption.encryptDecryptXOR(json);
      }


      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    } catch (Exception e) {
      System.out.println(e.getMessage());

      ArrayList<Product> products = productCache.getProducts(false);

      String json = new Gson().toJson(products);

      json = Encryption.encryptDecryptXOR(json);

      return Response.status(400).type(MediaType.APPLICATION_JSON).entity(json).build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(String body) {

    // Read the json from body and transfer it to a product class
    Product newProduct = new Gson().fromJson(body, Product.class);

    // Use the controller to add the user
    Product createdProduct = ProductController.createProduct(newProduct);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdProduct);

    // Return the data to the user
    if (createdProduct != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }
}
