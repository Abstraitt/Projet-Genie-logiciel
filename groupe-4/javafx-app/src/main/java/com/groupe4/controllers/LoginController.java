package com.groupe4.controllers;

import com.groupe4.services.AuthService;
import com.groupe4.services.AuthService.ResultatConnexion;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Contrôleur de l'écran de connexion (LoginView.fxml).
 * Chaque champ fx:id du fichier FXML correspond à une variable ici.
 */
public class LoginController {

    @FXML
    private TextField champEmail;

    @FXML
    private PasswordField champMotDePasse;

    @FXML
    private Label messageErreur;

    @FXML
    private Button boutonConnexion;

    @FXML
    private Hyperlink lienInscription;

    private final AuthService authService = new AuthService();

    /**
     * Appelée automatiquement quand l'utilisateur clique sur "Se connecter".
     */
    @FXML
    private void seConnecter() {
        String email = champEmail.getText().trim();
        String motDePasse = champMotDePasse.getText();

        cacherErreur();

        if (email.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Merci de remplir l'email et le mot de passe.");
            return;
        }

        // On désactive le bouton le temps de l'appel, pour éviter les doubles clics.
        boutonConnexion.setDisable(true);
        boutonConnexion.setText("Connexion en cours...");

        // Appel au module du Groupe 1 (authentification), via AuthService.
        ResultatConnexion resultat = authService.connecter(email, motDePasse);

        boutonConnexion.setDisable(false);
        boutonConnexion.setText("Se connecter");

        if (resultat.succes()) {
            // TODO : quand les autres écrans seront prêts, on ouvrira ici le catalogue.
            System.out.println("Connexion réussie, jeton reçu : " + resultat.jeton());
        } else {
            afficherErreur(resultat.messageErreur());
        }
    }

    /**
     * Appelée quand l'utilisateur clique sur "Créer un compte".
     */
    @FXML
    private void allerVersInscription() {
        // TODO : ouvrir l'écran d'inscription une fois qu'il existera.
        System.out.println("Ouverture de l'écran d'inscription (à venir).");
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }

    private void cacherErreur() {
        messageErreur.setVisible(false);
        messageErreur.setManaged(false);
    }
}
