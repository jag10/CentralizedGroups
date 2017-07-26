package centralizedgroups;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Jeferson Arboleda
 */
//http://docs.oracle.com/javase/tutorial/rmi/server.html
public class GroupServer extends UnicastRemoteObject implements GroupServerInterface {

    private ArrayList<ObjectGroup> groupList;
    private int id;
    private ReentrantLock lock;

    public GroupServer() throws RemoteException {
        super();
        groupList = new ArrayList<ObjectGroup>();
        id = 0;
        this.lock = new ReentrantLock();
    }

    @Override
    public int createGroup(String galias, String oalias, String ohostname, int puerto) throws RemoteException {
        int h = -1;
        lock.lock();
        try {
            for (int i = 0; i < groupList.size(); i++) {
                if (!groupList.isEmpty() && groupList.get(i).galias.equals(galias)) {
                    return -1;
                }
            }
            ObjectGroup g = new ObjectGroup(galias, id, oalias, ohostname, puerto);
            groupList.add(g);
            id++;
            h = g.gid;
        } finally {
            lock.unlock();
        }
        return h;
    }

    @Override
    public int findGroup(String galias) throws RemoteException {
        lock.lock();
        try {
            for (int i = 0; i < groupList.size(); i++) {
                if (!groupList.isEmpty() && groupList.get(i).galias.equals(galias)) {
                    return i;
                }
            }
        } finally {
            lock.unlock();
        }
        return -1;
    }

    @Override
    public boolean removeGroup(String galias, String oalias) throws RemoteException {
        lock.lock();
        for (int i = 0; i < groupList.size(); i++) {
            if (!groupList.isEmpty() && groupList.get(i).galias.equals(galias) && groupList.get(i).oalias.equals(oalias)) {
                lock.unlock();
                groupList.remove(i);
                return true;
            }
        }
        lock.unlock();
        return false;
    }

    @Override
    public boolean removeGroup(int gid, String oalias) throws RemoteException {
        lock.lock();
        for (int i = 0; i < groupList.size(); i++) {
            if (!groupList.isEmpty() && groupList.get(i).gid == gid && groupList.get(i).oalias.equals(oalias)) {
                lock.unlock();
                groupList.remove(i);
                return true;
            }
        }
        lock.unlock();
        return false;
    }

    @Override
    public GroupMember addMember(int gid, String alias, String hostname, int puerto) throws RemoteException {
        lock.lock();
        for (int i = 0; i < groupList.size(); i++) {
            if (!groupList.isEmpty() && groupList.get(i).gid == gid && groupList.get(i).isMember(alias) == null) {
                lock.unlock();
                return groupList.get(i).addMember(alias, hostname, puerto);
            }
        }
        lock.unlock();
        return null;
    }

    @Override
    public boolean removeMember(int gid, String alias) throws RemoteException {
        lock.lock();
        for (int i = 0; i < groupList.size(); i++) {
            if (!groupList.isEmpty() && groupList.get(i).gid == gid && groupList.get(i).isMember(alias) != null
                    && !groupList.get(i).oalias.equals(alias)) {
                lock.unlock();
                return groupList.get(i).removeMember(alias);
            }
        }
        lock.unlock();
        return false;
    }

    @Override
    public GroupMember isMember(int gid, String alias) throws RemoteException {
        lock.lock();
        int i;
        try {
            for (i = 0; i < groupList.size(); i++) {
                if (!groupList.isEmpty() && groupList.get(i).gid == gid) {
                    for (int j = 0; j < groupList.get(i).listaMiembros.size(); j++) {
                        if (groupList.get(i).listaMiembros.get(j).alias.equals(alias)) {
                            return groupList.get(i).listaMiembros.get(j);
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

//    @Override
//    public boolean StopMembers(int gid) throws RemoteException {
//        lock.lock();
//        int i;
//        for (i = 0; i < groupList.size(); i++) {
//            if (!groupList.isEmpty() && groupList.get(i).gid == gid) {
//                lock.unlock();
//                groupList.get(i).StopMembers();
//                return true;
//            }
//        }
//        lock.unlock();
//        return false;
//    }
//
//    @Override
//    public boolean AllowMembers(int gid) throws RemoteException {
//        lock.lock();
//        int i;
//        for (i = 0; i < groupList.size(); i++) {
//            if (!groupList.isEmpty() && groupList.get(i).gid == gid) {
//                lock.unlock();
//                groupList.get(i).AllowMembers();
//                return true;
//            }
//        }
//        lock.unlock();
//        return false;
//    }
    @Override
    public boolean sendGroupMessage(GroupMember gm, byte[] msg) throws RemoteException {
        lock.lock();
        try {
            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).gid == gm.gid) {
                    groupList.get(i).Sending();
                    SendingMessage h = new SendingMessage(groupList.get(i), gm, msg);
                    h.start();
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public static void main(String args[]) {
        try {
            String hostname;
            if (args.length > 0) {
                hostname = args[0];
            } else {
                hostname = "localhost";
            }
            System.setProperty("java.security.policy", "server-policy.txt");
            //System.setProperty("java.rmi.server.hostname", hostname);
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            Registry reg = LocateRegistry.createRegistry(1099);
            GroupServer obj = new GroupServer();
            reg.rebind("GroupServer", obj);

            System.out.println("Server ready");
        } catch (RemoteException ex) {
            System.err.println("Server exception: " + ex.toString());
        }
    }
}
