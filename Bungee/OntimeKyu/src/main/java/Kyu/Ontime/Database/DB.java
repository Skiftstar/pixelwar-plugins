package Kyu.Ontime.Database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import Kyu.Ontime.Main;
import org.mariadb.jdbc.MariaDbDataSource;

public class DB {

    private String host, database, user, password, url;
    private int port;
    private MariaDbDataSource dataSource;

    public DB() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        loadConfig();
        try {
            url = "jdbc:mariadb://" + host + ":" + port + "/" + database;
            dataSource = new MariaDbDataSource(url);
            initDb();
        } catch (Exception e) {
            Main.logger().severe("Error initializing DB tables!");
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        host = Main.getConfig().getString("database.host");
        port = Main.getConfig().getInt("database.port");
        database = Main.getConfig().getString("database.database");
        user = Main.getConfig().getString("database.user");
        password = Main.getConfig().getString("database.password");
    }

    public MariaDbDataSource getDataSource() {
        return dataSource;
    }

    private void initDb() throws SQLException, IOException {
        // first lets read our setup file.
        // This file contains statements to create our inital tables.
        // it is located in the resources.
        String setup;
        try (InputStream in = Main.instance().getResourceAsStream("dbsetup.sql")) {
            // Java 9+ way
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            Main.logger().log(Level.SEVERE, "Could not read db setup file.", e);
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
        Main.logger().info("§2Database setup complete.");
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
