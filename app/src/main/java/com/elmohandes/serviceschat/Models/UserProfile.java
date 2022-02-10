package com.elmohandes.serviceschat.Models;

public class UserProfile {

    String full_name , bio , image ;

    public UserProfile(){

    }

    public UserProfile(String full_name, String bio, String image) {
        this.full_name = full_name;
        this.bio = bio;
        this.image = image;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
