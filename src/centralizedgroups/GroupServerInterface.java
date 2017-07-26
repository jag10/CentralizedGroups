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
public interface GroupServerInterface extends Remote{

    int createGroup(String galias, String oalias, String ohostname, int puerto) throws RemoteException;
    
    int findGroup(String galias) throws RemoteException;
    
    boolean removeGroup(String galias, String oalias) throws RemoteException;
    
    boolean removeGroup(int gid, String oalias) throws RemoteException;
    
    GroupMember addMember(int gid, String alias, String hostname, int puerto) throws RemoteException;
    
    boolean removeMember(int gid, String alias) throws RemoteException;
    
    GroupMember isMember(int gid, String alias) throws RemoteException;
    
//    boolean StopMembers(int gid) throws RemoteException;
//    
//    boolean AllowMembers(int gid) throws RemoteException;
    
    boolean sendGroupMessage (GroupMember gm, byte[] msg) throws RemoteException;
    
}
