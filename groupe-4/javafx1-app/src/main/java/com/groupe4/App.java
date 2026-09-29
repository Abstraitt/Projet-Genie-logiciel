package com.groupe4;

import com.groupe4.utils.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage fenetrePrincipale) {
        Navigation.initialiser(fenetrePrincipale);
        Navigation.afficher("/vues/LoginView.fxml", "Connexion");
        fenetrePrincipale.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
