/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

/**
 *
 * @author Raouf
 */
public class vente {
    
    private int idVente;
    private String nomProduit;
    private String typeProduit;
    private int quant;
     private int prixVente;
    private int idSession;

    public vente(int idVente, String nomProduit, String typeProduit, int quant, int prixVente, int idSession) {
        this.idVente = idVente;
        this.nomProduit = nomProduit;
        this.typeProduit = typeProduit;
        this.quant = quant;
        this.prixVente = prixVente;
        this.idSession = idSession;
    }

    
    
    
    public int getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(int prixVente) {
        this.prixVente = prixVente;
    }


    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getTypeProduit() {
        return typeProduit;
    }

    public void setTypeProduit(String typeProduit) {
        this.typeProduit = typeProduit;
    }

    public int getQuant() {
        return quant;
    }

    public void setQuant(int quant) {
        this.quant = quant;
    }

    public int getIdSession() {
        return idSession;
    }

    public void setIdSession(int idSession) {
        this.idSession = idSession;
    }
    
    
    
}


