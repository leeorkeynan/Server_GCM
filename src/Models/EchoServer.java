// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package Models;

import Models.Interfaces.IClientDetails;
import Models.Interfaces.IDBHandler;
import Models.OCSF.server.AbstractServer;
import Models.OCSF.server.ConnectionToClient;
import Models.common.ChatIF;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer
{
  //Class variables *************************************************

  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT =5555;
  private IDBHandler DB;
  private  ConnectionToClient m_client;

  /**
   * The interface type variable. It allows the implementation of 
   * the display method in the client.
   */
  ChatIF serverUI;


  //Constructors ****************************************************

  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port)
  {
    super(port);
    DB = new DBHandler();
  }

  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   * @param serverUI The interface type variable.
   */
  public EchoServer(int port, ChatIF serverUI) throws IOException
  {
    super(port);
    this.serverUI = serverUI;
    DB = new DBHandler();
  }


  //Instance methods ************************************************

  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */


  public void handleMessageFromClient
  (Object msg, ConnectionToClient client) throws IOException {

    // args: login, username, password
    // response: login, success/failure , reason (1- permission? *** 0-client not exist, bad password, already connectd)

  if (msg instanceof ArrayList) {
    ArrayList MessageArray = (ArrayList) msg;
    if (MessageArray.get(0).equals("login")) {
      String username = MessageArray.get(1).toString();
      String password = MessageArray.get(2).toString();
      ArrayList<String> ans = DB.CheckUser(username,password);
      ArrayList<String> ans2 = new ArrayList<>(ans);
      client.sendToClient(ans2);
    }
  }

    if (msg.toString().startsWith("#login "))
    {
      if (client.getInfo("loginID") != null)
      {
        try
        {
          client.sendToClient("You are already logged in.");
        }
        catch (IOException e)
        {

        }
        return;
      }
      client.setInfo("loginID", msg.toString().substring(7));
    }
    else
    {
      if (client.getInfo("loginID") == null)
      {
        try
        {
          client.sendToClient("You need to login before you can chat.");
          client.close();
        }
        catch (IOException e) {}
        return;
      }


      //case 1
      if (msg.toString().split("_")[0].equals("GetPurchaseByID"))
      {
        try
        {
          String username = msg.toString().split("_")[1];
          client.sendToClient(username + "'s Purchases number: "+ DB.GetNumOfPurchases(username));
        }
        catch (IOException e) {}
        return;
      }

      //case 2

      if (msg.toString().split("_")[0].equals("IncreasePurchase"))
      {
        try
        {
          String username = msg.toString().split("_")[1];
          DB.IncreaseNumOfPurchases(username,1);
        }
        catch (Exception e)
        {
          System.out.println(e.getMessage());
        }
        return;
      }

      //case 3

      if (msg.toString().split("_")[0].equals("GetDetails"))
      {
        try
        {
          String username = msg.toString().split("_")[1];
          IClientDetails details = DB.GetClientDetails(username);
          client.sendToClient(username +"'s Details:\n" + details.ToString());
        }
        catch (IOException e) {}
        return;
      }

      //case 4
      if (msg.toString().split("_")[0].equals("IsUsernameExists"))
      {
        try
        {
          String username = msg.toString().split("_")[1];
          String password = msg.toString().split("_")[2];
          client.sendToClient(DB.IsUsernameExists(username,password));
        }
        catch (IOException e) {}
        return;
      }

      System.out.println("Message received: " + msg + " from \"" +
              client.getInfo("loginID") + "\" " + client);
      //this.sendToAllClients(client.getInfo("loginID") + "> " + msg);


    }
  }

  /**
   * This method handles all data coming from the UI
   *
   * @param message The message from the UI
   */
  public void handleMessageFromServerUI(String message) throws IOException {
    if (message.charAt(0) == '#')
    {
      runCommand(message);
    }
    else
    {
      // send message to clients
      serverUI.display(message);
      this.sendToAllClients("SERVER MSG> " + message);
      handleMessageFromClient(message, m_client);
    }
  }

  /**
   * This method executes server commands.
   *
   * @param message String from the server console.
   */
  private void runCommand(String message)
  {
    // run commands
    // a series of if statements

    if (message.equalsIgnoreCase("#quit"))
    {
      quit();
    }
    else if (message.equalsIgnoreCase("#stop"))
    {
      stopListening();
    }
    else if (message.equalsIgnoreCase("#close"))
    {
      try
      {
        close();
      }
      catch(IOException e) {}
    }
    else if (message.toLowerCase().startsWith("#setport"))
    {
      if (getNumberOfClients() == 0 && !isListening())
      {
        // If there are no connected clients and we are not 
        // listening for new ones, assume server closed.
        // A more exact way to determine this was not obvious and
        // time was limited.
        int newPort = Integer.parseInt(message.substring(9));
        setPort(newPort);
        //error checking should be added
        serverUI.display
                ("Server port changed to " + getPort());
      }
      else
      {
        serverUI.display
                ("The server is not closed. Port cannot be changed.");
      }
    }
    else if (message.equalsIgnoreCase("#start"))
    {
      if (!isListening())
      {
        try
        {
          listen();
        }
        catch(Exception ex)
        {
          serverUI.display("Error - Could not listen for clients!");
        }
      }
      else
      {
        serverUI.display
                ("The server is already listening for clients.");
      }
    }
    else if (message.equalsIgnoreCase("#getport"))
    {
      serverUI.display("Currently port: " + Integer.toString(getPort()));
    }
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
            ("Server listening for connections on port " + getPort());
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
            ("Server has stopped listening for connections.");
  }

  /**
   * Run when new clients are connected. Implemented by Benjamin Bergman,
   * Oct 22, 2009.
   *
   * @param client the connection connected to the client
   */
  protected void clientConnected(ConnectionToClient client)
  {
    // display on server and clients that the client has connected.
    String msg = "A Client has connected";
    System.out.println(msg);
    m_client = client;

  }

  /**
   * Run when clients disconnect. Implemented by Benjamin Bergman,
   * Oct 22, 2009
   *
   * @param client the connection with the client
   */
  synchronized protected void clientDisconnected(
          ConnectionToClient client)
  {
    // display on server and clients when a user disconnects
    String msg = client.getInfo("loginID").toString() + " has disconnected";

    System.out.println(msg);
    //this.sendToAllClients(msg);
  }

  /**
   * Run when a client suddenly disconnects. Implemented by Benjamin
   * Bergman, Oct 22, 2009
   *
   * @param client the client that raised the exception
   * @param //Throwable the exception thrown
   */
  synchronized protected void clientException(
          ConnectionToClient client, Throwable exception)
  {
    String msg = client.getInfo("loginID").toString() + " has disconnected";

    System.out.println(msg);
    //this.sendToAllClients(msg);
  }

  /**
   * This method terminates the server.
   */
  public void quit()
  {
    try
    {
      close();
    }
    catch(IOException e)
    {
    }
    System.exit(0);
  }


  //Class methods ***************************************************

  /**
   * This method is responsible for the creation of
   * the server instance (there is no UI in this phase).
   *
   * @param //args[0] The port number to listen on.  Defaults to 5555
   *          if no argument is entered.
   */
  public static void main(String[] args)
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }

    EchoServer sv = new EchoServer(port);

    try
    {
      sv.listen(); //Start listening for connections
    }
    catch (Exception ex)
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
