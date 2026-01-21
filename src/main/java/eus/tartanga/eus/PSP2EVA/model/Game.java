package eus.tartanga.eus.PSP2EVA.model;

public class Game {

    private int id;
    private String nombre;
    private String apkUrl;
    private String imageUrl;
    private String downloadHash;

    public Game() {
    }

    public Game(int id, String nombre, String apkUrl, String imageUrl, String downloadHash) {
        this.id = id;
        this.nombre = nombre;
        this.apkUrl = apkUrl;
        this.imageUrl = imageUrl;
        this.downloadHash = downloadHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDownloadHash() {
        return downloadHash;
    }

    public void setDownloadHash(String downloadHash) {
        this.downloadHash = downloadHash;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apkUrl='" + apkUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", downloadHash='" + downloadHash + '\'' +
                '}';
    }
}

