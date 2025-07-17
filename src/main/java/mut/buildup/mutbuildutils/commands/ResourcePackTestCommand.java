package mut.buildup.mutbuildutils.commands;

import mut.buildup.mutbuildutils.config.ResourcePackConfig;
import mut.buildup.mutbuildutils.config.WorldConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResourcePackTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§c此命令只能由玩家执行！"));
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("mutbuildutils.resourcepack.test")) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "base":
                return testBaseResourcePack(player);
            case "main":
                return testMainResourcePack(player, args);
            case "world":
                return testWorldResourcePacks(player);
            case "status":
                return showResourcePackStatus(player);
            default:
                sendUsage(player);
                return true;
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("§e=== 资源包测试命令 ==="));
        player.sendMessage(Component.text("§f/rptest base - 测试基础资源包"));
        player.sendMessage(Component.text("§f/rptest main <缩写> - 测试指定主资源包"));
        player.sendMessage(Component.text("§f/rptest world - 测试当前世界的资源包配置"));
        player.sendMessage(Component.text("§f/rptest status - 显示资源包配置状态"));
    }

    private boolean testBaseResourcePack(Player player) {
        player.sendMessage(Component.text("§e正在测试基础资源包..."));
        
        String baseUrl = ResourcePackConfig.getBaseResourcePackUrl();
        String baseHash = ResourcePackConfig.getBaseResourcePackHash();
        String baseUuid = ResourcePackConfig.getBaseResourcePackUuid();
        
        if (baseUrl == null || baseUrl.isEmpty()) {
            player.sendMessage(Component.text("§c基础资源包未配置！"));
            return true;
        }
        
        player.sendMessage(Component.text("§a基础资源包信息:"));
        player.sendMessage(Component.text("§f  URL: " + baseUrl));
        player.sendMessage(Component.text("§f  Hash: " + (baseHash != null && !baseHash.isEmpty() ? baseHash : "§c未设置")));
        player.sendMessage(Component.text("§f  UUID: " + (baseUuid != null && !baseUuid.isEmpty() ? baseUuid : "§c未设置")));
        
        // 应用基础资源包
        try {
            if (baseUuid != null && !baseUuid.isEmpty() && baseHash != null && !baseHash.isEmpty()) {
                player.setResourcePack(baseUrl, baseHash, true, null);
            } else if (baseHash != null && !baseHash.isEmpty()) {
                player.setResourcePack(baseUrl, baseHash);
            } else {
                player.setResourcePack(baseUrl);
            }
            player.sendMessage(Component.text("§a基础资源包已发送！请查看客户端状态。"));
        } catch (Exception e) {
            player.sendMessage(Component.text("§c发送基础资源包失败: " + e.getMessage()));
        }
        
        return true;
    }

    private boolean testMainResourcePack(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(Component.text("§c用法: /rptest main <资源包缩写>"));
            return true;
        }
        
        String shortName = args[1];
        player.sendMessage(Component.text("§e正在测试主资源包: " + shortName));
        
        ResourcePackConfig.ResourcePackInfo resourcePack = ResourcePackConfig.getResourcePack(shortName);
        if (resourcePack == null) {
            player.sendMessage(Component.text("§c未找到资源包: " + shortName));
            return true;
        }
        
        player.sendMessage(Component.text("§a主资源包信息:"));
        player.sendMessage(Component.text("§f  缩写: " + shortName));
        player.sendMessage(Component.text("§f  URL: " + resourcePack.getUrl()));
        player.sendMessage(Component.text("§f  Hash: " + (resourcePack.getHash() != null && !resourcePack.getHash().isEmpty() ? resourcePack.getHash() : "§c未设置")));
        player.sendMessage(Component.text("§f  UUID: " + (resourcePack.getUuid() != null && !resourcePack.getUuid().isEmpty() ? resourcePack.getUuid() : "§c未设置")));
        
        // 应用主资源包
        try {
            String url = resourcePack.getUrl();
            String hash = resourcePack.getHash();
            String uuid = resourcePack.getUuid();
            
            if (uuid != null && !uuid.isEmpty() && hash != null && !hash.isEmpty()) {
                player.setResourcePack(url, hash, true, null);
            } else if (hash != null && !hash.isEmpty()) {
                player.setResourcePack(url, hash);
            } else {
                player.setResourcePack(url);
            }
            player.sendMessage(Component.text("§a主资源包已发送！请查看客户端状态。"));
        } catch (Exception e) {
            player.sendMessage(Component.text("§c发送主资源包失败: " + e.getMessage()));
        }
        
        return true;
    }

    private boolean testWorldResourcePacks(Player player) {
        String worldName = player.getWorld().getName();
        player.sendMessage(Component.text("§e正在测试世界 " + worldName + " 的资源包配置..."));
        
        String mainResourcePackKey = WorldConfig.getWorldMainResourcePack(worldName);
        String baseResourcePackKey = WorldConfig.getWorldBaseResourcePack(worldName);
        
        player.sendMessage(Component.text("§a世界资源包配置:"));
        player.sendMessage(Component.text("§f  基础资源包: " + (baseResourcePackKey != null && !baseResourcePackKey.isEmpty() ? baseResourcePackKey : "§7默认(base)")));
        player.sendMessage(Component.text("§f  主资源包: " + (mainResourcePackKey != null && !mainResourcePackKey.isEmpty() ? mainResourcePackKey : "§7无")));
        
        // 模拟应用世界资源包的过程
        player.sendMessage(Component.text("§e正在模拟应用世界资源包..."));
        
        // 先测试基础资源包
        if (baseResourcePackKey == null || baseResourcePackKey.isEmpty()) {
            baseResourcePackKey = "base";
        }
        
        if ("base".equals(baseResourcePackKey)) {
            String baseUrl = ResourcePackConfig.getBaseResourcePackUrl();
            if (baseUrl != null && !baseUrl.isEmpty()) {
                player.sendMessage(Component.text("§a✓ 基础资源包可用"));
            } else {
                player.sendMessage(Component.text("§c✗ 基础资源包未配置"));
            }
        } else {
            ResourcePackConfig.ResourcePackInfo baseResourcePack = ResourcePackConfig.getResourcePack(baseResourcePackKey);
            if (baseResourcePack != null) {
                player.sendMessage(Component.text("§a✓ 自定义基础资源包可用: " + baseResourcePackKey));
            } else {
                player.sendMessage(Component.text("§c✗ 自定义基础资源包不存在: " + baseResourcePackKey));
            }
        }
        
        // 再测试主资源包
        if (mainResourcePackKey != null && !mainResourcePackKey.isEmpty()) {
            ResourcePackConfig.ResourcePackInfo mainResourcePack = ResourcePackConfig.getResourcePack(mainResourcePackKey);
            if (mainResourcePack != null) {
                player.sendMessage(Component.text("§a✓ 主资源包可用: " + mainResourcePackKey));
            } else {
                player.sendMessage(Component.text("§c✗ 主资源包不存在: " + mainResourcePackKey));
            }
        } else {
            player.sendMessage(Component.text("§7- 该世界未配置主资源包"));
        }
        
        return true;
    }

    private boolean showResourcePackStatus(Player player) {
        player.sendMessage(Component.text("§e=== 资源包配置状态 ==="));
        
        // 显示基础资源包状态
        String baseUrl = ResourcePackConfig.getBaseResourcePackUrl();
        String baseHash = ResourcePackConfig.getBaseResourcePackHash();
        String baseUuid = ResourcePackConfig.getBaseResourcePackUuid();
        
        player.sendMessage(Component.text("§a基础资源包:"));
        player.sendMessage(Component.text("§f  配置状态: " + (baseUrl != null && !baseUrl.isEmpty() ? "§a已配置" : "§c未配置")));
        if (baseUrl != null && !baseUrl.isEmpty()) {
            player.sendMessage(Component.text("§f  Hash状态: " + (baseHash != null && !baseHash.isEmpty() ? "§a已设置" : "§c未设置")));
            player.sendMessage(Component.text("§f  UUID状态: " + (baseUuid != null && !baseUuid.isEmpty() ? "§a已设置" : "§c未设置")));
        }
        
        // 显示主资源包状态
        player.sendMessage(Component.text("§a主资源包:"));
        var resourcePacks = ResourcePackConfig.getAllResourcePacks();
        if (resourcePacks.isEmpty()) {
            player.sendMessage(Component.text("§7  无主资源包配置"));
        } else {
            player.sendMessage(Component.text("§f  已配置数量: " + resourcePacks.size()));
            for (String shortName : resourcePacks.keySet()) {
                ResourcePackConfig.ResourcePackInfo info = resourcePacks.get(shortName);
                boolean hasHash = info.getHash() != null && !info.getHash().isEmpty();
                boolean hasUuid = info.getUuid() != null && !info.getUuid().isEmpty();
                player.sendMessage(Component.text("§f    " + shortName + ": Hash=" + (hasHash ? "§a✓" : "§c✗") + "§f, UUID=" + (hasUuid ? "§a✓" : "§c✗")));
            }
        }
        
        return true;
    }
}