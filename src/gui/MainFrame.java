package src.gui;

import src.BookService;
import src.DBConnection;
import src.util.AppLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

public class MainFrame extends JFrame {

    private Connection conn;
    private String user;
    private String database;

    private JTable table;
    private DefaultTableModel model;

    private JLabel countLabel;
    private JTextArea logArea;

    public MainFrame(Connection conn,String user,String database) {

        this.conn=conn;
        this.user=user;
        this.database=database;

        setTitle("Library - user: "+user);
        setSize(1000,550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID","Title","Author","Genre","Year"},0);

        table = new JTable(model);
        table.setRowSorter(new TableRowSorter<>(model));

        add(new JScrollPane(table),BorderLayout.CENTER);

        JPanel top = new JPanel();

        JButton viewBtn = new JButton("View All");
        JButton searchBtn = new JButton("Search");

        top.add(viewBtn);
        top.add(searchBtn);

        add(top,BorderLayout.NORTH);

        JPanel bottom = new JPanel();

        JButton backBtn = new JButton("Back");

        countLabel = new JLabel("Books: 0");

        bottom.add(backBtn);
        bottom.add(countLabel);

        if(isAdmin()) {

            JButton addBtn = new JButton("Add");
            JButton updateBtn = new JButton("Update");
            JButton deleteBtn = new JButton("Delete");
            JButton clearBtn = new JButton("Clear");

            JButton createUserBtn = new JButton("Create User");
            JButton dropDBBtn = new JButton("Drop DB");

            bottom.add(addBtn);
            bottom.add(updateBtn);
            bottom.add(deleteBtn);
            bottom.add(clearBtn);

            bottom.add(createUserBtn);
            bottom.add(dropDBBtn);

            addBtn.addActionListener(e -> addBook());
            updateBtn.addActionListener(e -> updateBook());
            deleteBtn.addActionListener(e -> deleteBook());
            clearBtn.addActionListener(e -> clearTable());

            createUserBtn.addActionListener(e -> createUser());
            dropDBBtn.addActionListener(e -> dropDatabase());
        }

        add(bottom,BorderLayout.SOUTH);

        logArea = new JTextArea(10,30);
        logArea.setEditable(false);

        add(new JScrollPane(logArea),BorderLayout.EAST);

        AppLogger.setLogArea(logArea);

        viewBtn.addActionListener(e -> loadAll());
        searchBtn.addActionListener(e -> search());
        backBtn.addActionListener(e -> backToSetup());

        AppLogger.log("User "+user+" connected to DB "+database);

        loadAll();

        setVisible(true);
    }

    private void fillTable(List<String[]> books) {

        model.setRowCount(0);

        for(String[] b : books) {
            model.addRow(b);
        }

        countLabel.setText("Books: "+books.size());
    }

    private void loadAll() {

        try {

            fillTable(BookService.getAllBooks(conn));
            AppLogger.log("View all books");

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void search() {

        String[] fields = {"Title","Author","Genre"};

        String field = (String) JOptionPane.showInputDialog(
                this,
                "Search field",
                "Search",
                JOptionPane.QUESTION_MESSAGE,
                null,
                fields,
                fields[0]
        );

        if(field==null) return;

        String value = JOptionPane.showInputDialog("Enter value");

        if(value==null) return;

        try {

            fillTable(BookService.search(conn,field,value));
            AppLogger.log("Search "+field+" = "+value);

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void addBook() {

        try {

            String title = JOptionPane.showInputDialog("Title");
            String author = JOptionPane.showInputDialog("Author");
            String genre = JOptionPane.showInputDialog("Genre");

            int year = Integer.parseInt(
                    JOptionPane.showInputDialog("Year"));

            BookService.addBook(conn,title,author,genre,year);

            AppLogger.log("Add book "+title);

            loadAll();

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void updateBook() {

        int row = table.getSelectedRow();

        if(row==-1){
            JOptionPane.showMessageDialog(this,"Select row");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row,0).toString());

        JTextField titleField = new JTextField(model.getValueAt(row,1).toString());
        JTextField authorField = new JTextField(model.getValueAt(row,2).toString());
        JTextField genreField = new JTextField(model.getValueAt(row,3).toString());
        JTextField yearField = new JTextField(model.getValueAt(row,4).toString());

        JPanel panel = new JPanel(new GridLayout(4,2));

        panel.add(new JLabel("Title"));
        panel.add(titleField);

        panel.add(new JLabel("Author"));
        panel.add(authorField);

        panel.add(new JLabel("Genre"));
        panel.add(genreField);

        panel.add(new JLabel("Year"));
        panel.add(yearField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Update Book",
                JOptionPane.OK_CANCEL_OPTION
        );

        if(result!=JOptionPane.OK_OPTION) return;

        try{

            BookService.updateBook(
                    conn,
                    id,
                    titleField.getText(),
                    authorField.getText(),
                    genreField.getText(),
                    Integer.parseInt(yearField.getText())
            );

            AppLogger.log("Update book id="+id);

            loadAll();

        }catch(Exception ex){

            showError(ex);

        }
    }

    private void deleteBook() {

        int row = table.getSelectedRow();

        if(row==-1) {
            JOptionPane.showMessageDialog(this,"Select row");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row,0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete book?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm!=JOptionPane.YES_OPTION) return;

        try {

            BookService.deleteById(conn,id);

            AppLogger.log("Delete book id="+id);

            loadAll();

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void clearTable() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Clear table?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm!=JOptionPane.YES_OPTION) return;

        try {

            BookService.clearTable(conn);

            AppLogger.log("Clear table");

            loadAll();

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void createUser() {

        String username = JOptionPane.showInputDialog("Username");
        String password = JOptionPane.showInputDialog("Password");

        String[] roles = {"admin_role","guest_role"};

        String role = (String) JOptionPane.showInputDialog(
                this,
                "Role",
                "Choose role",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[0]
        );

        try {

            var stmt = conn.prepareCall("CALL sp_create_db_user(?,?,?)");

            stmt.setString(1,username);
            stmt.setString(2,password);
            stmt.setString(3,role);

            stmt.execute();

            AppLogger.log("Create user "+username+" role="+role);

            JOptionPane.showMessageDialog(this,"User created");

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private boolean isAdmin() {

        try{

            var st = conn.createStatement();

            var rs = st.executeQuery(
                    "SELECT pg_has_role(current_user,'admin_role','member')");

            rs.next();

            return rs.getBoolean(1);

        }catch(Exception e){

            return false;

        }
    }

    private void dropDatabase() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete database " + database + " ?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {

            Connection conn = DBConnection.getConnection(
                    "localhost",
                    5433,
                    "postgres",
                    "db_admin",
                    "admin"
            );

            Statement st = conn.createStatement();

            st.execute(
                    "SELECT pg_terminate_backend(pid) " +
                            "FROM pg_stat_activity " +
                            "WHERE datname = '" + database + "' " +
                            "AND pid <> pg_backend_pid();"
            );

            st.execute("DROP DATABASE \"" + database + "\"");

            AppLogger.log("Database deleted: " + database);

            JOptionPane.showMessageDialog(this, "Database deleted");

            conn.close();

            new DatabaseSetupFrame();
            dispose();

        }
        catch(Exception ex) {

            showError(ex);

        }
    }

    private void backToSetup() {

        try {
            conn.close();
        }
        catch(Exception ignored){}

        AppLogger.log("Return to database setup");

        new DatabaseSetupFrame();

        dispose();
    }

    private void showError(Exception ex) {

        AppLogger.log("ERROR: "+ex.getMessage());

        JOptionPane.showMessageDialog(this,ex.getMessage());

    }
}
