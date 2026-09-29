package com.groupe4.controllers;

import com.groupe4.modeles.Produit;
import com.groupe4.utils.Formatage;
import com.groupe4.utils.Navigation;
import com.groupe4.utils.Panier;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.StackPane;

/**
 * Contrôleur de la fiche produit (ProductView.fxml).
 * Le catalogue appelle setProduit() juste après avoir ouvert cet écran.
 */
public class ProductController {

    @FXML
    private StackPane zoneImage;
    @FXML
    private Label labelNom;
    @FXML
    private Label labelCategorie;
    @FXML
    private Label labelPrix;
    @FXML
    private Label labelStock;
    @FXML
    private Label labelDescription;
    @FXML
    private Spinner<Integer> spinnerQuantite;
    @FXML
    private Button boutonAjouter;
    @FXML
    private Label messageConfirmation;

    private Produit produit;

    /** Reçoit le produit choisi dans le catalogue et remplit l'écran. */
    public void setProduit(Produit produit) {
        this.produit = produit;

        labelNom.setText(produit.nom());
        labelCategorie.setText(produit.categorie());
        labelPrix.setText(Formatage.prix(produit.prix()));
        labelDescription.setText(produit.description());
        labelStock.setText(Formatage.texteStock(produit.stock()));
        labelStock.getStyleClass().add(Formatage.classeStock(produit.stock()));

        // Texte lu par les lecteurs d'écran, puisque l'image est pour l'instant un carré gris.
        zoneImage.setAccessibleText("Image du produit " + produit.nom());

        if (produit.stock() == 0) {
            // Produit indisponible : on ne peut ni choisir de quantité ni ajouter au panier.
            spinnerQuantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            spinnerQuantite.setDisable(true);
            boutonAjouter.setDisable(true);
            boutonAjouter.setText("Produit indisponible");
        } else {
            spinnerQuantite.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, produit.stock(), 1));
        }
    }

    @FXML
    private void ajouterAuPanier() {
        int quantite = spinnerQuantite.getValue();
        boolean complet = Panier.ajouter(produit, quantite);

        if (complet) {
            afficherConfirmation(quantite + " × " + produit.nom() + " ajouté(s) au panier.");
        } else {
            afficherConfirmation("Quantité limitée au stock disponible : "
                    + Panier.quantiteDe(produit) + " dans le panier.");
        }
    }

    @FXML
    private void voirPanier() {
        Navigation.afficher("/vues/CartView.fxml", "Panier", 960, 680);
    }

    @FXML
    private void retourCatalogue() {
        Navigation.afficher("/vues/CatalogueView.fxml", "Catalogue", 960, 680);
    }

    private void afficherConfirmation(String texte) {
        messageConfirmation.setText(texte);
        messageConfirmation.setVisible(true);
        messageConfirmation.setManaged(true);
    }
}
