package com.groupe4.controllers;

import com.groupe4.services.AuthService;
import com.groupe4.services.AuthService.ResultatInscription;
import com.groupe4.utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Contrôleur de l'écran d'inscription (RegisterView.fxml).
 * Les règles de mot de passe suivent celles du Groupe 1 :
 * 12 caractères minimum, majuscule, minuscule, chiffre, caractère spécial.
 */
public class RegisterController {

    @FXML
    private TextField champNom;
    @FXML
    private TextField champEmail;
    @FXML
    private PasswordField champMotDePasse;
    @FXML
    private PasswordField champConfirmation;
    @FXML
    private Label messageErreur;
    @FXML
    private Label messageSucces;
    @FXML
    private Button boutonInscription;

    private final AuthService authService = new AuthService();

    @FXML
    private void sInscrire() {
        cacherMessages();

        String nom = champNom.getText().trim();
        String email = champEmail.getText().trim();
        String motDePasse = champMotDePasse.getText();
        String confirmation = champConfirmation.getText();

        if (nom.isEmpty() || email.isEmpty() || motDePasse.isEmpty() || confirmation.isEmpty()) {
            afficherErreur("Merci de remplir tous les champs.");
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            afficherErreur("L'adresse email n'est pas valide.");
            return;
        }
        if (!motDePasseValide(motDePasse)) {
            afficherErreur("Le mot de passe doit contenir au moins 12 caractères, avec une majuscule, "
                    + "une minuscule, un chiffre et un caractère spécial.");
            return;
        }
        if (!motDePasse.equals(confirmation)) {
            afficherErreur("Les deux mots de passe ne sont pas identiques.");
            return;
        }

        boutonInscription.setDisable(true);
        ResultatInscription resultat = authService.inscrire(nom, email, motDePasse);
        boutonInscription.setDisable(false);

        if (resultat.succes()) {
            afficherSucces("Compte créé. Un email de confirmation vous a été envoyé.");
        } else {
            afficherErreur(resultat.messageErreur());
        }
    }

    @FXML
    private void retourConnexion() {
        Navigation.afficher("/vues/LoginView.fxml", "Connexion");
    }

    private boolean motDePasseValide(String mdp) {
        return mdp.length() >= 12
                && mdp.matches(".*[A-Z].*")
                && mdp.matches(".*[a-z].*")
                && mdp.matches(".*\\d.*")
                && mdp.matches(".*[^A-Za-z0-9].*");
    }

    private void afficherErreur(String texte) {
        messageErreur.setText(texte);
        messageErreur.setVisible(true);
        messageErreur.setManaged(true);
    }

    private void afficherSucces(String texte) {
        messageSucces.setText(texte);
        messageSucces.setVisible(true);
        messageSucces.setManaged(true);
    }

    private void cacherMessages() {
        messageErreur.setVisible(false);
        messageErreur.setManaged(false);
        messageSucces.setVisible(false);
        messageSucces.setManaged(false);
    }
}
