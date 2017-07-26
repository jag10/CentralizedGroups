package centralizedgroups;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeferson Arboleda
 */
public class ObjectGroup {

    ReentrantLock lock;
    String galias, oalias, ohostname;
    int gid, contador = 1, mensajes = 0;
    Condition bloqueo;
    boolean bloquear = false;
    ArrayList<GroupMember> listaMiembros = new ArrayList<>();

    public ObjectGroup(String galias, int gid, String oalias, String ohostname, int puerto) {
        this.galias = galias;
        this.gid = gid;
        this.oalias = oalias;
        this.ohostname = ohostname;

        this.lock = new ReentrantLock();
        bloqueo = lock.newCondition();
        this.addMember(oalias, ohostname, puerto);
    }

    public GroupMember isMember(String alias) {
        lock.lock();
        try {
            for (int i = 0; i < listaMiembros.size(); i++) {
                if (listaMiembros.get(i).alias.equals(alias)) {
                    return listaMiembros.get(i);
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    GroupMember addMember(String alias, String hostname, int puerto) {
        lock.lock();
        try {
            while (bloquear) {
                try {
                    bloqueo.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObjectGroup.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            GroupMember temp = new GroupMember(alias, hostname, contador, this.gid, puerto);
            if (isMember(alias) == null) {
                contador++;
                listaMiembros.add(temp);
                return temp;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public boolean removeMember(String alias) {
        lock.lock();
        try {
            while (bloquear) {
                try {
                    bloqueo.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObjectGroup.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            for (int i = 0; i < listaMiembros.size(); i++) {
                if (listaMiembros.get(i).alias.equals(alias)) {
                    listaMiembros.remove(i);
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean StopMembers() {
        lock.lock();
        try {
            bloquear = true;
        } finally {
            lock.unlock();
        }
        return true;
    }

    public boolean AllowMembers() {
        lock.lock();
        try {
            bloquear = false;
            this.bloqueo.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }
    
    public void Sending(){
        lock.lock();
        try {
            bloquear = true;
            mensajes++;
        } finally {
            lock.unlock();
        }
    }
    
    public void EndSending(GroupMember gm){
        lock.lock();
        try {
            bloquear = false;
            mensajes--;
        } finally {
            lock.unlock();
        }
    }
    
}
