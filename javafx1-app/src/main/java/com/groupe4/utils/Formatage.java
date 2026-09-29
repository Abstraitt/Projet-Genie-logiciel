package com.groupe4.utils;

import java.util.Locale;

/** Fonctions d'affichage communes à tous les écrans. */
public class Formatage {

    // Pour changer de devise, il suffit de modifier cette ligne.
    private static final String DEVISE = "FCFA";

    public static String prix(double montant) {
        return String.format(Locale.FRANCE, "%,.0f %s", montant, DEVISE);
    }

    /** Texte de l'état du stock (écrit en toutes lettres, pas seulement une couleur). */
    public static String texteStock(int stock) {
        if (stock == 0) {
            return "Rupture de stock";
        } else if (stock <= 5) {
            return "Stock faible (" + stock + " restants)";
        }
        return "En stock";
    }

    /** Nom du style CSS (couleur) correspondant à l'état du stock. */
    public static String classeStock(int stock) {
        if (stock == 0) {
            return "stock-rupture";
        } else if (stock <= 5) {
            return "stock-faible";
        }
        return "stock-ok";
    }
}
