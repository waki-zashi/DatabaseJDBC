package src.gui;

import src.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    private String host;
    private int port;
    private String database;

    public LoginFrame(String host,int port,String database) {

        this.host=host;
        this.port=port;
        this.database=database;

        setTitle("Login");
        setSize(350,200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3,2,10,10));

        panel.add(new JLabel("User"));
        userField = new JTextField();
        panel.add(userField);

        panel.add(new JLabel("Password"));
        passField = new JPasswordField();
        panel.add(passField);

        JButton loginBtn = new JButton("Login");

        panel.add(new JLabel());
        panel.add(loginBtn);

        add(panel);

        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {

        String user = userField.getText();
        String pass = new String(passField.getPassword());

        try {

            Connection conn = DBConnection.getConnection(
                    host,
                    port,
                    database,
                    user,
                    pass
            );

            new MainFrame(conn,user,database);
            dispose();

        }
        catch(Exception ex) {

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }
    }
}