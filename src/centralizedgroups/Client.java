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

    private ReentrantLock lock;
    private Condition waitForMessage;
    private static int port = 1099;
    ArrayList<GroupMessage> msgQueue;

    public Client() throws RemoteException {
        super();
        msgQueue = new ArrayList<>();
        lock = new ReentrantLock();
        waitForMessage = lock.newCondition();
    }

    public static void main(String args[]) throws RemoteException, MalformedURLException {
        String galiasV, oaliasV, hostnameV, aliasV, serverHostName, clientHostName;
        int gidV, option = 0;
        byte[] msg;
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
            Registry registry = LocateRegistry.getRegistry(serverHostName, port);
            GroupServerInterface stub = (GroupServerInterface) registry.lookup("GroupServer");

            System.out.println("Type your client alias");
            oaliasV = sc.nextLine();
            /*System.out.println("Introduzca el puerto que va a usar");
            port = sc.nextInt();
            sc.nextLine();*/

            //Registry reg = LocateRegistry.createRegistry(puerto);
            Client c = new Client();
            registry.rebind(oaliasV, c);

            System.out.println("Client " + oaliasV + " ready");

            while (option != 8) {
                /////////////////////////////Menu/////////////////////////////
                System.out.println("Choose an option: ");
                System.out.println("1.- Create a group");
                System.out.println("2.- Delete a group through its group alias");
                System.out.println("3.- Delete a group through its group id");
                System.out.println("4.- Add a member to a group");
                System.out.println("5.- Remove a member from a group");
                System.out.println("6.- Send a message to a group");
                System.out.println("7.- Receive messages");
                System.out.println("8.- Exit");
                option = sc.nextInt();
                sc.nextLine(); //Get newline
                switch (option) {
                    case 1:
                        System.out.println("Type the group alias for the new group: ");
                        galiasV = sc.nextLine();
                        int g = stub.createGroup(galiasV, oaliasV, clientHostName, port);
                        if (g != -1) {
                            System.out.println("\nGroup " + g + " was created\n");
                        } else {
                            System.out.println("\nA group with this group alias already exists: " + galiasV + "\n");
                        }
                        break;
                    case 2:
                        System.out.println("Type the group alias of the group to delete: ");
                        galiasV = sc.nextLine();
                        if (stub.removeGroup(galiasV, oaliasV)) {
                            System.out.println("\nGroup " + galiasV + " removed\n");
                        } else {
                            System.out.println("\nCouldn't find the group with group alias " + galiasV + " and oalias " + oaliasV + "\n");
                        }
                        break;
                    case 3:
                        System.out.println("Type the group id of the group you want to remove: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        if (stub.removeGroup(gidV, oaliasV)) {
                            System.out.println("\nGroup " + gidV + " removed\n");
                        } else {
                            System.out.println("\nCouldn't find the group with group id " + gidV + " and oalias " + oaliasV + "\n");
                        }
                        break;
                    case 4:
                        System.out.println("Type the group id ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Type the alias of the member you want to add: ");
                        aliasV = sc.nextLine();
                        if (!aliasV.equals(oaliasV)) {
                            System.out.println("Type the port of the member: ");
                            port = sc.nextInt();
                            sc.nextLine();
                        }
                        System.out.println("Type the member's hostname: ");
                        hostnameV = sc.nextLine();
                        if (stub.addMember(gidV, aliasV, hostnameV, port) != null) {
                            System.out.println("\nMember added\n");
                        } else {
                            System.out.println("\nSomething went wrong\n");
                        }
                        break;
                    case 5:
                        System.out.println("Type the group id: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Type the member's alias: ");
                        aliasV = sc.nextLine();
                        if (stub.removeMember(gidV, aliasV)) {
                            System.out.println("\nMember " + aliasV + " removed from the group " + gidV + "\n");
                        } else {
                            System.out.println("\nThere is no member with alias " + aliasV + " in the group " + gidV + " or you are "
                                    + "trying to remove the owner of the group " + "\n");
                        }
                        break;
                    case 6:
                        System.out.println("Type the group: ");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        System.out.println("Type the message: ");
                        if (stub.sendGroupMessage(stub.isMember(gidV, oaliasV), sc.nextLine().getBytes())) {
                            System.out.println("Message sent");
                        } else {
                            System.out.println("You don't belong to this group or it doesn't exist");
                        }
                        break;
                    case 7:
                        System.out.println("Type the group for which you want to read your messages");
                        gidV = sc.nextInt();
                        sc.nextLine();
                        if (stub.isMember(gidV, oaliasV) == null) {
                            System.out.println("You don't belong to that group or it doesn't exist");
                        } else {
                            msg = c.receiveGroupMessage(gidV);
                            String s = "";
                            if (msg != null) {
                                for (int i = 0; i < msg.length; i++) {
                                    s += (char) msg[i];
                                }
                                System.out.print("You have received a message from the group " + gidV + ": " + s + "\n");
                            } else {
                                System.out.println("You don't have any messages");
                            }
                        }
                        break;
                    case 8:
                        UnicastRemoteObject.unexportObject(registry, true);
                        break;
                }
            }
        } catch (InputMismatchException ex) {
            System.out.println("\nYou can't use characters in a group id\n");
            option = 8;
        } catch (NoSuchObjectException ex) {
            System.out.println("The execution of the client has ended");
        } catch (RemoteException | NotBoundException ex) {
            System.out.println("NotBoundException" + ex);
        }
    }

    @Override    
    public void DepositMessage(GroupMessage m) throws RemoteException {
        lock.lock();
        try {
            this.msgQueue.add(m);
            waitForMessage.signal();
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
            if (msgQueue.isEmpty()) {
                System.out.println("You don't have any messages from the group " + gid + ". Waiting...");
                waitForMessage.await();
            }
            while (!llegaMensaje) {
                for (index = 0; index < msgQueue.size(); index++) {
                    if (msgQueue.get(index).creator.gid == gid) {
                        llegaMensaje = true;
                        break;
                    }
                }
                if (index == msgQueue.size()) {
                    System.out.println("You don't have any messages from the group " + gid + ". Waiting...");
                    waitForMessage.await();
                }
            }
            return msgQueue.remove(index).buffer;
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            lock.unlock();
        }
        return null;
    }
}
