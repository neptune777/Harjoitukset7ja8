package com.example.android.harjoitus.data;

public class Aku {

    private String id;
    private String nimi;
    private String numero;
    private String painos;
    private String hankintaPvm;

    public Aku(String id, String nimi, String numero, String hankintaPvm, String painos) {
        this.id=id;
        this.nimi=nimi;
        this.numero=numero;
        this.painos=painos;
        this.hankintaPvm=hankintaPvm;
    }

    public Aku(String nimi, String numero, String hankintaPvm, String painos) {
        this.nimi=nimi;
        this.numero=numero;
        this.painos=painos;
        this.hankintaPvm=hankintaPvm;
    }

    public void setId(String id){
        this.id=id;
    }
    public void setNimi(String nimi){
        this.nimi=nimi;
    }
    public void setNumero(String numero){
        this.numero=numero;
    }
    public void setPainos(String painos){
        this.painos=painos;
    }
    public void setHankintaPvm(String hankintaPvm){
        this.hankintaPvm=hankintaPvm;
    }

    public String getId(){
        return this.id;
    }
    public String getNimi(){
        return this.nimi;
    }
    public String getNumero(){
        return this.numero;
    }
    public String getPainos(){
        return this.painos;
    }
    public String getHankintaPvm(){
        return this.hankintaPvm;
    }

}
