package mut.buildup.mutbuildutils.menu;

import mut.buildup.mutbuildutils.config.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldPlayerMenu {
    private static final String MENU_TITLE_PREFIX = "§6世界玩家列表: ";
    private static final int MENU_SIZE = 54; // 6行
    private static final int PLAYERS_PER_PAGE = 45; // 前5行用于显示玩家，最后一行用于导航

    public static void openWorldPlayerMenu(Player viewer, String worldName) {
        openWorldPlayerMenu(viewer, worldName, 0);
    }

    public static void openWorldPlayerMenu(Player viewer, String worldName, int page) {
        WorldConfig.WorldSettings settings = WorldConfig.getWorldSettings(worldName);
        if (settings == null) {
            viewer.sendMessage(ChatColor.RED + "世界配置未找到！");
            return;
        }

        List<String> allowedPlayers = settings.getAllowedPlayers();
        // 移除世界所有者（自己）
        List<String> invitedPlayers = new ArrayList<>();
        for (String playerName : allowedPlayers) {
            if (!playerName.equals(viewer.getName())) {
                invitedPlayers.add(playerName);
            }
        }

        if (invitedPlayers.isEmpty()) {
            viewer.sendMessage(ChatColor.YELLOW + "世界 " + worldName + " 中没有被邀请的玩家！");
            return;
        }

        int totalPages = (int) Math.ceil((double) invitedPlayers.size() / PLAYERS_PER_PAGE);
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, 
            MENU_TITLE_PREFIX + worldName + " (" + (page + 1) + "/" + totalPages + ")");

        // 添加玩家头颅
        int startIndex = page * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, invitedPlayers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String playerName = invitedPlayers.get(i);
            ItemStack playerHead = createPlayerHead(playerName);
            inventory.setItem(i - startIndex, playerHead);
        }

        // 添加导航按钮
        addNavigationButtons(inventory, page, totalPages, worldName);

        viewer.openInventory(inventory);
    }

    private static ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + playerName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "玩家: " + playerName);
            
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            
            // 检查玩家是否真实存在（曾经加入过服务器）
            if (offlinePlayer != null && (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline())) {
                if (offlinePlayer.isOnline()) {
                    lore.add(ChatColor.GREEN + "状态: 在线");
                } else {
                    lore.add(ChatColor.GRAY + "状态: 离线");
                }
                
                // 设置玩家头颅
                meta.setOwningPlayer(offlinePlayer);
            } else {
                lore.add(ChatColor.RED + "状态: 未知玩家");
                // 对于未知玩家，不设置头颅所有者，使用默认头颅
            }
            
            lore.add(ChatColor.YELLOW + "右键点击移除此玩家的世界权限");
            meta.setLore(lore);
        }
        
        head.setItemMeta(meta);
        return head;
    }

    private static void addNavigationButtons(Inventory inventory, int currentPage, int totalPages, String worldName) {
        // 上一页按钮
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "上一页");
                List<String> prevLore = new ArrayList<>();
                prevLore.add(ChatColor.GRAY + "点击查看上一页");
                prevMeta.setLore(prevLore);
                prevButton.setItemMeta(prevMeta);
            }
            inventory.setItem(45, prevButton);
        }

        // 返回按钮
        ItemStack backButton = new ItemStack(Material.BOOK);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GOLD + "返回世界列表");
            List<String> backLore = new ArrayList<>();
            backLore.add(ChatColor.GRAY + "点击返回我的世界列表");
            backMeta.setLore(backLore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(48, backButton);

        // 关闭按钮
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "关闭菜单");
            List<String> closeLore = new ArrayList<>();
            closeLore.add(ChatColor.GRAY + "点击关闭此菜单");
            closeMeta.setLore(closeLore);
            closeButton.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeButton);

        // 下一页按钮
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "下一页");
                List<String> nextLore = new ArrayList<>();
                nextLore.add(ChatColor.GRAY + "点击查看下一页");
                nextMeta.setLore(nextLore);
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(53, nextButton);
        }
    }

    public static boolean isWorldPlayerMenu(String title) {
        return title != null && title.startsWith(MENU_TITLE_PREFIX);
    }

    public static int getCurrentPage(String title) {
        if (title == null || !title.contains("(")) return 0;
        
        try {
            String pageInfo = title.substring(title.indexOf("(") + 1, title.indexOf("/"));
            return Integer.parseInt(pageInfo) - 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String extractWorldName(String title) {
        if (title == null || !title.startsWith(MENU_TITLE_PREFIX)) return "";
        
        try {
            String worldPart = title.substring(MENU_TITLE_PREFIX.length());
            if (worldPart.contains(" (")) {
                return worldPart.substring(0, worldPart.indexOf(" ("));
            }
            return worldPart;
        } catch (Exception e) {
            return "";
        }
    }
}