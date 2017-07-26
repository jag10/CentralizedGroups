package centralizedgroups;

import java.net.MalformedURLException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jeferson Arboleda
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    ReentrantLock lock;
    Condition esperaMensaje;
    ArrayList<GroupMessage> cola;

    public Client() throws RemoteException {
        super();
        cola = new ArrayList<>();
        lock = new ReentrantLock();
        esperaMensaje = lock.newCondition();
    }

    public static void main(String args[]) throws RemoteException, MalformedURLException {
        String galiasV, oaliasV, hostnameV, aliasV, serverHostName, clientHostName;
        int gidV, opcion = 0, puerto;
        byte[] mensaje;
        Scanner sc = new Scanner(System.in);
        if (args.length > 0) {
            serverHostName = args[0];
        } else {
            serverHostName = "localhost";
        }

        if (args.length > 1) {
            clientHostName = args[1];
        } else {
            clientHostName = "localhost";
        }

        //System.setProperty("java.security.policy", "client-policy.txt");
        System.setProperty("java.security.policy", "server-policy.txt");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Registry registry = LocateRegistry.getRegistry(serverHostName, 1099);
            GroupServerInterface stub = (GroupServerInterface) registry.lookup("GroupServer");

            System.out.println("Introduzca su alias de cliente");
            oaliasV = sc.nextLine();
            System.out.println("Introduzca el puerto que va a usar");
            puerto = sc.nextInt();
            sc.nextLine();

            //Registry reg = LocateRegistry.createRegistry(puerto);
            Client c = new Client();
            registry.rebind(oaliasV, c);

            System.out.println("Cliente " + oaliasV + " listo");

            while (opcion != 8) {
                /////////////////////////////Menu/////////////////////////////
                System.out.println("Elige una opción: ");
                System.out.println("1.- Crear un grupo");
                System.out.println("2.- Eliminar un grupo a través de su galias");
                System.out.println("3.- Eliminar un grupo a través de su gid");
                System.out.println("4.- Añadir un miembro a un grupo");
                System.out.println("5.- Eliminar un miembro de un grupo");
                System.out.println("6.- Enviar un mensaje a un grupo");
                System.out.println("7.- Recibir mensajes");
                System.out.println("8.- Terminar ejecución");
                opcion = sc.nextInt();
                sc.nextLine(); //Para capturar salto de linea, nextInt solo captura el numero
                switch (opcion) {
                    case 1:
                        System.out.println("Inserta el galias del nuevo grupo: ");
                        galiasV = sc.nextLine();
                        int g = stub.createGroup(galiasV, oaliasV, clientHostName, puerto);
                        if (g != -1) {
                            System.out.println("\nGrupo " + g + " creado correctamente\n");
                        } else {
                            System.out.println("\nYa existe un grupo con este galias: " + galiasV + "\n");
                        }
                        break;
                    case 2:
                        System.out.println("Inserta el galias del grupo a borrar: ");
                        galiasV = sc.nextLine();
                        if (stub.removeGroup(galiasV, oaliasV)) {
                            System.out.println("\nGrupo " + galiasV + " borrado correctamente\n");
                        } else {
                            System.out.println("\nNo se ha encontrado el grupo con galias " + galiasV + " y oalias " + oaliasV + "\n");
                        }
                        break;
                    case 3:
                        System.out.println("Inserta el gid del grupo a borrar: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        if (stub.removeGroup(gidV, oaliasV)) {
                            System.out.println("\nGrupo " + gidV + " borrado correctamente\n");
                        } else {
                            System.out.println("\nNo se ha encontrado el grupo con gid " + gidV + " y oalias " + oaliasV + "\n");
                        }
                        break;
                    case 4:
                        System.out.println("Inserta el gid del grupo al que añadir un miembro: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Inserta el alias del miembro a añadir al grupo: ");
                        aliasV = sc.nextLine();
                        if (!aliasV.equals(oaliasV)) {
                            System.out.println("Inserta el puerto del miembro a añadir al grupo: ");
                            puerto = sc.nextInt();
                            sc.nextLine();
                        }
                        System.out.println("Inserta el hostname del miembro a añadir al grupo: ");
                        hostnameV = sc.nextLine();
                        if (stub.addMember(gidV, aliasV, hostnameV, puerto) != null) {
                            System.out.println("\nMiembro añadido correctamente\n");
                        } else {
                            System.out.println("\nError al añadir el miembro\n");
                        }
                        break;
                    case 5:
                        System.out.println("Inserta el gid del grupo al que borrar un miembro: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Inserta el alias del miembro a borrar: ");
                        aliasV = sc.nextLine();
                        if (stub.removeMember(gidV, aliasV)) {
                            System.out.println("\nMiembro " + aliasV + " borrado del grupo " + gidV + "\n");
                        } else {
                            System.out.println("\nNo existe un miembro " + aliasV + " en el grupo " + gidV + " o está "
                                    + "intentando borrar al propietario del grupo " + "\n");
                        }
                        break;
                    case 6:
                        System.out.println("Inserta el grupo al que perteneces y quieres enviar un mensaje: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Introduzca el mensaje que desea enviar: ");
                        if (stub.sendGroupMessage(stub.isMember(gidV, oaliasV), sc.nextLine().getBytes())) {
                            System.out.println("Mensaje enviado");
                        } else {
                            System.out.println("No pertenece al grupo o dicho grupo no existe");
                        }
                        break;
                    case 7:
                        System.out.println("De qué grupo al que pertenece desea leer los mensajes?");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        if (stub.isMember(gidV, oaliasV) == null) {
                            System.out.println("No es miembro de ése grupo o dicho grupo no existe");
                        } else {
                            mensaje = c.receiveGroupMessage(gidV);
                            String s = "";
                            if (mensaje != null) {
                                for (int i = 0; i < mensaje.length; i++) {
                                    s += (char) mensaje[i];
                                }
                                System.out.print("Ha recibido un mensaje del grupo " + gidV + ": " + s + "\n");
                            } else {
                                System.out.println("No tiene mensajes");
                            }
                        }
                        break;
                    case 8:
                        UnicastRemoteObject.unexportObject(registry, true);
                        break;
                }
            }
        } catch (InputMismatchException ex) {
            System.out.println("\nNo puede introducir caracteres en un gid\n");
            opcion = 8;
        } catch (NoSuchObjectException ex) {
            System.out.println("La ejecución del cliente ha finalizado");
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("NotBoundException" + ex);
        }
    }

    @Override    
    public void DepositMessage(GroupMessage m) throws RemoteException {
        lock.lock();
        try {
            this.cola.add(m);
            esperaMensaje.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte[] receiveGroupMessage(int gid) {
        lock.lock();
        boolean llegaMensaje = false;
        int index = 0;
        try {
            if (cola.isEmpty()) {
                System.out.println("No tiene mensajes del grupo " + gid + ". A la espera...");
                esperaMensaje.await();
            }
            while (!llegaMensaje) {
                for (index = 0; index < cola.size(); index++) {
                    if (cola.get(index).creador.gid == gid) {
                        llegaMensaje = true;
                        break;
                    }
                }
                if (index == cola.size()) {
                    System.out.println("No tiene mensajes del grupo " + gid + ". A la espera...");
                    esperaMensaje.await();
                }
            }
            return cola.remove(index).buffer;
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lock.unlock();
        }
        return null;
    }
}
