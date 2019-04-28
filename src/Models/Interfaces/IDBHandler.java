package Models.Interfaces;

public interface IDBHandler
{
    //region Methods
    boolean HandleMassege(IMessage message);


    void Init();
    boolean IsConnectedToDB();
    IDBData GetResoultsFromDB();
    //endregion

    int GetNumOfPurchases(String p_userName);
    void IncreaseNumOfPurchases(String p_userName, int p_value);
    IClientDetails GetClientDetails(String p_userName);
    boolean IsUsernameExists(String p_username, String p_password);

}
