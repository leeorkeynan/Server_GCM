package Models.Interfaces;

public interface IMessage
{
    //region Properties

    String[] Content = null;
    long MessageID = 0;

    //endregion

    //region Methods

    String ToString();

    //endregion
}
