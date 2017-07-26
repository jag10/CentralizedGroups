/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package centralizedgroups;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Jeferson Arboleda
 */
public interface ClientInterface extends Remote{
    
    void DepositMessage(GroupMessage m) throws RemoteException;
    
    byte[] receiveGroupMessage(int galias) throws RemoteException;
}
