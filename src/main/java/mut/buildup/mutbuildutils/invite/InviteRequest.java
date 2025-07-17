package mut.buildup.mutbuildutils.invite;

public class InviteRequest {
    private final String inviterName;
    private final String targetPlayerName;
    private final String worldName;
    private final long timestamp;
    
    public InviteRequest(String inviterName, String targetPlayerName, String worldName) {
        this.inviterName = inviterName;
        this.targetPlayerName = targetPlayerName;
        this.worldName = worldName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getInviterName() {
        return inviterName;
    }
    
    public String getTargetPlayerName() {
        return targetPlayerName;
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "InviteRequest{" +
                "inviter='" + inviterName + '\'' +
                ", target='" + targetPlayerName + '\'' +
                ", world='" + worldName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}