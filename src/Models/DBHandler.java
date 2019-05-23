package Models;

import Models.Interfaces.IClientDetails;
import Models.Interfaces.IDBData;
import Models.Interfaces.IDBHandler;
import Models.Interfaces.IMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import javax.net.ssl.SSLException;

public class DBHandler implements IDBHandler
{

    //region C'tor
    public DBHandler()
    {
        m_isConnected = false;
        Init();
    }
    //endregion

    //region Public Methods
    @Override
    public boolean HandleMassege(IMessage message) {
        return false;
    }

    @Override
    public void Init()
    {
        m_connection = null;
        m_statement = null;

        try
        {
            Class.forName(JDBC_DRIVER);
            m_connection = DriverManager.getConnection(DB_URL, USER, PASS);
            m_statement = m_connection.createStatement();
            m_isConnected = true;
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void Close()
    {
        try
        {
            if (m_statement != null)
            {
                m_statement.close();
            }
            if (m_connection != null)
            {
                m_connection.close();
            }
        }
        catch (SQLException se)
        {
            se.printStackTrace();
        }
    }

    @Override
    public boolean IsConnectedToDB()
    {
        return m_isConnected;
    }

    @Override
    public IDBData GetResoultsFromDB()
    {
        return m_resault;
    }

    @Override
    public int GetNumOfPurchases(String p_userName)
    {
        String query = "Select COUNT(*) count FROM purchases WHERE USERNAME = '"+ p_userName +"'";
        ResultSet result = executeQuery(query);
        try {
            if (result == null) {
                return 0;
            }
            return result.getInt("count");
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    @Override
    public void IncreaseNumOfPurchases (String p_userName, int p_value)
    {
        return;
    }

    @Override
    public IClientDetails GetClientDetails (String p_userName)
    {
        try {
            String getNumOfPurchasesQuery = "SELECT * FROM clients WHERE username = '" +  p_userName+"'";
            PreparedStatement statement = m_connection.prepareStatement(getNumOfPurchasesQuery);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                int numOfPurchase = results.getInt("numOfPurchase");
                ClientDetails details = new ClientDetails();
                details.UserName = results.getString("USERNAME");
                details.FirstName = results.getString("FIRST_NAME");
                details.LastName = results.getString("LAST_NAME");
                details.Email = results.getString("EMAIL");
                details.Email = results.getString("PHONE");
                statement.close();
                return details;
            }
            else
            {
                statement.close();
                return null;
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> CheckUser(String p_userName, String p_password)
    {
        String query = "Select IS_CONNECTED 'connection', CLASSIFICATION 'type' FROM persons WHERE USERNAME = '" + p_userName + "' AND PASSWORD = '"+ p_password +"'";
        ResultSet result = executeQuery(query);
        try {
            if(result == null)
            {
                return new ArrayList<String>(){
                    {
                        add("login");
                        add("failure");
                        add("Username or password was invalid.");
                    }};
            }
            else if(result.getBoolean("connection"))
            {
                return new ArrayList<String>(){
                    {
                        add("login");
                        add("failure");
                        add("User is already logged in.");
                    }};
            }
            else{
                return new ArrayList<String>(){
                    {
                        add("login");
                        add("success");
                        add(Integer.toString(result.getInt("type")));
                    }};
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
            return new ArrayList<String>() {
                {
                    add("login");
                    add("failure");
                    add("Connection problem.");
                }};
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<String>(){
                {
                    add("login");
                    add("failure");
                    add("Connection problem.");
                }};
        }
    }

    @Override
    public boolean IsUsernameExists(String p_username, String p_password)
    {
        try
        {
            PreparedStatement statement = m_connection.prepareStatement("SELECT * FROM clients WHERE USERNAME = '"+ p_username +"'");
            ResultSet results = statement.executeQuery();
            if (results.next())
            {
                if(p_password.equals(results.getString("password")))
                {
                    statement.close();
                    return true;
                }
                else
                {
                    statement.close();
                    return false;
                }

            }
            else
            {
                statement.close();
                return false;
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean LogInUser(String p_userName, String p_userPassword)
    {
        String query = "UPDATE persons SET IS_CONNECTED = 1 WHERE USERNAME = '"+ p_userName +"'";
        return executeUpdate(query);
    }

    public boolean LogOutUser(String p_userName, String p_userPassword)
    {
        String query = "UPDATE persons SET IS_CONNECTED = 0 WHERE USERNAME = '"+ p_userName +"'";
        return executeUpdate(query);
    }

    //endregion

    //region Private Methods

    private ResultSet executeQuery(String p_query)
    {
        try
        {
            PreparedStatement statement = m_connection.prepareStatement(p_query);
            ResultSet results = statement.executeQuery();
            statement.close();
            if (results.next())
            {
                return results;
            }
            else
            {
                return null;
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean executeUpdate(String p_query)
    {
        try
        {
            PreparedStatement statement = m_connection.prepareStatement(p_query);
            statement.executeUpdate();
            statement.close();
            return true;
        }
        catch (SQLException se) {
            se.printStackTrace();
            System.out.println("SQLException: " + se.getMessage());
            System.out.println("SQLState: " + se.getSQLState());
            System.out.println("VendorError: " + se.getErrorCode());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void Test()
    {
        //LEEOR 23/5/19 12:00
    }

    //endregion

    //region Fields
    boolean m_isConnected;
    IDBData m_resault;
    private Connection m_connection;
    private Statement m_statement;
    private ResultSet m_result;

    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // update USER, PASS and DB URL according to credentials provided by the website:
    // https://remotemysql.com/
    // in future get those hardcoede string into separated config file.
    private final String DB = "n0LBO2gM5F";
    private final String DB_URL = "jdbc:mysql://remotemysql.com/"+ DB + "?useSSL=false";
    private final String USER = "n0LBO2gM5F";
    private final String PASS = "FYYphwfFm3";

    //endregion

}