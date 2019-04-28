package Models;
import Models.Interfaces.IClientDetails;

public class ClientDetails implements IClientDetails
{
    public int userId;
    public String FirstName;
    public String LastName;
    public String UserName;
    //public String Password;
    public boolean HasLicense;
    public int TimestampLicenseExp;
    public int NumOfPurchase;

    @Override
    public String ToString() {
        String value = "UserID: " + userId + "\n" +
                "First Name: " + FirstName + "\n" +
                "Last Name: " + LastName + "\n" +
                "UserName: " + UserName + "\n" +
                "LicendeExp: " + TimestampLicenseExp + "\n" +
                "Num Of Purchases: " + NumOfPurchase;
        return value;
    }
}
