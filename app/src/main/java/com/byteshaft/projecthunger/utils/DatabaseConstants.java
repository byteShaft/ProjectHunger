package com.byteshaft.projecthunger.utils;

/**
 * Created by fi8er1 on 23/11/2016.
 */
public class DatabaseConstants {

    public static final String DATABASE_NAME = "Database.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "FavoritesDatabase";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String LOCATION = "location";
    public static final String FORMATTED_ADDRESS = "formatted_address";
    public static final String TIMINGS = "timings";
    public static final String RATING = "rating";
    public static final String NUMBER_OF_RATINGS = "number_of_ratings";
    public static final String CONTACT_NUMBER = "contact_number";
    public static final String FOOD_MENU = "food_menu";

    private static final String OPENING_BRACE = "(";
    private static final String CLOSING_BRACE = ")";

    public static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + OPENING_BRACE
            + ID + " TEXT,"
            + TYPE + " TEXT,"
            + NAME + " TEXT,"
            + TIMINGS + " TEXT,"
            + RATING + " TEXT,"
            + NUMBER_OF_RATINGS + " TEXT,"
            + LOCATION + " TEXT,"
            + FORMATTED_ADDRESS + " TEXT,"
            + CONTACT_NUMBER + " TEXT,"
            + FOOD_MENU + " TEXT"
            + CLOSING_BRACE;
}