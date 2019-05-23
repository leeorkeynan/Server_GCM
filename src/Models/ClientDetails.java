package Models;
import Models.Interfaces.IClientDetails;

public class ClientDetails implements IClientDetails
{
    //public int UserId;
    public String FirstName;
    public String LastName;
    public String UserName;
    public String Email;
    public String Phone;
//    public String Password;
//    public boolean HasLicense;
//    public int TimestampLicenseExp;

    @Override
    public String ToString() {
        String value = "UserName: " + UserName + "\n" +
                "First Name: " + FirstName + "\n" +
                "Last Name: " + LastName + "\n" +
                "UserName: " + UserName + "\n" +
                "Phone: " + Phone + "\n" +
                "Email:" + Email + "\n" ;
        return value;
    }
}