package centralizedgroups;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeferson Arboleda
 */
public class SendingMessage extends Thread {

    ObjectGroup grupo;
    GroupMember emisor;
    byte[] men;

    public SendingMessage(ObjectGroup grupo, GroupMember emisor, byte[] men) {
        super("SendingMessageThread");
        this.grupo = grupo;
        this.emisor = emisor;
        this.men = men;
    }

    @Override
    public void run() {
        for (int i = 0; i < grupo.listaMiembros.size(); i++) {
            if (!grupo.listaMiembros.get(i).alias.equals(this.emisor.alias)) {
                try {
                    //Look for the receivers registry
                    Registry registry = LocateRegistry.getRegistry(grupo.listaMiembros.get(i).hostname, 
                            grupo.listaMiembros.get(i).port);
                    
                    ClientInterface stub = (ClientInterface) registry.lookup(grupo.listaMiembros.get(i).alias);
                    GroupMessage mensaje = new GroupMessage(this.men, this.emisor);
                    //Thread.sleep(new Random().nextInt(30000) +30000);
                    stub.DepositMessage(mensaje);
                } catch (RemoteException | NotBoundException ex) {
                    System.out.println("Error al enviar: " + ex.toString());
                } 
//                catch (InterruptedException ex) {
//                    Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
        grupo.EndSending(emisor);
    }
}
