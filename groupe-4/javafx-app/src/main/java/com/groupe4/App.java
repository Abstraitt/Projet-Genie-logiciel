package com.groupe4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage fenetrePrincipale) throws Exception {
        Parent racine = FXMLLoader.load(getClass().getResource("/vues/LoginView.fxml"));

        Scene scene = new Scene(racine, 480, 560);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        fenetrePrincipale.setTitle("Plateforme e-commerce — Connexion");
        fenetrePrincipale.setScene(scene);
        fenetrePrincipale.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
