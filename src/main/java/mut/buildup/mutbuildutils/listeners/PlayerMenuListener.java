package mut.buildup.mutbuildutils.listeners;

import mut.buildup.mutbuildutils.menu.PlayerManagementMenu;
import mut.buildup.mutbuildutils.menu.OwnWorldMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerMenuListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        
        if (!title.contains("玩家管理菜单")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // 处理导航按钮
        if (clickedItem.getType() == Material.ARROW) {
            handleNavigationClick(player, clickedItem, title);
            return;
        }
        
        // 处理关闭按钮
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // 处理玩家头颅点击
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            handlePlayerHeadClick(player, clickedItem, event.isLeftClick());
        }
    }
    
    private void handleNavigationClick(Player player, ItemStack item, String title) {
        String itemName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        int currentPage = PlayerManagementMenu.getCurrentPage(title);
        
        if (itemName.contains("上一页")) {
            PlayerManagementMenu.openPlayerMenu(player, currentPage - 1);
        } else if (itemName.contains("下一页")) {
            PlayerManagementMenu.openPlayerMenu(player, currentPage + 1);
        }
    }
    
    private void handlePlayerHeadClick(Player clicker, ItemStack head, boolean isLeftClick) {
        if (!(head.getItemMeta() instanceof SkullMeta)) {
            return;
        }
        
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        OfflinePlayer targetPlayer = skullMeta.getOwningPlayer();
        
        if (targetPlayer == null) {
            return;
        }
        
        if (!clicker.isOp()) {
            clicker.sendMessage(Component.text("§c你没有权限执行此操作！"));
            return;
        }
        
        if (isLeftClick) {
            // 左键：打开玩家的世界列表
            openPlayerWorldList(clicker, targetPlayer);
        } else {
            // 右键：传送到玩家
            teleportToPlayer(clicker, targetPlayer);
        }
    }
    
    private void openPlayerWorldList(Player viewer, OfflinePlayer target) {
        viewer.closeInventory();
        
        // 打开目标玩家的世界列表
        OwnWorldMenu.openPlayerWorldMenu(viewer, target.getName());
    }
    
    private void teleportToPlayer(Player teleporter, OfflinePlayer target) {
        if (!target.isOnline()) {
            teleporter.sendMessage(Component.text("§c玩家 " + target.getName() + " 不在线！"));
            return;
        }
        
        Player onlineTarget = (Player) target;
        teleporter.teleport(onlineTarget.getLocation());
        teleporter.sendMessage(Component.text("§a已传送到玩家 " + target.getName() + " 的位置！"));
        teleporter.closeInventory();
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