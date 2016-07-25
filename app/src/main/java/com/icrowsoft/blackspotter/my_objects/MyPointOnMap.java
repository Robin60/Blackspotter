package com.icrowsoft.blackspotter.my_objects;

/**
 * Created by teardrops on 7/17/16.
 */
public class MyPointOnMap {

    private String _description;
    private  String _name;
    private  String _lat;
    private  String _lon;
    private  int _cases;
    private  long _last_modified;
    private  String _country;

        // Empty constructor
    public MyPointOnMap(){

    }

    // constructor
    public MyPointOnMap(String name, String lat,String lon,int cases,long last_modified,String description,String country){
        this._name = name;
        this._lat = lat;
        this._lon = lon;
        this._cases = cases;
        this._last_modified = last_modified;
        this._country = country;
        this._description=description;
    }

    // constructor
    public MyPointOnMap(String lat){
        this._lat = lat;
    }

    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }

    // getting latitude
    public String getLatitude(){
        return this._lat;
    }

    // setting latitude
    public void setLatitude(String lat){
        this._lat = lat;
    }

    // getting longitude
    public String getLongitude(){
        return this._lon;
    }

    // setting latitude
    public void setLongitude(String lon){
        this._lon = lon;
    }

    // getting cases
    public int getCases(){
        return this._cases;
    }

    // setting cases
    public void setCases(int cases){
        this._cases = cases;
    }

    // getting last_modified
    public long getLastModified(){
        return this._last_modified;
    }

    // setting last_modified
    public void setLastModified(long last_modified){
        this._last_modified = last_modified;
    }

    // getting country
    public String getCountry(){
        return this._country;
    }

    // setting country
    public void setCountry(String country){
        this._country = country;
    }

    // getting description
    public String getDescription(){
        return this._description;
    }

    // setting description
    public void setDescription(String description){
        this._description = description;
    }
}