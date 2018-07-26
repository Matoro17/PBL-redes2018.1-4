import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Newsinterface extends Remote {
    public float point (int code) throws RemoteException;
    public void classify(int code) throws RemoteException;

}
