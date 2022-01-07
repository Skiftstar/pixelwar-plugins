package Kyu.ServerCore.DB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import org.mariadb.jdbc.MariaDbDataSource;

import Kyu.ServerCore.Main;

public class DB {

    private String host, database, user, password, url;
    private int port;
    private MariaDbDataSource dataSource;
    public String table = "Economy";

    public DB() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        loadConfig();
        System.out.println(host);
        System.out.println(database);
        try {
            url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            dataSource = new MariaDbDataSource(url);
            System.out.println(url);
            initDb();
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Error initializing DB tables!");
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        host = Main.getInstance().getConfig().getString("database.host");
        port = Main.getInstance().getConfig().getInt("database.port");
        database = Main.getInstance().getConfig().getString("database.database");
        user = Main.getInstance().getConfig().getString("database.user");
        password = Main.getInstance().getConfig().getString("database.password");
    }

    public MariaDbDataSource getDataSource() {
        return dataSource;
    }

    private void initDb() throws SQLException, IOException {
        // first lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = Main.getInstance().getResource("dbsetup.sql")) {
            // Java 9+ way
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            throw e;
        }
        // Mariadb can only handle a single query per statement. We need to split at ;.
        String[] queries = setup.split(";");
        // execute each query to the database.
        Connection conn = dataSource.getConnection(user, password);
        PreparedStatement stmt = null;
        for (String query : queries) {
            // If you use the legacy way you have to check for empty queries here.
            if (query.isBlank())
                continue;
            stmt = conn.prepareStatement(query);
            System.out.println(query);
            stmt.execute();
            stmt.close();
        }
        conn.close();
        Main.getInstance().getLogger().info("§2Database setup complete.");
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection(user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
