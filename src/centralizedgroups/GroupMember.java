package centralizedgroups;

import java.io.Serializable;

/**
 *
 * @author Jeff
 */
public class GroupMember implements Serializable{

    String alias, hostname;
    int idmem, gid, puerto;

    public GroupMember(String alias, String hostname, int idmem, int gid, int puerto) {
        this.alias = alias;
        this.hostname = hostname;
        this.idmem = idmem;
        this.gid = gid;
        this.puerto = puerto;
    }
}
