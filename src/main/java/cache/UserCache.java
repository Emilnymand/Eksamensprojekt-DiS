package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. - FIXED
public class UserCache {

    //List of users
    private ArrayList<User> users;

    //Time cache should live
    private long ttl;

    //Sets when the cache has been created
    private long created;

    public  UserCache(){
        this.ttl = Config.getCacheTtl();
    }

    public ArrayList<User> getUsers(Boolean forceUpdate) {

        if (forceUpdate
                //Emil - Changed greather than or equal sign, so the cache lives at least ttl/1000L if no further changes.
                //currentTimeMillis gets the exact time right now
                ||((this.created + ttl) <= (System.currentTimeMillis() / 1000L))
                || this.users.isEmpty()) {

            //Get users from controller, since we wish to update
            ArrayList<User> users = UserController.getUsers();

            //Set users for the instance and set created timestamp
            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }
        return this.users;
    }

}
