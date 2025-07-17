package mut.buildup.mutbuildutils.menu;

import mut.buildup.mutbuildutils.config.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OwnWorldMenu {
    private static final String MENU_TITLE = "§6我的世界列表";
    private static final int MENU_SIZE = 54; // 6行
    private static final int WORLDS_PER_PAGE = 45; // 前5行用于显示世界，最后一行用于导航

    public static void openOwnWorldMenu(Player player) {
        openOwnWorldMenu(player, 0);
    }
    
    public static void openPlayerWorldMenu(Player viewer, String targetPlayerName) {
        openPlayerWorldMenu(viewer, targetPlayerName, 0);
    }
    
    public static void openPlayerWorldMenu(Player viewer, String targetPlayerName, int page) {
        List<String> ownedWorlds = getPlayerOwnedWorlds(targetPlayerName);
        
        if (ownedWorlds.isEmpty()) {
            viewer.sendMessage(ChatColor.YELLOW + "玩家 " + targetPlayerName + " 还没有创建任何世界！");
            return;
        }

        int totalPages = (int) Math.ceil((double) ownedWorlds.size() / WORLDS_PER_PAGE);
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, "§6" + targetPlayerName + "的世界列表" + " (" + (page + 1) + "/" + totalPages + ")");

        // 添加世界项目
        int startIndex = page * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, ownedWorlds.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String worldName = ownedWorlds.get(i);
            ItemStack worldItem = createWorldItem(worldName);
            inventory.setItem(i - startIndex, worldItem);
        }

        // 添加导航按钮
        addPlayerWorldNavigationButtons(inventory, page, totalPages, targetPlayerName);

        viewer.openInventory(inventory);
    }

    public static void openOwnWorldMenu(Player player, int page) {
        List<String> ownedWorlds = getPlayerOwnedWorlds(player.getName());
        
        if (ownedWorlds.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "你还没有创建任何世界！");
            return;
        }

        int totalPages = (int) Math.ceil((double) ownedWorlds.size() / WORLDS_PER_PAGE);
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        Inventory inventory = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE + " (" + (page + 1) + "/" + totalPages + ")");

        // 添加世界项目
        int startIndex = page * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, ownedWorlds.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String worldName = ownedWorlds.get(i);
            ItemStack worldItem = createWorldItem(worldName);
            inventory.setItem(i - startIndex, worldItem);
        }

        // 添加导航按钮
        addNavigationButtons(inventory, page, totalPages);

        player.openInventory(inventory);
    }

    private static ItemStack createWorldItem(String worldName) {
        // 从世界配置中获取菜单材料设置
        WorldConfig.WorldSettings settings = WorldConfig.getWorldSettings(worldName);
        Material material = Material.GRASS_BLOCK; // 默认材料
        int customModelData = 0;
        
        if (settings != null) {
            try {
                material = Material.valueOf(settings.getMenuMaterial().toUpperCase());
                customModelData = settings.getCustomModelData();
            } catch (IllegalArgumentException e) {
                // 如果材料名称无效，使用默认材料
                material = Material.GRASS_BLOCK;
            }
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + worldName);
            
            // 设置自定义模型数据（如果不为0）
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "世界名称: " + worldName);
            
            // 检查世界是否已加载
            if (Bukkit.getWorld(worldName) != null) {
                lore.add(ChatColor.GREEN + "状态: 已加载");
                lore.add(ChatColor.YELLOW + "左键点击传送到此世界");
                lore.add(ChatColor.YELLOW + "右键点击查看被邀请玩家列表");
            } else {
                lore.add(ChatColor.RED + "状态: 未加载");
                lore.add(ChatColor.GRAY + "世界当前未加载，无法传送");
                lore.add(ChatColor.YELLOW + "右键点击查看被邀请玩家列表");
            }
            
            meta.setLore(lore);
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private static void addNavigationButtons(Inventory inventory, int currentPage, int totalPages) {
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

    private static List<String> getPlayerOwnedWorlds(String playerName) {
        List<String> ownedWorlds = new ArrayList<>();
        
        Map<String, WorldConfig.WorldSettings> allWorlds = WorldConfig.getAllWorldSettings();
        for (Map.Entry<String, WorldConfig.WorldSettings> entry : allWorlds.entrySet()) {
            String worldName = entry.getKey();
            WorldConfig.WorldSettings settings = entry.getValue();
            
            // 检查玩家是否在此世界的玩家列表中
            if (settings.getPlayers().contains(playerName)) {
                ownedWorlds.add(worldName);
            }
        }
        
        return ownedWorlds;
    }

    public static boolean isOwnWorldMenu(String title) {
        return title != null && (title.startsWith(MENU_TITLE) || title.contains("的世界列表"));
    }
    
    private static void addPlayerWorldNavigationButtons(Inventory inventory, int currentPage, int totalPages, String targetPlayerName) {
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
            backMeta.setDisplayName(ChatColor.GOLD + "返回玩家列表");
            List<String> backLore = new ArrayList<>();
            backLore.add(ChatColor.GRAY + "点击返回玩家管理菜单");
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

    public static int getCurrentPage(String title) {
        if (title == null || !title.contains("(")) return 0;
        
        try {
            String pageInfo = title.substring(title.indexOf("(") + 1, title.indexOf("/"));
            return Integer.parseInt(pageInfo) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}