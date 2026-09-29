package com.groupe4.services;

/**
 * Gère la communication avec le module d'authentification (Groupe 1).
 * Pour l'instant, connecter() est SIMULÉ (pas de vrai appel réseau),
 * le temps que l'API du Groupe 1 soit branchée. On remplacera juste
 * le contenu de cette méthode plus tard, sans toucher au reste de l'écran.
 */
public class AuthService {

    public ResultatConnexion connecter(String email, String motDePasse) {

        // --- Simulation en attendant le vrai appel à POST /auth/connexion ---
        if (email.equalsIgnoreCase("test@groupe4.com") && motDePasse.equals("motdepasse")) {
            return ResultatConnexion.succes("jeton-simule-123");
        }
        return ResultatConnexion.echec("Email ou mot de passe incorrect.");

        // --- Ce que ça deviendra avec l'API réelle (à activer plus tard) ---
        // HttpClient client = HttpClient.newHttpClient();
        // HttpRequest requete = HttpRequest.newBuilder()
        //         .uri(URI.create("http://localhost:3000/auth/connexion"))
        //         .header("Content-Type", "application/json")
        //         .POST(HttpRequest.BodyPublishers.ofString(
        //                 "{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}"))
        //         .build();
        // ... envoi de la requête, lecture du JSON, et retour d'un ResultatConnexion.
    }

    /**
     * Petit objet qui représente le résultat d'une tentative de connexion :
     * soit ça a marché (on a un jeton), soit ça a échoué (on a un message d'erreur).
     */
    public record ResultatConnexion(boolean succes, String jeton, String messageErreur) {

        public static ResultatConnexion succes(String jeton) {
            return new ResultatConnexion(true, jeton, null);
        }

        public static ResultatConnexion echec(String messageErreur) {
            return new ResultatConnexion(false, null, messageErreur);
        }
    }
}
