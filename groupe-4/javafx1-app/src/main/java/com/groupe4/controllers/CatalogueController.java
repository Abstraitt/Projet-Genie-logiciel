package com.groupe4.controllers;

import com.groupe4.modeles.Produit;
import com.groupe4.services.CatalogueService;
import com.groupe4.services.CatalogueService.PageProduits;
import com.groupe4.utils.Formatage;
import com.groupe4.utils.Navigation;
import com.groupe4.utils.Panier;
import com.groupe4.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Contrôleur de l'écran du catalogue (CatalogueView.fxml).
 * Affiche les produits par pages de 6, avec recherche et filtre par catégorie.
 */
public class CatalogueController {

    private static final int TAILLE_PAGE = 6;
    private static final String TOUTES = "Toutes les catégories";

    @FXML
    private TextField champRecherche;
    @FXML
    private ComboBox<String> comboCategorie;
    @FXML
    private FlowPane zoneProduits;
    @FXML
    private Label messageVide;
    @FXML
    private Label labelPage;
    @FXML
    private Button boutonPrecedent;
    @FXML
    private Button boutonSuivant;
    @FXML
    private Hyperlink lienPanier;

    private final CatalogueService service = new CatalogueService();
    private int pageCourante = 0;

    /** Appelée automatiquement à l'ouverture de l'écran. */
    @FXML
    private void initialize() {
        comboCategorie.getItems().add(TOUTES);
        comboCategorie.getItems().addAll(service.categories());
        comboCategorie.setValue(TOUTES);

        lienPanier.setText("Mon panier (" + Panier.nombreArticles() + ")");

        // Appuyer sur Entrée dans le champ de recherche lance la recherche.
        champRecherche.setOnAction(e -> rechercher());
        // Changer de catégorie relance aussi la recherche.
        comboCategorie.setOnAction(e -> rechercher());

        charger();
    }

    @FXML
    private void rechercher() {
        pageCourante = 0;
        charger();
    }

    @FXML
    private void pagePrecedente() {
        pageCourante--;
        charger();
    }

    @FXML
    private void pageSuivante() {
        pageCourante++;
        charger();
    }

    @FXML
    private void ouvrirPanier() {
        Navigation.afficher("/vues/CartView.fxml", "Panier", 960, 680);
    }

    @FXML
    private void seDeconnecter() {
        Session.deconnecter();
        Navigation.afficher("/vues/LoginView.fxml", "Connexion");
    }

    /** Demande les produits au service et remplit la grille. */
    private void charger() {
        String categorie = TOUTES.equals(comboCategorie.getValue()) ? null : comboCategorie.getValue();
        PageProduits page = service.rechercher(champRecherche.getText().trim(), categorie, pageCourante, TAILLE_PAGE);
        pageCourante = page.page();

        zoneProduits.getChildren().clear();
        for (Produit produit : page.produits()) {
            zoneProduits.getChildren().add(creerCarte(produit));
        }

        boolean vide = page.produits().isEmpty();
        messageVide.setVisible(vide);
        messageVide.setManaged(vide);

        labelPage.setText("Page " + (page.page() + 1) + " sur " + Math.max(1, page.totalPages()));
        boutonPrecedent.setDisable(page.page() <= 0);
        boutonSuivant.setDisable(page.page() >= page.totalPages() - 1);
    }

    /** Fabrique la petite carte d'un produit : nom, catégorie, prix, état du stock, bouton. */
    private VBox creerCarte(Produit produit) {
        Label nom = new Label(produit.nom());
        nom.getStyleClass().add("carte-nom");
        nom.setWrapText(true);

        Label categorie = new Label(produit.categorie());
        categorie.getStyleClass().add("carte-categorie");

        Label prix = new Label(Formatage.prix(produit.prix()));
        prix.getStyleClass().add("carte-prix");

        // L'état du stock est écrit en toutes lettres (pas seulement une couleur),
        // pour rester lisible par tout le monde.
        Label stock = new Label(Formatage.texteStock(produit.stock()));
        stock.getStyleClass().add(Formatage.classeStock(produit.stock()));

        Button detail = new Button("Voir le détail");
        detail.setOnAction(e -> {
            ProductController fiche = Navigation.afficherAvecControleur(
                    "/vues/ProductView.fxml", "Fiche produit", 960, 680);
            fiche.setProduit(produit);
        });

        VBox carte = new VBox(6, nom, categorie, prix, stock, detail);
        carte.getStyleClass().add("carte-produit");
        carte.setPrefWidth(200);
        return carte;
    }
}
