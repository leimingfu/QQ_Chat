package com.example.Fragment;

public class Result {
    public int id;
    public String user;
    public String word;

    public int getid() {
        return id;
    }

    public void setid(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}
