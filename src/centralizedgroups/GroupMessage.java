package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author Jeferson Arboleda
 */
public class GroupMessage implements Serializable{
    byte[] buffer;
    GroupMember creator;
    
    public GroupMessage(byte[] buffer, GroupMember creator){
        this.buffer = buffer;
        this.creator = creator;
    }
}
