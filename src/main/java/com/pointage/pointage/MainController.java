package com.pointage.pointage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    @FXML
    ComboBox<String> cb, cb1, cb2, dpt, grd, mdl;
    @FXML
    DatePicker dt, dtrp;
    @FXML
    CheckBox al1, al2, mo;
    @FXML
    TextField nm, prnm, ntel, addr, ser, cp, em;
    @FXML
    TextArea t1, t2;
    @FXML
    TableView tv;
    private final AccessUtils accessUtils = new AccessUtils();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(accessUtils.connect());
        updateBoxes();
        updateOther(dpt, "dpt");
        updateOther(grd, "grade");
        updateOther(mdl, "module");

    }

    @FXML
    private void AddPoint(ActionEvent event) {
        Button src = (Button) event.getSource();
        switch (src.getText()) {
            case "Present":
                if (al1.isSelected()) {
                    for (String ens : cb.getItems()) {
                        check(t2, ens);
                        t1.appendText(ens + "\n");
                    }
                    return;
                }
                check(t2, cb.getValue());
                if (!t1.getText().contains(cb.getValue())) t1.appendText(cb.getValue() + "\n");
                break;
            case "Absent":
                if (al1.isSelected()) {
                    for (String ens : cb.getItems()) {
                        check(t1, ens);
                        t2.appendText(ens + "\n");
                    }
                    return;
                }
                check(t1, cb.getValue());
                if (!t2.getText().contains(cb.getValue())) t2.appendText(cb.getValue() + "\n");
                break;


        }
    }

    private void check(TextArea tf, String value) {
        for (String line : tf.getText().split("\n")) {
            if (line.contains(value)) {
                tf.setText(tf.getText().replace(line, ""));
            }
            if (line.isEmpty()) tf.setText(tf.getText().replace(line.replaceAll("[\\\\\\r\\\\\\n]+", ""), ""));
        }
        tf.setText(tf.getText().replaceAll("[\\\\\\r\\\\\\n]+", "\n"));
    }

    @FXML
    private void validate() {
        String values;
        for (String line : t1.getText().split("\n")) {
            if (!line.isEmpty()) {
                values = line.split("_")[0] + ",'" + dt.getValue() + "','P'";
                accessUtils.executeQuery("insert into pointage (id_ens,date_pres,presence) values (" + values + ")");
            }
        }

        for (String line : t2.getText().split("\n")) {
            if (!line.isEmpty()) {
                values = line.split("_")[0] + ",'" + dt.getValue() + "','A'";
                accessUtils.executeQuery("insert into pointage (id_ens,date_pres,presence) values (" + values + ")");
            }
        }
        reset();
    }

    @FXML
    private void addEns() {
        String values = cb.getItems().size() + ",'" + nm.getText() + "','" + prnm.getText() + "','" + addr.getText() + "','" + cp.getText() + "','" + em.getText() + "'," + ntel.getText() + ",'" + grd.getValue() + "','" + dpt.getValue() + "','" + mdl.getValue() + "'";
        accessUtils.executeQuery("INSERT INTO ensignant (ID,nom,prenom,Adresse,CodePostal,email,tel,grade,dpt,module) values (" + values + ")");
        updateBoxes();
        setText(mdl, grd);
    }

    @FXML
    private void deleteEns() {
        String sql = "delete from ensignant where id = " + cb2.getValue().split("_")[0];
        accessUtils.executeQuery(sql);
        updateBoxes();
    }

    @FXML
    private void editEns() {
        String[] n = cb2.getValue().split("_");
        ResultSet rs = accessUtils.executeQuery("SELECT * FROM ensignant WHERE id = " + n[0] + " and nom = '" + n[1] + "' and prenom = '" + n[2] + "'");
        try {
            if (rs.next()) {
                nm.setText(rs.getString("nom"));
                prnm.setText(rs.getString("prenom"));
                ntel.setText(rs.getInt("tel") + "");
                addr.setText(rs.getString("Adresse"));
                cp.setText(rs.getString("CodePostal"));
                grd.setValue(rs.getString("grade"));
                mdl.setValue(rs.getString("module"));
                dpt.setValue(rs.getString("dpt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void search() {
        try {
            ResultSet rs = accessUtils.executeQuery("select id,nom,prenom from ensignant where nom = '%" + ser.getText() + "%'");
            cb2.getItems().remove(0, cb2.getItems().size());
            while (rs.next()) {
                cb2.getItems().add(rs.getInt("id") + "_" + rs.getString("nom") + "_" + rs.getString("prenom"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void show() {
        try {
            String date = "date_pres = '" + dtrp.getValue().toString();
            if (mo.isSelected()) {
                String Ndate = date.substring(0, date.length() - 2).replace("date_pres = '", "");
                date = "date_pres between '" + Ndate + "01' AND '" + Ndate + ("00" + EndDay(dtrp)).substring(EndDay(dtrp).length());
            }
            String sql;
            if (al2.isSelected())
                sql = "SELECT id,nom,prenom,presence FROM ensignant,pointage where ensignant.id = pointage.id_ens and " + date + "'";
            else {
                String[] id = cb1.getValue().split("_");
                sql = "SELECT id,nom,prenom,presence FROM ensignant,pointage where ensignant.id = pointage.id_ens and ensignant.id = " + id[0] + " and " + date + "'";
            }
            accessUtils.populateData(tv, sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void reset() {
        t1.setText("");
        t2.setText("");
    }

    @FXML
    private void reset1() {
        setText(grd, mdl);
    }

    private void setText(ComboBox<String> grd, ComboBox<String> mdl) {
        nm.setText("");
        prnm.setText("");
        ntel.setText("");
        addr.setText("");
        cp.setText("");
        em.setText("");
        dpt.setValue("");
        grd.setValue("");
        mdl.setValue("");
    }

    @FXML
    private void auto() {

    }

    private void updateBoxes() {
        try {
            ResultSet rs = accessUtils.executeQuery("SELECT * FROM ensignant");
            if (rs == null) return;
            if (!cb.getItems().isEmpty()) cb.getItems().remove(0, cb.getItems().size());
            if (!cb1.getItems().isEmpty()) cb1.getItems().remove(0, cb1.getItems().size());
            if (!cb2.getItems().isEmpty()) cb2.getItems().remove(0, cb2.getItems().size());
            while (rs.next()) {
                String name = rs.getInt("id") + "_" + rs.getString("nom") + "_" + rs.getString("prenom");
                cb.getItems().add(name);
                cb1.getItems().add(name);
                cb2.getItems().add(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateOther(ComboBox<String> c, String name) {
        try {
            ResultSet rs = accessUtils.executeQuery("SELECT * FROM " + name);
            while (rs.next()) {
                String n = rs.getInt("id") + "_" + rs.getString(2);
                c.getItems().add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String EndDay(DatePicker dp) {
        int m = Integer.parseInt(dp.getValue().toString().split("-")[1]);
        if (m == 2) return "28";
        if (m >= 8) m++;
        return m % 2 == 0 ? "30" : "31";
    }

    private String flipDate(String date) {
        String[] dt = date.split("-");
        return dt[2] + "/" + dt[1] + "/" + dt[0];
    }
}