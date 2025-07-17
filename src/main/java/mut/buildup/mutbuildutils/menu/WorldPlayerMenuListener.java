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
import org.bukkit.inventory.meta.SkullMeta;

public class WorldPlayerMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // 检查是否是世界玩家菜单
        if (!WorldPlayerMenu.isWorldPlayerMenu(title)) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        String worldName = WorldPlayerMenu.extractWorldName(title);
        
        // 处理导航按钮
        if (slot == 45) { // 上一页
            int currentPage = WorldPlayerMenu.getCurrentPage(title);
            if (currentPage > 0) {
                WorldPlayerMenu.openWorldPlayerMenu(player, worldName, currentPage - 1);
            }
            return;
        }
        
        if (slot == 48) { // 返回按钮
            OwnWorldMenu.openOwnWorldMenu(player);
            return;
        }
        
        if (slot == 49) { // 关闭按钮
            player.closeInventory();
            return;
        }
        
        if (slot == 53) { // 下一页
            int currentPage = WorldPlayerMenu.getCurrentPage(title);
            WorldPlayerMenu.openWorldPlayerMenu(player, worldName, currentPage + 1);
            return;
        }
        
        // 处理玩家头颅点击
        if (slot < 45 && clickedItem.getType() == Material.PLAYER_HEAD) {
            if (!(clickedItem.getItemMeta() instanceof SkullMeta)) return;
            
            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();
            if (skullMeta.getDisplayName() == null) return;
            
            String targetPlayerName = ChatColor.stripColor(skullMeta.getDisplayName());
            
            if (event.getClick() == ClickType.RIGHT) {
                // 右键删除玩家权限
                removePlayerFromWorld(player, worldName, targetPlayerName);
            }
        }
    }
    
    private void removePlayerFromWorld(Player owner, String worldName, String targetPlayerName) {
        // 检查是否是世界所有者
        WorldConfig.WorldSettings settings = WorldConfig.getWorldSettings(worldName);
        if (settings == null) {
            owner.sendMessage(ChatColor.RED + "世界配置未找到！");
            return;
        }
        
        // 检查是否是世界所有者（第一个玩家）
        if (settings.getAllowedPlayers().isEmpty() || !settings.getAllowedPlayers().get(0).equals(owner.getName())) {
            owner.sendMessage(ChatColor.RED + "你不是此世界的所有者，无法移除玩家！");
            return;
        }
        
        // 检查目标玩家是否在允许列表中
        if (!settings.getAllowedPlayers().contains(targetPlayerName)) {
            owner.sendMessage(ChatColor.RED + "玩家 " + targetPlayerName + " 不在此世界的允许列表中！");
            return;
        }
        
        // 不能移除自己
        if (targetPlayerName.equals(owner.getName())) {
            owner.sendMessage(ChatColor.RED + "你不能移除自己的世界权限！");
            return;
        }
        
        // 从世界中移除玩家
        WorldConfig.removePlayerFromWorld(worldName, targetPlayerName);
        
        // 如果目标玩家在线且在该世界中，将其踢回主世界
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            World currentWorld = targetPlayer.getWorld();
            if (currentWorld.getName().equals(worldName)) {
                World mainWorld = Bukkit.getWorlds().get(0);
                targetPlayer.teleport(mainWorld.getSpawnLocation());
                targetPlayer.sendMessage(ChatColor.YELLOW + "你已被移除出世界 " + worldName + " 的权限，已传送至主世界。");
            }
        }
        
        owner.sendMessage(ChatColor.GREEN + "已成功移除玩家 " + targetPlayerName + " 的世界权限！");
        
        // 刷新菜单
        int currentPage = WorldPlayerMenu.getCurrentPage(owner.getOpenInventory().getTitle());
        WorldPlayerMenu.openWorldPlayerMenu(owner, worldName, currentPage);
    }
}