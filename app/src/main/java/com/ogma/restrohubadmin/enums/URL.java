package com.ogma.restrohubadmin.enums;

/**
 * Created by alokdas on 11/08/15.
 */
public enum URL {

    /*LOGIN("login"),*/
    LOGIN("login_restaurant_user"),
    ORDER_LIST("order_list"),
    CHANGE_ORDER_ITEM_STATUS("change_order_item_status"),
    CREATE_CATEGORY("create_restaurant_category"),
    CATEGORY_LIST("category_list"),
    CREATE_MENU("create_menu"),
    MENU_LIST("menu_list"),
    CUSTOMER_LIST("customer_list"),
    CREATE_RESTAURANT_CUSTOMER("create_restaurant_customer"),
    TABLE_LIST("table_list"),
    CREATE_RESTAURANT_TABLE("create_restaurant_table"),
    EDIT_CATEGORY("edit_category"),
    EDIT_MENU("edit_menu");

    public String BASE_URL = "http://ogmaconceptions.com/demo/restaurant/admin_restro_hubapp/";

    public String mURL;

    URL(String mURL) {
        this.mURL = this.BASE_URL + mURL;
    }

    public String getURL() {
        return mURL;
    }

}
