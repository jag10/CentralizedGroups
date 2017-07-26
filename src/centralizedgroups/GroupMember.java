package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author Jeferson Arboleda
 */
public class GroupMember implements Serializable{

    String alias, hostname;
    private int idmem;
    int gid, port;

    public GroupMember(String alias, String hostname, int idmem, int gid, int port) {
        this.alias = alias;
        this.hostname = hostname;
        this.idmem = idmem;
        this.gid = gid;
        this.port = port;
    }
}
