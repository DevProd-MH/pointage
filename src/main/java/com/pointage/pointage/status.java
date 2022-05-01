package com.pointage.pointage;

public class status {
    String id, date_pres, presence;

    public status(String id, String date_pres, String presence) {
        this.id = id;
        this.date_pres = date_pres;
        this.presence = presence;
    }

    public String toString() {
        return id + "," + date_pres + "," + presence;
    }
}
