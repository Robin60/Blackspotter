package com.icrowsoft.blackspotter.my_objects;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by teardrops on 7/17/16.
 */
public class MyPointOnMap implements Serializable {

    private String _cause;
    private String _photo;
    private String _description;
    private String _name;
    private String _lat;
    private String _lon;
    private int _cases;
    private String _last_modified;
    private String _country;

    // Empty constructor
    public MyPointOnMap() {
    }

    // constructor
    public MyPointOnMap(String name, String lat, String lon, int cases, String last_modified, String description, String country, String photo, String cause) {
        this._name = name;
        this._lat = lat;
        this._lon = lon;
        this._cases = cases;
        this._last_modified = last_modified;
        this._country = country;
        this._description = description;
        this._cause = cause;
        this._photo = photo;
    }

    // getting name
    public String getName() {
        return (TextUtils.isEmpty(this._name)) ? _description : _name;
    }

    // setting name
    public void setName(String name) {
        this._name = name;
    }

    // getting latitude
    public String getLatitude() {
        return (TextUtils.isEmpty(this._lat)) ? "null" : _lat;
    }

    // setting latitude
    public void setLatitude(String lat) {
        this._lat = lat;
    }

    // getting longitude
    public String getLongitude() {
        return (TextUtils.isEmpty(this._lon)) ? "null" : _lon;
    }

    // setting latitude
    public void setLongitude(String lon) {
        this._lon = lon;
    }

    // getting cases
    public int getCases() {
        return (TextUtils.isEmpty(String.valueOf(this._cases))) ? 0 : _cases;
    }

    // setting cases
    public void setCases(int cases) {
        this._cases = cases;
    }

    // getting last_modified
    public String getLastModified() {
        return (TextUtils.isEmpty(this._last_modified)) ? "null" : _last_modified;
    }

    // setting last_modified
    public void setLastModified(String last_modified) {
        this._last_modified = last_modified;
    }

    // getting country
    public String getCountry() {
        return (TextUtils.isEmpty(this._country)) ? "null" : _country;
    }

    // setting country
    public void setCountry(String country) {
        this._country = country;
    }

    // getting description
    public String getDescription() {
        return (TextUtils.isEmpty(this._description)) ? "null" : _description;
    }

    // setting description
    public void setDescription(String description) {
        this._description = description;
    }

    // getting photo
    public String getPhoto() {
        return (TextUtils.isEmpty(this._photo)) ? "null" : _photo;
    }

    // setting photo
    public void setPhoto(String photo) {
        this._photo = photo;
    }

    // getting cause
    public String getCause() {
        return (TextUtils.isEmpty(this._cause)) ? "null" : _cause;
    }

    // setting cause
    public void setCause(String cause) {
        this._cause = cause;
    }
}