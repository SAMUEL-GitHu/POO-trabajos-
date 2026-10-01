package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class mainguiController {
    @FXML
    BorderPane bp;
    @FXML
    MenuBar menuBar;
    @FXML
    MenuItem menuItem1, menuItem2;
    @FXML
    TabPane tabPane;
    @FXML
    public void initialize(){
        System.out.println("Inicio.... ");
    }



}
