package src.gui;

import src.DBConnection;
import src.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetupFrame extends JFrame {

    private JTextField hostField;
    private JTextField portField;
    private JTextField dbField;
    private JTextArea logArea;

    public DatabaseSetupFrame() {

        setTitle("Database Setup");
        setSize(450,350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4,2,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        panel.add(new JLabel("Host"));
        hostField = new JTextField("localhost");
        panel.add(hostField);

        panel.add(new JLabel("Port"));
        portField = new JTextField("5433");
        panel.add(portField);

        panel.add(new JLabel("Database"));
        dbField = new JTextField("library");
        panel.add(dbField);

        JButton connectBtn = new JButton("Connect");
        JButton createBtn = new JButton("Create DB");

        panel.add(connectBtn);
        panel.add(createBtn);

        add(panel,BorderLayout.NORTH);

        logArea = new JTextArea(8,40);
        logArea.setEditable(false);

        add(new JScrollPane(logArea),BorderLayout.CENTER);

        AppLogger.setLogArea(logArea);

        connectBtn.addActionListener(e -> connect());
        createBtn.addActionListener(e -> createDatabase());

        setVisible(true);
    }

    private void connect() {

        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        String db = dbField.getText();

        AppLogger.log("Open login window for DB "+db);

        new LoginFrame(host,port,db);
        dispose();
    }

    private void createDatabase() {

        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());

        String db = JOptionPane.showInputDialog(
                this,
                "Enter database name"
        );

        if(db==null || db.isBlank())
            return;

        try {

            Connection conn = DBConnection.getConnection(
                    host,
                    port,
                    "postgres",
                    "postgres",
                    "postgres"
            );

            var check = conn.prepareStatement(
                    "SELECT 1 FROM pg_database WHERE datname=?");

            check.setString(1,db);

            var rs = check.executeQuery();

            if(rs.next()){

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Database already exists. Connect?",
                        "Database exists",
                        JOptionPane.YES_NO_OPTION
                );

                if(choice==JOptionPane.YES_OPTION){

                    dbField.setText(db);

                    AppLogger.log("DB exists, connecting: "+db);

                    return;

                }else{

                    return;

                }
            }

            var stmt = conn.createStatement();

            stmt.execute("CREATE DATABASE "+db);

            AppLogger.log("Database created: "+db);

            initializeDatabase(host,port,db);

            JOptionPane.showMessageDialog(
                    this,
                    "Database created and initialized"
            );

        }
        catch(Exception ex){

            ex.printStackTrace();

            AppLogger.log("Create DB error: "+ex.getMessage());

            JOptionPane.showMessageDialog(this,ex.getMessage());

        }
    }

    private void initializeDatabase(String host,int port,String db) throws Exception {

        Connection conn = DBConnection.getConnection(
                host,
                port,
                db,
                "postgres",
                "postgres"
        );

        Statement st = conn.createStatement();

        AppLogger.log("Initializing database "+db);

        st.execute("""
        CREATE TABLE IF NOT EXISTS books(
            id SERIAL PRIMARY KEY,
            title VARCHAR(255),
            author VARCHAR(255),
            genre VARCHAR(255),
            year INT
        )
        """);

        st.execute("""
        DO $$
        BEGIN
        IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname='admin_role'
        )
        THEN
        CREATE ROLE admin_role;
        END IF;
        END$$;
        """);
        st.execute("""
        DO $$
        BEGIN
        IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname='guest_role'
        )
        THEN
        CREATE ROLE guest_role;
        END IF;
        END$$;
        """);

        st.execute("""
        CREATE OR REPLACE PROCEDURE sp_add_book(
        p_title VARCHAR,
        p_author VARCHAR,
        p_genre VARCHAR,
        p_year INT
        )
        LANGUAGE SQL
        AS $$
        INSERT INTO books(title,author,genre,year)
        VALUES(p_title,p_author,p_genre,p_year);
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE PROCEDURE sp_update_book(
        p_id INT,
        p_title VARCHAR,
        p_author VARCHAR,
        p_genre VARCHAR,
        p_year INT
        )
        LANGUAGE SQL
        AS $$
        UPDATE books
        SET title=p_title,
        author=p_author,
        genre=p_genre,
        year=p_year
        WHERE id=p_id;
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE PROCEDURE sp_delete_by_id(p_id INT)
        LANGUAGE SQL
        AS $$
        DELETE FROM books WHERE id=p_id;
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE PROCEDURE sp_clear_table()
        LANGUAGE SQL
        AS $$
        DELETE FROM books;
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE FUNCTION sp_get_all_books()
        RETURNS TABLE(
        id INT,
        title VARCHAR,
        author VARCHAR,
        genre VARCHAR,
        year INT)
        LANGUAGE SQL
        AS $$
        SELECT * FROM books;
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE FUNCTION sp_find_by_title(p_title VARCHAR)
        RETURNS TABLE(
        id INT,title VARCHAR,author VARCHAR,genre VARCHAR,year INT)
        LANGUAGE SQL
        AS $$
        SELECT * FROM books
        WHERE LOWER(title) LIKE LOWER('%'||p_title||'%');
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE FUNCTION sp_find_by_author(p_author VARCHAR)
        RETURNS TABLE(
        id INT,title VARCHAR,author VARCHAR,genre VARCHAR,year INT)
        LANGUAGE SQL
        AS $$
        SELECT * FROM books
        WHERE LOWER(author) LIKE LOWER('%'||p_author||'%');
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE FUNCTION sp_find_by_genre(p_genre VARCHAR)
        RETURNS TABLE(
        id INT,title VARCHAR,author VARCHAR,genre VARCHAR,year INT)
        LANGUAGE SQL
        AS $$
        SELECT * FROM books
        WHERE LOWER(genre) LIKE LOWER('%'||p_genre||'%');
        $$;
        """);

        st.execute("""
        CREATE OR REPLACE PROCEDURE sp_create_db_user(
        p_username VARCHAR,
        p_password VARCHAR,
        p_role VARCHAR
        )
        LANGUAGE plpgsql
        SECURITY DEFINER
        AS $$
        BEGIN
        EXECUTE format('CREATE USER %I WITH PASSWORD %L',
        p_username,p_password);

        EXECUTE format('GRANT %I TO %I',
        p_role,p_username);
        END;
        $$;
        """);

        st.execute("""
        GRANT EXECUTE ON PROCEDURE sp_add_book TO admin_role;
        GRANT EXECUTE ON PROCEDURE sp_update_book TO admin_role;
        GRANT EXECUTE ON PROCEDURE sp_delete_by_id TO admin_role;
        GRANT EXECUTE ON PROCEDURE sp_clear_table TO admin_role;

        GRANT EXECUTE ON FUNCTION sp_get_all_books TO admin_role,guest_role;
        GRANT EXECUTE ON FUNCTION sp_find_by_title TO admin_role,guest_role;
        GRANT EXECUTE ON FUNCTION sp_find_by_author TO admin_role,guest_role;
        GRANT EXECUTE ON FUNCTION sp_find_by_genre TO admin_role,guest_role;

        GRANT EXECUTE ON PROCEDURE sp_create_db_user TO admin_role;
        """);

        AppLogger.log("Database initialized successfully");

        conn.close();
    }
}