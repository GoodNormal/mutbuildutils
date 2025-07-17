package mut.buildup.mutbuildutils.invite;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InviteManager {
    private static final Map<String, InviteRequest> pendingInvites = new ConcurrentHashMap<>();
    
    /**
     * 添加邀请请求
     * @param inviterName 邀请者名称
     * @param targetPlayerName 被邀请玩家名称
     * @param worldName 目标世界名称
     */
    public static void addInviteRequest(String inviterName, String targetPlayerName, String worldName) {
        InviteRequest request = new InviteRequest(inviterName, targetPlayerName, worldName);
        pendingInvites.put(inviterName, request);
    }
    
    /**
     * 获取邀请请求
     * @param inviterName 邀请者名称
     * @return 邀请请求，如果不存在则返回null
     */
    public static InviteRequest getInviteRequest(String inviterName) {
        return pendingInvites.get(inviterName);
    }
    
    /**
     * 移除邀请请求
     * @param inviterName 邀请者名称
     * @return 被移除的邀请请求，如果不存在则返回null
     */
    public static InviteRequest removeInviteRequest(String inviterName) {
        return pendingInvites.remove(inviterName);
    }
    
    /**
     * 检查是否存在邀请请求
     * @param inviterName 邀请者名称
     * @return 如果存在则返回true
     */
    public static boolean hasInviteRequest(String inviterName) {
        return pendingInvites.containsKey(inviterName);
    }
    
    /**
     * 获取所有待审核的邀请请求
     * @return 所有邀请请求的副本
     */
    public static Map<String, InviteRequest> getAllPendingInvites() {
        return new HashMap<>(pendingInvites);
    }
    
    /**
     * 清理过期的邀请请求（超过30分钟）
     */
    public static void cleanupExpiredInvites() {
        long currentTime = System.currentTimeMillis();
        long expireTime = 30 * 60 * 1000; // 30分钟
        
        pendingInvites.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > expireTime
        );
    }
}