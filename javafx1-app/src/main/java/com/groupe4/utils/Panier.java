package com.groupe4.utils;

import com.groupe4.modeles.LignePanier;
import com.groupe4.modeles.Produit;

import java.util.ArrayList;
import java.util.List;

/**
 * Panier gardé en mémoire pendant que l'application est ouverte.
 * SIMULÉ en attendant l'API du Groupe 3 : plus tard, chaque méthode
 * appellera leur module au lieu de modifier cette simple liste.
 */
public class Panier {

    private static final List<LignePanier> lignes = new ArrayList<>();

    /**
     * Ajoute un produit. Si la quantité totale dépasse le stock, elle est limitée au stock.
     * Renvoie false quand la quantité a dû être limitée.
     */
    public static boolean ajouter(Produit produit, int quantite) {
        for (int i = 0; i < lignes.size(); i++) {
            LignePanier ligne = lignes.get(i);
            if (ligne.produit().id() == produit.id()) {
                int nouvelle = ligne.quantite() + quantite;
                boolean complet = nouvelle <= produit.stock();
                lignes.set(i, new LignePanier(produit, Math.min(nouvelle, produit.stock())));
                return complet;
            }
        }
        lignes.add(new LignePanier(produit, Math.min(quantite, produit.stock())));
        return quantite <= produit.stock();
    }

    /** Change la quantité d'un produit déjà dans le panier (entre 1 et le stock). */
    public static void modifierQuantite(int produitId, int quantite) {
        for (int i = 0; i < lignes.size(); i++) {
            LignePanier ligne = lignes.get(i);
            if (ligne.produit().id() == produitId) {
                int valide = Math.max(1, Math.min(quantite, ligne.produit().stock()));
                lignes.set(i, new LignePanier(ligne.produit(), valide));
                return;
            }
        }
    }

    public static void retirer(int produitId) {
        lignes.removeIf(l -> l.produit().id() == produitId);
    }

    public static int quantiteDe(Produit produit) {
        return lignes.stream()
                .filter(l -> l.produit().id() == produit.id())
                .mapToInt(LignePanier::quantite)
                .sum();
    }

    /** Nombre total d'articles (en comptant les quantités). */
    public static int nombreArticles() {
        return lignes.stream().mapToInt(LignePanier::quantite).sum();
    }

    public static List<LignePanier> lignes() {
        return List.copyOf(lignes);
    }

    public static double total() {
        return lignes.stream().mapToDouble(LignePanier::total).sum();
    }
}
