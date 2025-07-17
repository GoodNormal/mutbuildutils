package mut.buildup.mutbuildutils.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import mut.buildup.mutbuildutils.config.WorldConfig;
import mut.buildup.mutbuildutils.invite.InviteManager;
import mut.buildup.mutbuildutils.invite.InviteRequest;

import java.util.Map;

public class WorldAccessListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        String worldName = event.getTo().getWorld().getName();
        
        // 跳过主世界、下界和末地的检查
        if (isDefaultWorld(worldName)) {
            return;
        }
        
        // 检查玩家是否有权限进入该世界
        if (!WorldConfig.canPlayerEnterWorld(worldName, player.getName()) && 
            !player.hasPermission("mutbuildutils.world.admin") && 
            !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§c你没有权限进入世界 '" + worldName + "'！"));
            player.sendMessage(Component.text("§7请联系管理员或通过邀请系统获得进入权限。"));
            player.sendMessage(Component.text("§7提示：使用 /world invite <玩家名> 来邀请其他玩家进入你的世界。"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 跳过主世界、下界和末地的检查
        if (isDefaultWorld(worldName)) {
            return;
        }
        
        // 检查玩家是否有权限进入该世界
        if (!WorldConfig.canPlayerEnterWorld(worldName, player.getName()) && 
            !player.hasPermission("mutbuildutils.world.admin") && 
            !player.isOp()) {
            // 将玩家传送回主世界
            player.teleport(event.getFrom().getSpawnLocation());
            player.sendMessage(Component.text("§c你没有权限进入世界 '" + worldName + "'！"));
            player.sendMessage(Component.text("§7请联系管理员或通过邀请系统获得进入权限。"));
            player.sendMessage(Component.text("§7提示：使用 /world invite <玩家名> 来邀请其他玩家进入你的世界。"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 在默认世界中，只有OP和管理员可以破坏方块
        if (isDefaultWorld(worldName)) {
            if (!player.isOp() && !player.hasPermission("mutbuildutils.world.admin")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("§c你没有权限在默认世界破坏方块！"));
            }
            return;
        }
        
        // 在其他世界中，检查玩家是否有权限
        if (!WorldConfig.canPlayerEnterWorld(worldName, player.getName()) && 
            !player.hasPermission("mutbuildutils.world.admin") && 
            !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§c你没有权限在世界 '" + worldName + "' 中破坏方块！"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 在默认世界中，只有OP和管理员可以放置方块
        if (isDefaultWorld(worldName)) {
            if (!player.isOp() && !player.hasPermission("mutbuildutils.world.admin")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("§c你没有权限在默认世界放置方块！"));
            }
            return;
        }
        
        // 在其他世界中，检查玩家是否有权限
        if (!WorldConfig.canPlayerEnterWorld(worldName, player.getName()) && 
            !player.hasPermission("mutbuildutils.world.admin") && 
            !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§c你没有权限在世界 '" + worldName + "' 中放置方块！"));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否为OP或管理员
        if (player.isOp() || player.hasPermission("mutbuildutils.world.admin")) {
            // 延迟3秒后检查待审核邀请，避免登录时消息过多
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                mut.buildup.mutbuildutils.MUTbuildUtils.getInstance(), 
                () -> checkPendingInvites(player), 
                60L // 3秒 = 60 ticks
            );
        }
    }
    
    /**
     * 检查并提醒OP待审核的邀请
     */
    private void checkPendingInvites(Player op) {
        Map<String, InviteRequest> pendingInvites = InviteManager.getAllPendingInvites();
        
        if (!pendingInvites.isEmpty()) {
            op.sendMessage(Component.text("§6=== 待审核邀请提醒 ==="));
            op.sendMessage(Component.text("§f你有 §e" + pendingInvites.size() + "§f 个待审核的邀请申请："));
            
            for (Map.Entry<String, InviteRequest> entry : pendingInvites.entrySet()) {
                InviteRequest request = entry.getValue();
                op.sendMessage(Component.text("§7- 邀请者: §e" + request.getInviterName() + 
                    "§7, 被邀请玩家: §e" + request.getTargetPlayerName() + 
                    "§7, 世界: §e" + request.getWorldName()));
            }
            
            op.sendMessage(Component.text("§f使用 §a/world approve <邀请者>§f 同意邀请"));
            op.sendMessage(Component.text("§f使用 §c/world deny <邀请者>§f 拒绝邀请"));
        }
    }
    
    /**
     * 检查是否为默认世界（主世界、下界、末地）
     */
    private boolean isDefaultWorld(String worldName) {
        return worldName.equals("world") || 
               worldName.equals("world_nether") || 
               worldName.equals("world_the_end") ||
               worldName.startsWith("world") && (worldName.endsWith("_nether") || worldName.endsWith("_the_end"));
    }
}