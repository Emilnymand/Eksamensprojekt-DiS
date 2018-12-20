package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. - FIXED
public class OrderCache {

    private ArrayList<Order> orders;

    private long ttl;

    private long created;

    public OrderCache() {
        this.ttl = Config.getCacheTtl();
    }

    public ArrayList<Order> getOrders(boolean forceUpdate) {

        if (forceUpdate
                //Emil - Changed greather than or equal sign, so the cache lives at least ttl/1000L if no further changes.
                //currentTimeMillis gets the exact time right now
                || ((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L))
                || this.orders.isEmpty()) {

            ArrayList<Order> orders = OrderController.getOrders();

            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.orders;
    }
}
