package com.groupe4.utils;

/** Garde en mémoire le jeton de l'utilisateur connecté (reçu du Groupe 1). */
public class Session {

    private static String jeton;

    public static void setJeton(String nouveauJeton) {
        jeton = nouveauJeton;
    }

    public static String getJeton() {
        return jeton;
    }

    public static void deconnecter() {
        jeton = null;
    }
}
