package mut.buildup.mutbuildutils.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import mut.buildup.mutbuildutils.config.ResourcePackConfig;
import mut.buildup.mutbuildutils.config.WorldConfig;

public class ResourcePackListener implements Listener {
    
    // 存储等待应用主资源包的玩家信息
    private final java.util.Map<java.util.UUID, PendingResourcePack> pendingMainPacks = new java.util.concurrent.ConcurrentHashMap<>();
    
    // 内部类：存储待应用的主资源包信息
    private static class PendingResourcePack {
        final ResourcePackConfig.ResourcePackInfo resourcePack;
        final long timestamp;
        
        PendingResourcePack(ResourcePackConfig.ResourcePackInfo resourcePack) {
            this.resourcePack = resourcePack;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 清除之前的待应用资源包
        pendingMainPacks.remove(player.getUniqueId());
        
        // 应用世界的资源包
        applyWorldResourcePacks(player, worldName);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 延迟应用资源包，确保玩家完全加载
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            mut.buildup.mutbuildutils.MUTbuildUtils.getInstance(), 
            () -> applyWorldResourcePacks(player, worldName), 
            40L // 2秒延迟，确保玩家完全加载
        );
    }
    
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        java.util.UUID playerUuid = player.getUniqueId();
        
        System.out.println("[ResourcePackListener] 玩家 " + player.getName() + " 资源包状态: " + status.name());
        
        switch (status) {
            case SUCCESSFULLY_LOADED:
                System.out.println("[ResourcePackListener] 资源包成功加载");
                
                // 检查是否有待应用的主资源包
                PendingResourcePack pendingPack = pendingMainPacks.get(playerUuid);
                if (pendingPack != null) {
                    System.out.println("[ResourcePackListener] 基础资源包加载完成，现在应用主资源包");
                    
                    // 延迟一小段时间确保基础资源包完全生效
                    org.bukkit.Bukkit.getScheduler().runTaskLater(
                        mut.buildup.mutbuildutils.MUTbuildUtils.getInstance(),
                        () -> {
                            applyMainResourcePack(player, pendingPack.resourcePack);
                            pendingMainPacks.remove(playerUuid);
                        },
                        10L // 0.5秒延迟
                    );
                }
                break;
            case DECLINED:
                System.out.println("[ResourcePackListener] 玩家拒绝了资源包");
                // 清除待应用的主资源包
                pendingMainPacks.remove(playerUuid);
                break;
            case FAILED_DOWNLOAD:
                System.out.println("[ResourcePackListener] 资源包下载失败");
                // 清除待应用的主资源包
                pendingMainPacks.remove(playerUuid);
                break;
            case ACCEPTED:
                System.out.println("[ResourcePackListener] 玩家接受了资源包，正在下载...");
                break;
            case INVALID_URL:
                System.out.println("[ResourcePackListener] 资源包URL无效");
                // 清除待应用的主资源包
                pendingMainPacks.remove(playerUuid);
                break;
            case FAILED_RELOAD:
                System.out.println("[ResourcePackListener] 资源包重载失败");
                break;
            case DISCARDED:
                System.out.println("[ResourcePackListener] 资源包被丢弃");
                // 清除待应用的主资源包
                pendingMainPacks.remove(playerUuid);
                break;
        }
    }

    /**
     * 为玩家应用世界的资源包
     */
    private void applyWorldResourcePacks(Player player, String worldName) {
        System.out.println("[ResourcePackListener] 为玩家 " + player.getName() + " 应用世界 " + worldName + " 的资源包");
        
        // 获取世界的主资源包
        String mainResourcePackKey = WorldConfig.getWorldMainResourcePack(worldName);
        
        // 获取世界的基础资源包
        String baseResourcePackKey = WorldConfig.getWorldBaseResourcePack(worldName);
        
        // 如果没有设置基础资源包，使用默认的基础资源包
        if (baseResourcePackKey == null || baseResourcePackKey.isEmpty()) {
            baseResourcePackKey = "base";
        }
        
        System.out.println("[ResourcePackListener] 基础资源包: " + baseResourcePackKey + ", 主资源包: " + mainResourcePackKey);
        
        // 先应用基础资源包
        ResourcePackConfig.ResourcePackInfo baseResourcePack = null;
        if ("base".equals(baseResourcePackKey)) {
            // 使用默认基础资源包
            String baseUrl = ResourcePackConfig.getBaseResourcePackUrl();
            String baseHash = ResourcePackConfig.getBaseResourcePackHash();
            String baseUuid = ResourcePackConfig.getBaseResourcePackUuid();
            if (baseUrl != null && !baseUrl.isEmpty()) {
                baseResourcePack = new ResourcePackConfig.ResourcePackInfo(baseUrl, baseHash, baseUuid);
                System.out.println("[ResourcePackListener] 找到基础资源包: " + baseUrl);
            } else {
                System.out.println("[ResourcePackListener] 基础资源包URL为空");
            }
        } else {
            // 使用自定义基础资源包
            baseResourcePack = ResourcePackConfig.getResourcePack(baseResourcePackKey);
            if (baseResourcePack != null) {
                System.out.println("[ResourcePackListener] 找到自定义基础资源包: " + baseResourcePack.getUrl());
            } else {
                System.out.println("[ResourcePackListener] 未找到自定义基础资源包: " + baseResourcePackKey);
            }
        }
        
        if (baseResourcePack != null) {
            System.out.println("[ResourcePackListener] 应用基础资源包: " + baseResourcePack.getUrl());
            applyBaseResourcePack(player, baseResourcePack);
        }
        
        // 准备应用主资源包（如果存在）
        if (mainResourcePackKey != null && !mainResourcePackKey.isEmpty()) {
            ResourcePackConfig.ResourcePackInfo mainResourcePack = ResourcePackConfig.getResourcePack(mainResourcePackKey);
            if (mainResourcePack != null) {
                System.out.println("[ResourcePackListener] 准备应用主资源包: " + mainResourcePack.getUrl());
                
                if (baseResourcePack != null) {
                    // 如果有基础资源包，将主资源包加入待应用队列，等待基础资源包加载完成
                    pendingMainPacks.put(player.getUniqueId(), new PendingResourcePack(mainResourcePack));
                    System.out.println("[ResourcePackListener] 主资源包已加入待应用队列，等待基础资源包加载完成");
                } else {
                    // 如果没有基础资源包，直接应用主资源包
                    System.out.println("[ResourcePackListener] 没有基础资源包，直接应用主资源包");
                    applyMainResourcePack(player, mainResourcePack);
                }
            } else {
                System.out.println("[ResourcePackListener] 未找到主资源包: " + mainResourcePackKey);
            }
        } else {
            System.out.println("[ResourcePackListener] 该世界没有配置主资源包");
        }
    }

    /**
     * 为玩家应用基础资源包（使用setResourcePack方法）
     */
    private void applyBaseResourcePack(Player player, ResourcePackConfig.ResourcePackInfo resourcePack) {
        try {
            String url = resourcePack.getUrl();
            String hash = resourcePack.getHash();
            String uuid = resourcePack.getUuid();
            
            System.out.println("[ResourcePackListener] 应用基础资源包详情:");
            System.out.println("  - 玩家: " + player.getName());
            System.out.println("  - URL: " + url);
            System.out.println("  - Hash: " + (hash != null && !hash.isEmpty() ? hash : "无"));
            System.out.println("  - UUID: " + (uuid != null && !uuid.isEmpty() ? uuid : "无"));
            
            if (uuid != null && !uuid.isEmpty() && hash != null && !hash.isEmpty()) {
                // 使用带UUID和哈希值的setResourcePack方法（1.21版本）
                System.out.println("[ResourcePackListener] 使用1.21版本setResourcePack API（带UUID和哈希）");
                java.util.UUID resourcePackUuid = java.util.UUID.fromString(uuid);
                player.setResourcePack(url, hash, true, net.kyori.adventure.text.Component.text("请接受基础资源包以获得最佳游戏体验"));
            } else if (hash != null && !hash.isEmpty()) {
                // 使用带哈希值的setResourcePack方法（兼容版本）
                System.out.println("[ResourcePackListener] 使用兼容版本setResourcePack API（仅哈希）");
                player.setResourcePack(url, hash, true, net.kyori.adventure.text.Component.text("请接受基础资源包以获得最佳游戏体验"));
            } else {
                // 使用不带哈希值的setResourcePack方法（最基本的兼容性）
                System.out.println("[ResourcePackListener] 使用基础版本setResourcePack API（无哈希）");
                player.setResourcePack(url);
            }
            
            System.out.println("[ResourcePackListener] 基础资源包发送成功: " + url);
        } catch (Exception e) {
            System.err.println("[ResourcePackListener] 应用基础资源包失败: " + resourcePack.getUrl() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 将十六进制字符串转换为byte数组
     */
    private byte[] hexStringToByteArray(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new byte[0];
        }
        
        // 移除可能的空格和前缀
        hexString = hexString.replaceAll("\\s+", "").toLowerCase();
        if (hexString.startsWith("0x")) {
            hexString = hexString.substring(2);
        }
        
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * 为玩家应用主资源包（使用addResourcePack方法）
     */
    private void applyMainResourcePack(Player player, ResourcePackConfig.ResourcePackInfo resourcePack) {
        try {
            String url = resourcePack.getUrl();
            String hash = resourcePack.getHash();
            String uuid = resourcePack.getUuid();

            System.out.println("[ResourcePackListener] 应用主资源包详情:");
            System.out.println("  - 玩家: " + player.getName());
            System.out.println("  - URL: " + url);
            System.out.println("  - Hash: " + (hash != null && !hash.isEmpty() ? hash : "无"));
            System.out.println("  - UUID: " + (uuid != null && !uuid.isEmpty() ? uuid : "无"));
            
            if (uuid != null && !uuid.isEmpty() && hash != null && !hash.isEmpty()) {
                // 使用带UUID和哈希值的addResourcePack方法（1.21版本）
                System.out.println("[ResourcePackListener] 使用1.21版本addResourcePack API（带UUID和哈希）");
                java.util.UUID resourcePackUuid = java.util.UUID.fromString(uuid);
                player.addResourcePack(resourcePackUuid, url, hexStringToByteArray(hash), "请接受主资源包以获得完整游戏体验", true);
            } else if (hash != null && !hash.isEmpty()) {
                // 如果没有UUID，回退到setResourcePack方法
                System.out.println("[ResourcePackListener] UUID缺失，回退到setResourcePack API（仅哈希）");
                player.setResourcePack(url, hash, true, net.kyori.adventure.text.Component.text("请接受主资源包以获得完整游戏体验"));
            } else {
                // 使用不带哈希值的setResourcePack方法（最基本的兼容性）
                System.out.println("[ResourcePackListener] 哈希缺失，回退到基础setResourcePack API（无哈希）");
                player.setResourcePack(url);
            }
            
            System.out.println("[ResourcePackListener] 主资源包发送成功: " + url);
        } catch (Exception e) {
            System.err.println("[ResourcePackListener] 应用主资源包失败: " + resourcePack.getUrl() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}