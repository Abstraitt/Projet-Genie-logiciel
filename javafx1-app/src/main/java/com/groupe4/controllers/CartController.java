package com.groupe4.controllers;

import com.groupe4.modeles.LignePanier;
import com.groupe4.modeles.Produit;
import com.groupe4.utils.Formatage;
import com.groupe4.utils.Navigation;
import com.groupe4.utils.Panier;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Contrôleur de l'écran du panier (CartView.fxml).
 * Lit le contenu de Panier et permet de changer les quantités ou de retirer un produit.
 */
public class CartController {

    @FXML
    private VBox zoneLignes;
    @FXML
    private Label messageVide;
    @FXML
    private Label labelTotal;
    @FXML
    private Button boutonCommander;

    /** Appelée automatiquement à l'ouverture de l'écran. */
    @FXML
    private void initialize() {
        charger();
    }

    @FXML
    private void continuerAchats() {
        Navigation.afficher("/vues/CatalogueView.fxml", "Catalogue", 960, 680);
    }

    @FXML
    private void passerCommande() {
        // TODO : ouvrir le tunnel de commande quand cet écran existera.
        System.out.println("Passage de la commande (à venir).");
    }

    /** Remplit l'écran avec le contenu actuel du panier. */
    private void charger() {
        zoneLignes.getChildren().clear();
        for (LignePanier ligne : Panier.lignes()) {
            zoneLignes.getChildren().add(creerLigne(ligne));
        }

        boolean vide = Panier.lignes().isEmpty();
        messageVide.setVisible(vide);
        messageVide.setManaged(vide);
        boutonCommander.setDisable(vide);
        mettreAJourTotal();
    }

    /** Fabrique une ligne : nom, prix unitaire, quantité, total de la ligne, bouton Retirer. */
    private HBox creerLigne(LignePanier ligne) {
        Produit produit = ligne.produit();

        Label nom = new Label(produit.nom());
        nom.getStyleClass().add("carte-nom");
        nom.setWrapText(true);
        nom.setPrefWidth(260);
        HBox.setHgrow(nom, Priority.ALWAYS);

        Label prixUnitaire = new Label(Formatage.prix(produit.prix()));
        prixUnitaire.setPrefWidth(110);

        Spinner<Integer> quantite = new Spinner<>();
        quantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, produit.stock(), ligne.quantite()));
        quantite.setEditable(false);
        quantite.setPrefWidth(90);
        quantite.setAccessibleText("Quantité de " + produit.nom());

        Label totalLigne = new Label(Formatage.prix(produit.prix() * ligne.quantite()));
        totalLigne.getStyleClass().add("carte-prix");
        totalLigne.setPrefWidth(120);
        totalLigne.setAlignment(Pos.CENTER_RIGHT);

        // Quand la quantité change : on met à jour le panier, le total de la ligne et le total général.
        quantite.valueProperty().addListener((obs, ancienne, nouvelle) -> {
            Panier.modifierQuantite(produit.id(), nouvelle);
            totalLigne.setText(Formatage.prix(produit.prix() * nouvelle));
            mettreAJourTotal();
        });

        Button retirer = new Button("Retirer");
        retirer.setAccessibleText("Retirer " + produit.nom() + " du panier");
        retirer.setOnAction(e -> {
            Panier.retirer(produit.id());
            charger();
        });

        HBox ligneBox = new HBox(16, nom, prixUnitaire, quantite, totalLigne, retirer);
        ligneBox.setAlignment(Pos.CENTER_LEFT);
        ligneBox.getStyleClass().add("ligne-panier");
        return ligneBox;
    }

    private void mettreAJourTotal() {
        labelTotal.setText("Total : " + Formatage.prix(Panier.total()));
    }
}
