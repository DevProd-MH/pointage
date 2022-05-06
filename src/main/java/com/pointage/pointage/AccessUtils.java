package com.pointage.pointage;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class AccessUtils {
    private Connection c;
    private Database db;
    private final List<String> columnNames = new ArrayList<>();

    AccessUtils() {
        connect();
    }

    public String connect() {
        try {
            File file = new File("src/main/resources/com/pointage/db/pointage.accdb");
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            c = DriverManager.getConnection("jdbc:ucanaccess://" + file.getAbsolutePath());
            return "Connected!";
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getCause());
            return "Not Connected!";
        }
    }

    public ResultSet executeQuery(String SQL) {
        ResultSet rs = null;
        try {
            Statement st = c.createStatement();
            PreparedStatement ps = c.prepareStatement(SQL);
            if (SQL.toUpperCase().startsWith("SELECT")) rs = ps.executeQuery();
            else ps.executeUpdate(); // ps.executeQuery();
            c.commit();
            System.out.println("\nexecuted successfully : " + SQL.toUpperCase().split(" ")[0]);//+ "\n" + SQL);
        } catch (SQLException e) {
            System.out.println("\nexecution failed : \n SQL :" + SQL + "\nCause :" + e.getCause());
        }
        return rs;
    }

    public void populateData(TableView tv, String query) throws SQLException {
        if (!tv.getColumns().isEmpty()) tv.getColumns().removeAll(tv.getColumns());
        ResultSet resultSet = executeQuery(query);
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            final int j = i;
            TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i + 1));
            col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> {
                if (param.getValue().get(j) != null) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                } else {
                    return null;
                }
            });
            tv.getColumns().addAll(col);
            this.columnNames.add(col.getText());
        }
        while (resultSet.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                row.add(resultSet.getString(i));
            }
            data.add(row);
        }

        tv.setItems(data);
    }

}
