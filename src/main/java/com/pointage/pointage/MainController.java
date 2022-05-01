package com.pointage.pointage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@SuppressWarnings("all")
public class MainController implements Initializable {
    @FXML
    ComboBox<String> cb, cb1, cb2, dpt, grd, mdl;
    @FXML
    DatePicker dt, dtrp;
    @FXML
    CheckBox al1, al2, mo, tod;
    @FXML
    TextField nm, prnm, ntel, addr, ser, cp, em;
    @FXML
    TextArea t1, t2;
    @FXML
    TableView tv;
    @FXML
    AnchorPane pn;
    @FXML
    Button prsnt, absnt;
    @FXML
    Label lb, lb1;
    @FXML
    ImageView iv;
    private final AccessUtils accessUtils = new AccessUtils();
    private boolean editing = false;

    /***
     *
     * Scene initialization <br>
     * set Background image<br>
     * connect to .accdb file<br>
     * set keyboard shortcut for easy setting Present or Absent<br>
     * fill combo-boxes with data from database
     *
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            FileInputStream input = new FileInputStream("src/main/resources/com/pointage/pointage/bg.jpg");
            Image image = new Image(input);
            iv = new ImageView(image);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        pn.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case P:
                    prsnt.fire();
                    break;
                case A:
                    absnt.fire();
            }
        });
        System.out.println(accessUtils.connect());
        updateBoxes();
        updateOther(dpt, "dpt");
        updateOther(grd, "grade");
        updateOther(mdl, "module");

    }

    /***
     *
     * @param event
     * <br>auto set Present or Absent from pressing `event` on button<br>
     * add selected ENS to Present or Absent list if not 'selectioner tous'<br>
     * if 'selectioner tous' all ENS will be added to Present or Absent list
     *
     */
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

    /***
     *
     *<br>check if ENS already in P/A TextFields if not exist add it
     */
    private void check(TextArea tf, String value) {
        for (String line : tf.getText().split("\n")) {
            if (line.contains(value)) {
                tf.setText(tf.getText().replace(line, ""));
            }
            if (line.isEmpty()) tf.setText(tf.getText().replace(line.replaceAll("[\\\\\\r\\n]+", ""), ""));
        }
        tf.setText(tf.getText().replaceAll("[\\\\\\r\\n]+", "\n"));
    }

    /***
     * Validate and insert the P/A lists to the database
     */
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

    /***
     * Insert ENS information to database
     */
    @FXML
    private void addEns() {
        try {
            ResultSet rs = accessUtils.executeQuery("SELECT id FROM ensignant ORDER BY id DESC LIMIT 1");
            int id = 0;
            if (rs.next()) id = rs.getInt("id") + 1;
            String values = id + ",'" + nm.getText() + "','" + prnm.getText() + "','" + addr.getText() + "','" + cp.getText() + "','" + em.getText() + "'," + ntel.getText() + ",'" + grd.getValue() + "','" + dpt.getValue() + "','" + mdl.getValue() + "'";
            String sql = "INSERT INTO ensignant (ID,nom,prenom,Adresse,CodePostal,email,tel,grade,dpt,module) values (" + values + ")";
            if (editing) {
                sql = "UPDATE ensignant set nom = '" + nm.getText() + "' , prenom = '" + prnm.getText() + "' , Adresse = '" + addr.getText() + "' , CodePostal = '" + cp.getText() + "' , email = '" + em.getText() + "' , tel = " + ntel.getText() + " , grade = '" + grd.getValue() + "' , dpt = '" + dpt.getValue() + "' , module = '" + mdl.getValue() + "' where id = " + lb1.getText().replace("id = ", "");
            }
            accessUtils.executeQuery(sql);
            updateBoxes();
            reset1();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Delete selected ENS from database
     */
    @FXML
    private void deleteEns() {
        String sql = "delete from ensignant where id = " + cb2.getValue().split("_")[0];
        accessUtils.executeQuery(sql);
        updateBoxes();
    }

    /***
     * Populate selected ENS information into correspending TextField
     */
    @FXML
    private void editEns() {
        String[] n = cb2.getValue().split("_");
        ResultSet rs = accessUtils.executeQuery("SELECT * FROM ensignant WHERE id = " + n[0] + " and nom = '" + n[1] + "' and prenom = '" + n[2] + "'");
        try {
            if (rs.next()) {
                lb1.setText("id = " + rs.getInt("id"));
                nm.setText(rs.getString("nom"));
                prnm.setText(rs.getString("prenom"));
                ntel.setText(rs.getInt("tel") + "");
                addr.setText(rs.getString("Adresse"));
                cp.setText(rs.getString("CodePostal"));
                em.setText(rs.getString("email"));
                grd.setValue(rs.getString("grade"));
                mdl.setValue(rs.getString("module"));
                dpt.setValue(rs.getString("dpt"));
            }
            editing = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Search for ENS by searching matching letters pattern found in it's name
     */
    @FXML
    private void search() {
        try {
            String sql = "select id,nom,prenom from ensignant where nom LIKE '%" + ser.getText() + "%'";
            if (ser.getText().isEmpty()) {
                updateBoxes();
                return;
            }
            ResultSet rs = accessUtils.executeQuery(sql);
            cb2.getItems().remove(0, cb2.getItems().size());
            while (rs.next()) {
                cb2.getItems().add(rs.getInt("id") + "_" + rs.getString("nom") + "_" + rs.getString("prenom"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Populate data to TableView from database based on ENS and DATE selection
     */
    @FXML
    private void show() {
        boolean s = false, m = false;
        try {
            String Ndate = "", date = "date_pres = '" + dtrp.getValue().toString();
            String[] id = new String[3];
            if (mo.isSelected()) {
                Ndate = date.substring(0, date.length() - 2).replace("date_pres = '", "");
                date = "date_pres between '" + Ndate + "01' AND '" + Ndate + ("00" + EndDay(dtrp)).substring(EndDay(dtrp).length());
                m = true;
            }
            String sql;
            if (al2.isSelected()) {
                sql = "SELECT id,nom,prenom,date_pres,presence FROM ensignant,pointage where ensignant.id = pointage.id_ens and " + date + "'";
            } else {
                id = cb1.getValue().split("_");
                sql = "SELECT id,nom,prenom,date_pres,presence FROM ensignant,pointage where ensignant.id = pointage.id_ens and ensignant.id = " + id[0] + " and " + date + "'";
                s = true;
            }
            accessUtils.populateData(tv, sql);
            if (s && m) {
                ResultSet rs = accessUtils.executeQuery("SELECT count(presence) FROM ensignant,pointage where ensignant.id = pointage.id_ens and ensignant.id = " + id[0] + " and presence = 'P' and " + date + "'");
                if (rs.next()) {
                    lb.setText("Presenter : " + rs.getInt(1) + " fois dans " + Ndate.substring(0, Ndate.length() - 1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * set DatePicker to current date
     */
    @FXML
    private void today() {
        if (!tod.isSelected()) return;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        dt.setValue(LocalDate.parse(dtf.format(now)));
    }

    /***
     * clear P/A Lists
     */
    @FXML
    private void reset() {
        t1.setText("");
        t2.setText("");
    }

    /***
     * clear ComboBoxes, TextFields,..., for new input
     */
    @FXML
    private void reset1() {
        setText(grd, mdl);
        editing = false;
        lb1.setText("");
        ser.setText("");
        updateBoxes();
    }

    /***
     *
     * clear TextFields,ComboBoxes
     */
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

    /***
     * set auto inserting ENS to P or A list
     */
    @FXML
    private void auto() {
        filter(t1, t2);
        filter(t2, t1);
    }

    /***
     *
     * filter data on both P/A TextAreas and orgnize it
     */
    private void filter(TextArea t2, TextArea t1) {
        if (t2.getText().isEmpty() && !t1.getText().isEmpty()) {
            for (String value : cb.getItems()) {
                for (String line : t1.getText().split("\n")) {
                    if (!line.equals(value) && !line.isEmpty()) {
                        if (!t2.getText().contains(value) && !t1.getText().contains(value)) t2.appendText(value + "\n");
                    }
                }
            }
        }
    }

    /***
     * update data of ComboBoxs from database
     */
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

    /***
     *
     * @param c ComboBox
     * @param name table name
     * <br>update other ComboBox `c` from table `name` from database
     */
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

    /***
     *
     * @param dp DatePicker with selected value
     * @return days count of a month
     *
     */
    private String EndDay(DatePicker dp) {
        int m = Integer.parseInt(dp.getValue().toString().split("-")[1]);
        if (m == 2) return "28";
        if (m >= 8) m++;
        return m % 2 == 0 ? "30" : "31";
    }

}