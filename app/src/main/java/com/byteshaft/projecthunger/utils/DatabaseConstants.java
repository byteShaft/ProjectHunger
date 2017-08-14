package com.byteshaft.projecthunger.utils;

/**
 * Created by fi8er1 on 23/11/2016.
 */
class DatabaseConstants {

    static final String DATABASE_NAME = "Database.db";
    static final int DATABASE_VERSION = 1;
    static final String TABLE_NAME = "FavoritesDatabase";
    static final String ID = "id";
    static final String TYPE = "type";
    static final String NAME = "name";
    static final String LOCATION = "location";
    static final String FORMATTED_ADDRESS = "formatted_address";
    static final String TIMINGS = "timings";
    static final String RATING = "rating";
    static final String NUMBER_OF_RATINGS = "number_of_ratings";
    static final String CONTACT_NUMBER = "contact_number";
    static final String FOOD_MENU = "food_menu";

    private static final String OPENING_BRACE = "(";
    private static final String CLOSING_BRACE = ")";

    static final String TABLE_CREATE = "CREATE TABLE "
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