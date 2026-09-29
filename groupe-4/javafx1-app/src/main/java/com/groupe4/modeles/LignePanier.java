package com.groupe4.modeles;

/** Une ligne du panier : un produit et la quantité choisie. */
public record LignePanier(Produit produit, int quantite) {

    public double total() {
        return produit.prix() * quantite;
    }
}
