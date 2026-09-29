package com.groupe4.modeles;

/** Un produit du catalogue (données fournies par le module du Groupe 2). */
public record Produit(int id, String nom, String categorie, String description, double prix, int stock) {
}
