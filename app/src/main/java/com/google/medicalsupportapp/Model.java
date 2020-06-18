package com.google.medicalsupportapp;

public class Model  {

    String Meno;
    String Priezvisko;
    String Adresa;
    String Region;
    String Mobil;
    String Tel;
    Integer odbor;
    String UID;

    public Model() {
    }

    public Model(String meno) {
        Meno = meno;
    }

    public String getMeno() {
        return Meno;
    }

    public void setMeno(String meno) {
        Meno = meno;
    }

    public String getPriezvisko() {
        return Priezvisko;
    }

    public void setPriezvisko(String priezvisko) {
        Priezvisko = priezvisko;
    }

    public String getAdresa() {
        return Adresa;
    }

    public void setAdresa(String adresa) {
        Adresa = adresa;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public String getMobil() {
        return Mobil;
    }

    public void setMobil(String mobil) {
        Mobil = mobil;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String tel) {
        Tel = tel;
    }

    public Integer getOdbor() {
        return odbor;
    }

    public void setOdbor(Integer odbor) {
        this.odbor = odbor;
    }

    public String getUid() {
        return UID;
    }

    public void setUid(String UID) {
        this.UID = UID;
    }
}
