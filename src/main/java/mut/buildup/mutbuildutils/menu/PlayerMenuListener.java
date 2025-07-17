package mut.buildup.mutbuildutils.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // 检查是否是玩家管理菜单
        if (!title.contains("玩家管理菜单")) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        // 处理导航按钮
        if (slot == 45) { // 上一页
            int currentPage = PlayerManagementMenu.getCurrentPage(title);
            if (currentPage > 1) {
                PlayerManagementMenu.openPlayerManagementMenu(player, currentPage - 1);
            }
            return;
        }
        
        if (slot == 49) { // 关闭按钮
            player.closeInventory();
            return;
        }
        
        if (slot == 53) { // 下一页
            int currentPage = PlayerManagementMenu.getCurrentPage(title);
            PlayerManagementMenu.openPlayerManagementMenu(player, currentPage + 1);
            return;
        }
        
        // 处理玩家头颅点击
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;
            
            OfflinePlayer targetPlayer = meta.getOwningPlayer();
            
            if (event.getClick() == ClickType.LEFT) {
                // 左键查看玩家的世界列表
                player.closeInventory();
                OwnWorldMenu.openPlayerWorldMenu(player, targetPlayer.getName(), 1);
            } else if (event.getClick() == ClickType.RIGHT) {
                // 右键传送到玩家
                teleportToPlayer(player, targetPlayer);
            }
        }
    }
    
    private void showPlayerDetails(Player viewer, OfflinePlayer target) {
        viewer.closeInventory();
        
        viewer.sendMessage(Component.text("§6=== 玩家详情: " + target.getName() + " ==="));
        viewer.sendMessage(Component.text("§7UUID: §f" + target.getUniqueId().toString()));
        viewer.sendMessage(Component.text("§7状态: " + (target.isOnline() ? "§a在线" : "§c离线")));
        
        if (target.getLastPlayed() > 0) {
            long lastSeen = System.currentTimeMillis() - target.getLastPlayed();
            String timeAgo = formatTime(lastSeen);
            viewer.sendMessage(Component.text("§7最后登录: §f" + timeAgo + "前"));
        }
        
        if (target.isOnline()) {
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                Location loc = onlineTarget.getLocation();
                viewer.sendMessage(Component.text("§7当前位置: §f" + 
                    loc.getWorld().getName() + " (" + 
                    loc.getBlockX() + ", " + 
                    loc.getBlockY() + ", " + 
                    loc.getBlockZ() + ")"));
            }
        }
        
        viewer.sendMessage(Component.text("§7使用 /mutbuild player 重新打开菜单"));
    }
    
    private void teleportToPlayer(Player viewer, OfflinePlayer target) {
        if (!target.isOnline()) {
            viewer.sendMessage(Component.text("§c玩家 " + target.getName() + " 当前不在线！"));
            return;
        }
        
        Player onlineTarget = target.getPlayer();
        if (onlineTarget == null) {
            viewer.sendMessage(Component.text("§c无法找到玩家 " + target.getName() + "！"));
            return;
        }
        
        viewer.closeInventory();
        viewer.teleport(onlineTarget.getLocation());
        viewer.sendMessage(Component.text("§a已传送到玩家 " + target.getName() + " 的位置！"));
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "天";
        } else if (hours > 0) {
            return hours + "小时";
        } else if (minutes > 0) {
            return minutes + "分钟";
        } else {
            return seconds + "秒";
        }
    }
}