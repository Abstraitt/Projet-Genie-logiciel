package com.groupe4.services;

import com.groupe4.modeles.Produit;

import java.util.ArrayList;
import java.util.List;

/**
 * Communication avec le module Catalogue (Groupe 2).
 * Pour l'instant les produits sont SIMULÉS : quand l'API du Groupe 2 sera
 * branchée, on remplacera le contenu de rechercher() et categories().
 */
public class CatalogueService {

    private final List<Produit> produits = List.of(
            new Produit(1, "Ordinateur portable 15 pouces", "Informatique", "Portable polyvalent pour le travail et les études.", 350000, 12),
            new Produit(2, "Souris sans fil", "Accessoires", "Souris ergonomique, connexion USB.", 8500, 40),
            new Produit(3, "Clavier mécanique", "Accessoires", "Clavier AZERTY rétroéclairé.", 25000, 5),
            new Produit(4, "Smartphone 128 Go", "Téléphonie", "Écran 6,5 pouces, double SIM.", 120000, 18),
            new Produit(5, "Chargeur rapide 30 W", "Téléphonie", "Compatible USB-C.", 6000, 0),
            new Produit(6, "Écran 24 pouces", "Informatique", "Écran Full HD pour poste de travail.", 95000, 7),
            new Produit(7, "Ventilateur de table", "Maison", "Trois vitesses, silencieux.", 15000, 25),
            new Produit(8, "Lampe de bureau LED", "Maison", "Éclairage réglable.", 9000, 3),
            new Produit(9, "Casque audio", "Accessoires", "Casque fermé avec microphone.", 18000, 14),
            new Produit(10, "Disque dur externe 1 To", "Informatique", "Stockage portable USB 3.0.", 45000, 9),
            new Produit(11, "Coque de protection", "Téléphonie", "Coque antichoc.", 3500, 60),
            new Produit(12, "Bouilloire électrique", "Maison", "Capacité 1,7 litre.", 12000, 0),
            new Produit(13, "Tablette 10 pouces", "Informatique", "Idéale pour la lecture et la vidéo.", 140000, 6),
            new Produit(14, "Multiprise parafoudre", "Maison", "Cinq prises avec protection.", 7500, 30)
    );

    public List<String> categories() {
        return produits.stream().map(Produit::categorie).distinct().sorted().toList();
    }

    /** Un résultat de recherche : les produits d'UNE page, plus le nombre total de pages. */
    public record PageProduits(List<Produit> produits, int page, int totalPages) {
    }

    /**
     * Cherche les produits dont le nom contient le texte, dans la catégorie choisie
     * (categorie = null pour toutes), et renvoie seulement la page demandée.
     */
    public PageProduits rechercher(String texte, String categorie, int page, int taillePage) {
        List<Produit> resultat = new ArrayList<>();
        for (Produit p : produits) {
            boolean texteOk = texte.isEmpty() || p.nom().toLowerCase().contains(texte.toLowerCase());
            boolean categorieOk = categorie == null || p.categorie().equals(categorie);
            if (texteOk && categorieOk) {
                resultat.add(p);
            }
        }
        int totalPages = (int) Math.ceil(resultat.size() / (double) taillePage);
        int pageValide = Math.max(0, Math.min(page, totalPages - 1));
        int debut = pageValide * taillePage;
        int fin = Math.min(debut + taillePage, resultat.size());
        List<Produit> laPage = resultat.isEmpty() ? List.of() : resultat.subList(debut, fin);
        return new PageProduits(laPage, pageValide, totalPages);
    }
}
