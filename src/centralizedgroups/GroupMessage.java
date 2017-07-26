package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author Jeferson Arboleda
 */
public class GroupMessage implements Serializable{
    byte[] buffer;
    GroupMember creador;
    
    public GroupMessage(byte[] buffer, GroupMember creador){
        this.buffer = buffer;
        this.creador = creador;
    }
}
