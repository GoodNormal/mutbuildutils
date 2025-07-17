package mut.buildup.mutbuildutils.menu;

import mut.buildup.mutbuildutils.config.WorldConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerManagementMenu {
    
    private static final int PLAYERS_PER_PAGE = 28;
    private static final int INVENTORY_SIZE = 54;
    
    public static void openPlayerMenu(Player viewer, int page) {
        List<OfflinePlayer> allPlayers = getAllPlayersWhoJoined();
        int totalPages = (int) Math.ceil((double) allPlayers.size() / PLAYERS_PER_PAGE);
        
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;
        
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, 
            "§6玩家管理菜单 §7(第" + page + "/" + totalPages + "页)");
        
        // 填充背景
        fillBackground(inventory);
        
        // 添加玩家头颅
        int startIndex = (page - 1) * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, allPlayers.size());
        
        int slot = 10; // 从第二行第二个位置开始
        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer player = allPlayers.get(i);
            ItemStack playerHead = createPlayerHead(player, viewer.isOp());
            inventory.setItem(slot, playerHead);
            
            slot++;
            if (slot == 17) slot = 19; // 跳到下一行
            if (slot == 26) slot = 28; // 跳到下一行
            if (slot == 35) slot = 37; // 跳到下一行
        }
        
        // 添加导航按钮
        addNavigationButtons(inventory, page, totalPages);
        
        viewer.openInventory(inventory);
    }
    
    private static void fillBackground(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        // 填充边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 45, glass);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }
    
    private static ItemStack createPlayerHead(OfflinePlayer player, boolean isViewerOp) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta == null) {
            return head; // 如果无法获取meta，返回默认头颅
        }
        
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e" + player.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7UUID: §f" + player.getUniqueId().toString());
        lore.add("§7状态: " + (player.isOnline() ? "§a在线" : "§c离线"));
        
        if (player.getLastPlayed() > 0) {
            long lastSeen = System.currentTimeMillis() - player.getLastPlayed();
            String timeAgo = formatTime(lastSeen);
            lore.add("§7最后登录: §f" + timeAgo + "前");
        }
        
        lore.add("");
        
        // 添加玩家详细信息到lore
        if (player.isOnline()) {
            Player onlinePlayer = (Player) player;
            lore.add("§7世界: §f" + onlinePlayer.getWorld().getName());
            lore.add("§7生命值: §f" + String.format("%.1f", onlinePlayer.getHealth()) + "/" + String.format("%.1f", onlinePlayer.getMaxHealth()));
            lore.add("§7等级: §f" + onlinePlayer.getLevel());
            lore.add("§7游戏模式: §f" + onlinePlayer.getGameMode().name());
        }
        
        if (isViewerOp) {
            // OP可以查看玩家拥有的世界
            lore.add("");
            lore.add("§6拥有的世界:");
            List<String> ownedWorlds = getPlayerOwnedWorlds(player.getName());
            if (ownedWorlds.isEmpty()) {
                lore.add("§7  无");
            } else {
                for (String worldName : ownedWorlds) {
                    lore.add("§7  - §f" + worldName);
                }
            }
            lore.add("");
            lore.add("§e左键: 查看玩家世界列表");
            lore.add("§e右键: 传送到玩家");
        } else {
            lore.add("");
            lore.add("§e左键: 查看玩家世界列表");
            lore.add("§c你没有权限传送到此玩家");
        }
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    private static void addNavigationButtons(Inventory inventory, int currentPage, int totalPages) {
        // 上一页按钮
        if (currentPage > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName("§a上一页");
            prevMeta.setLore(Arrays.asList("§7点击前往第" + (currentPage - 1) + "页"));
            prevPage.setItemMeta(prevMeta);
            inventory.setItem(45, prevPage);
        }
        
        // 下一页按钮
        if (currentPage < totalPages) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName("§a下一页");
            nextMeta.setLore(Arrays.asList("§7点击前往第" + (currentPage + 1) + "页"));
            nextPage.setItemMeta(nextMeta);
            inventory.setItem(53, nextPage);
        }
        
        // 关闭按钮
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c关闭菜单");
        close.setItemMeta(closeMeta);
        inventory.setItem(49, close);
    }
    
    private static List<OfflinePlayer> getAllPlayersWhoJoined() {
        List<OfflinePlayer> players = new ArrayList<>();
        
        // 获取主世界名称（从levelname配置读取）
        String mainWorldName = "world";
        if (!Bukkit.getWorlds().isEmpty()) {
            mainWorldName = Bukkit.getWorlds().get(0).getName();
        }
        
        File playerDataFolder = new File(Bukkit.getWorldContainer(), mainWorldName + "/playerdata");
        
        if (playerDataFolder.exists() && playerDataFolder.isDirectory()) {
            File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".dat"));
            if (playerFiles != null) {
                for (File playerFile : playerFiles) {
                    String fileName = playerFile.getName();
                    String uuidString = fileName.substring(0, fileName.length() - 4);
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        if (player.hasPlayedBefore() || player.isOnline()) {
                            players.add(player);
                        }
                    } catch (IllegalArgumentException ignored) {
                        // 忽略无效的UUID
                    }
                }
            }
        }
        
        // 按最后登录时间排序（最近的在前）
        players.sort((p1, p2) -> Long.compare(p2.getLastPlayed(), p1.getLastPlayed()));
        
        return players;
    }
    
    private static List<String> getPlayerOwnedWorlds(String playerName) {
        List<String> ownedWorlds = new ArrayList<>();
        File worldConfigDir = WorldConfig.getWorldConfigDir();
        
        if (worldConfigDir != null && worldConfigDir.exists()) {
            File[] configFiles = worldConfigDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (configFiles != null) {
                for (File configFile : configFiles) {
                    String worldName = configFile.getName().replace(".yml", "");
                    WorldConfig.WorldSettings settings = WorldConfig.getWorldSettings(worldName);
                    if (settings != null && settings.getAllowedPlayers().contains(playerName)) {
                        // 检查是否是世界所有者（第一个玩家通常是所有者）
                        if (!settings.getAllowedPlayers().isEmpty() && 
                            settings.getAllowedPlayers().get(0).equals(playerName)) {
                            ownedWorlds.add(worldName);
                        }
                    }
                }
            }
        }
        
        return ownedWorlds;
    }
    
    private static String formatTime(long milliseconds) {
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
    
    public static int getCurrentPage(String title) {
        try {
            String[] parts = title.split("第");
            if (parts.length > 1) {
                String pagePart = parts[1].split("/")[0];
                return Integer.parseInt(pagePart);
            }
        } catch (Exception ignored) {}
        return 1;
    }

    public static void openPlayerManagementMenu(Player player) {
        openPlayerManagementMenu(player, 1);
    }

    public static void openPlayerManagementMenu(Player player, int page) {
        openPlayerMenu(player, page);
    }
}