package mut.buildup.mutbuildutils.menu;

import mut.buildup.mutbuildutils.config.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class OwnWorldMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // 检查是否是自己的世界菜单
        if (!OwnWorldMenu.isOwnWorldMenu(title)) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        // 处理导航按钮
        if (slot == 45) { // 上一页
            int currentPage = OwnWorldMenu.getCurrentPage(title);
            if (title.contains("的世界列表") && !title.startsWith("§6我的世界列表")) {
                // 玩家世界菜单
                String targetPlayerName = extractPlayerNameFromTitle(title);
                if (currentPage > 0) {
                    OwnWorldMenu.openPlayerWorldMenu(player, targetPlayerName, currentPage - 1);
                }
            } else {
                // 自己的世界菜单
                if (currentPage > 0) {
                    OwnWorldMenu.openOwnWorldMenu(player, currentPage - 1);
                }
            }
            return;
        }
        
        if (slot == 48) { // 返回按钮（仅在玩家世界菜单中）
            if (title.contains("的世界列表") && !title.startsWith("§6我的世界列表")) {
                PlayerManagementMenu.openPlayerManagementMenu(player);
            }
            return;
        }
        
        if (slot == 49) { // 关闭按钮
            player.closeInventory();
            return;
        }
        
        if (slot == 53) { // 下一页
            int currentPage = OwnWorldMenu.getCurrentPage(title);
            if (title.contains("的世界列表") && !title.startsWith("§6我的世界列表")) {
                // 玩家世界菜单
                String targetPlayerName = extractPlayerNameFromTitle(title);
                OwnWorldMenu.openPlayerWorldMenu(player, targetPlayerName, currentPage + 1);
            } else {
                // 自己的世界菜单
                OwnWorldMenu.openOwnWorldMenu(player, currentPage + 1);
            }
            return;
        }
        
        // 处理世界项目点击
        if (slot < 45 && clickedItem.getType() == Material.GRASS_BLOCK) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || meta.getDisplayName() == null) return;
            
            String worldName = ChatColor.stripColor(meta.getDisplayName());
            World world = Bukkit.getWorld(worldName);
            
            if (event.getClick() == ClickType.LEFT) {
                // 左键传送
                if (world != null) {
                    player.closeInventory();
                    player.teleport(world.getSpawnLocation());
                    player.sendMessage(ChatColor.GREEN + "已传送到世界: " + worldName);
                } else {
                    player.sendMessage(ChatColor.RED + "世界 " + worldName + " 当前未加载，无法传送！");
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                // 右键查看被邀请玩家列表
                if (title.startsWith("§6我的世界列表")) {
                    // 只有在自己的世界菜单中才能查看被邀请玩家
                    WorldPlayerMenu.openWorldPlayerMenu(player, worldName);
                } else {
                    // 在其他玩家的世界菜单中，右键查看详情
                    showWorldDetails(player, worldName);
                }
            }
        }
    }
    
    private String extractPlayerNameFromTitle(String title) {
        // 从标题中提取玩家名称，例如从 "§6Goodnormal的世界列表 - 第1页" 中提取 "Goodnormal"
        if (title.contains("的世界列表")) {
            String[] parts = title.split("的世界列表");
            if (parts.length > 0) {
                return ChatColor.stripColor(parts[0]).trim();
            }
        }
        return "";
    }
    
    private void showWorldDetails(Player player, String worldName) {
        player.closeInventory();
        
        World world = Bukkit.getWorld(worldName);
        
        player.sendMessage(ChatColor.GOLD + "=== 世界详情: " + worldName + " ===");
        player.sendMessage(ChatColor.YELLOW + "世界名称: " + ChatColor.WHITE + worldName);
        
        if (world != null) {
            player.sendMessage(ChatColor.YELLOW + "状态: " + ChatColor.GREEN + "已加载");
            player.sendMessage(ChatColor.YELLOW + "环境: " + ChatColor.WHITE + world.getEnvironment().name());
            player.sendMessage(ChatColor.YELLOW + "玩家数量: " + ChatColor.WHITE + world.getPlayers().size());
            player.sendMessage(ChatColor.YELLOW + "出生点: " + ChatColor.WHITE + 
                world.getSpawnLocation().getBlockX() + ", " + 
                world.getSpawnLocation().getBlockY() + ", " + 
                world.getSpawnLocation().getBlockZ());
        } else {
            player.sendMessage(ChatColor.YELLOW + "状态: " + ChatColor.RED + "未加载");
        }
        
        player.sendMessage(ChatColor.GRAY + "使用 /world ownlist 重新打开菜单");
    }
}