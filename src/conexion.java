import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    static final String SERVER_IP = "localhost";
    static final String DB_NAME = "fotografo";
    static final String USER = "root";
    static final String PASSWORD = "root";
    static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_URL = "jdbc:mariadb://"+SERVER_IP + ":3306/" + DB_NAME;
    private Connection conn;

    public static Connection MyConexion(){
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            //System.out.println("Connecting to the database...");
            conn =DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
