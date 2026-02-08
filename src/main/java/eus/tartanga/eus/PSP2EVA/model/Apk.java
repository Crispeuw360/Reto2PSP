package eus.tartanga.eus.PSP2EVA.model;

public class Apk {

    private int id;
    private String nombre;
    private String apkUrl;
    private String imageUrl;
    private String downloadHash;
    private String descripcion;
    private int rating;

    public Apk() {
    }

    public Apk(int id, String nombre, String apkUrl, String imageUrl, String downloadHash,String descripcion,int rating) {
        this.id = id;
        this.nombre = nombre;
        this.apkUrl = apkUrl;
        this.imageUrl = imageUrl;
        this.downloadHash = downloadHash;
        this.descripcion = descripcion;
        this.rating = rating;
    }

    public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "Apk [id=" + id + ", nombre=" + nombre + ", apkUrl=" + apkUrl + ", imageUrl=" + imageUrl
				+ ", downloadHash=" + downloadHash + ", descripcion=" + descripcion + ", rating=" + rating + "]";
	}

    
}

