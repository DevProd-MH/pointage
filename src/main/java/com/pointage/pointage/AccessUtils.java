package com.pointage.pointage;

import com.healthmarketscience.jackcess.Database;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.jackcess.DatabaseBuilder;

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
            db = new DatabaseBuilder().open(new File("/home/devprod/IdeaProjects/pointage/src/main/resources/com/pointage/db/pointage.accdb"));
            c = DriverManager.getConnection("jdbc:ucanaccess:///home/devprod/IdeaProjects/pointage/src/main/resources/com/pointage/db/pointage.accdb");
            return "Connected!";
        } catch (SQLException | IOException e) {
            System.out.println(e.getCause());
            return "Not Connected!";
        }
    }

    public ResultSet executeQuery(String SQL) {
        ResultSet rs = null;
        try {
            rs = c.createStatement().executeQuery(SQL);
            c.commit();
            db.flush();
            System.out.println("executed successfully :\n" + SQL);
        } catch (SQLException | IOException e) {
            System.out.println("execution failed :\n" + SQL + "\n" + e.getCause());
        }
        return rs;
    }

    void populateData(TableView tv, String query) throws SQLException {
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
