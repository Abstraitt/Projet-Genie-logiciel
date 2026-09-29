package com.groupe4.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Sert à changer d'écran dans la même fenêtre.
 * Exemple : Navigation.afficher("/vues/RegisterView.fxml", "Inscription");
 */
public class Navigation {

    private static Stage fenetre;

    public static void initialiser(Stage fenetrePrincipale) {
        fenetre = fenetrePrincipale;
    }

    /** Écran de taille standard (utilisé pour la connexion et l'inscription). */
    public static void afficher(String cheminVue, String titre) {
        afficher(cheminVue, titre, 480, 640);
    }

    /** Écran avec une taille de fenêtre précise (utilisé pour le catalogue, plus large). */
    public static void afficher(String cheminVue, String titre, double largeur, double hauteur) {
        afficherAvecControleur(cheminVue, titre, largeur, hauteur);
    }

    /**
     * Comme afficher(), mais renvoie le contrôleur de l'écran ouvert.
     * Ça permet de lui transmettre des données (par exemple le produit choisi).
     */
    public static <T> T afficherAvecControleur(String cheminVue, String titre, double largeur, double hauteur) {
        try {
            FXMLLoader chargeur = new FXMLLoader(Navigation.class.getResource(cheminVue));
            Parent racine = chargeur.load();
            Scene scene = new Scene(racine, largeur, hauteur);
            scene.getStylesheets().add(Navigation.class.getResource("/styles/app.css").toExternalForm());
            fenetre.setTitle("Plateforme e-commerce — " + titre);
            fenetre.setScene(scene);
            fenetre.centerOnScreen();
            return chargeur.getController();
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir l'écran : " + cheminVue, e);
        }
    }
}
