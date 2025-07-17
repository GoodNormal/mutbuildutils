package mut.buildup.mutbuildutils.commands;

import mut.buildup.mutbuildutils.config.ResourcePackConfig;
import mut.buildup.mutbuildutils.config.WorldConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldResourcePackCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "refresh":
                return handleRefresh(sender);
            case "create":
                return handleCreate(sender, args);
            case "set":
                return handleSet(sender, args);
            case "add":
                return handleAdd(sender, args);
            case "delete":
                return handleDelete(sender, args);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleRefresh(CommandSender sender) {
        if (!sender.hasPermission("mutbuildutils.resourcepack.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        sender.sendMessage(Component.text("§e正在刷新资源包哈希值..."));
        
        boolean success = ResourcePackConfig.refreshHashes();
        if (success) {
            sender.sendMessage(Component.text("§a资源包哈希值刷新成功！"));
        } else {
            sender.sendMessage(Component.text("§c资源包哈希值刷新失败！请检查控制台错误信息。"));
        }
        
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mutbuildutils.resourcepack.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(Component.text("§c用法: /worldresourcepack create <资源包缩写> <URL>"));
            return true;
        }

        String shortName = args[1];
        String url = args[2];

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            sender.sendMessage(Component.text("§c无效的URL格式！URL必须以http://或https://开头。"));
            return true;
        }

        boolean success = ResourcePackConfig.addResourcePack(shortName, url);
        if (success) {
            sender.sendMessage(Component.text("§a成功添加资源包: " + shortName + " -> " + url));
        } else {
            sender.sendMessage(Component.text("§c添加资源包失败！请检查控制台错误信息。"));
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mutbuildutils.resourcepack.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(Component.text("§c用法: /worldresourcepack set <资源包缩写> <世界名>"));
            return true;
        }

        String shortName = args[1];
        String worldName = args[2];

        // 检查资源包是否存在
        ResourcePackConfig.ResourcePackInfo packInfo = ResourcePackConfig.getResourcePack(shortName);
        if (packInfo == null) {
            sender.sendMessage(Component.text("§c资源包 '" + shortName + "' 不存在！"));
            return true;
        }

        // 检查世界是否存在
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(Component.text("§c世界 '" + worldName + "' 不存在或未加载！"));
            return true;
        }

        // 为世界中的所有玩家设置资源包
        List<Player> playersInWorld = world.getPlayers();
        if (playersInWorld.isEmpty()) {
            sender.sendMessage(Component.text("§e世界 '" + worldName + "' 中没有玩家，资源包配置已保存。"));
        } else {
            for (Player player : playersInWorld) {
                applyResourcePacksToPlayer(player, shortName);
            }
            sender.sendMessage(Component.text("§a已为世界 '" + worldName + "' 中的 " + playersInWorld.size() + " 个玩家应用资源包 '" + shortName + "'。"));
        }

        // 保存世界的资源包配置
        saveWorldResourcePackConfig(worldName, shortName);
        
        return true;
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mutbuildutils.resourcepack.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(Component.text("§c用法: /worldresourcepack add <资源包缩写> <玩家名>"));
            return true;
        }

        String shortName = args[1];
        String playerName = args[2];

        // 检查资源包是否存在
        ResourcePackConfig.ResourcePackInfo packInfo = ResourcePackConfig.getResourcePack(shortName);
        if (packInfo == null) {
            sender.sendMessage(Component.text("§c资源包 '" + shortName + "' 不存在！"));
            return true;
        }

        // 处理目标选择器
        List<Player> targetPlayers = new ArrayList<>();
        if (playerName.startsWith("@")) {
            // 简单的目标选择器处理
            switch (playerName) {
                case "@a":
                    targetPlayers.addAll(Bukkit.getOnlinePlayers());
                    break;
                case "@p":
                    if (sender instanceof Player) {
                        targetPlayers.add((Player) sender);
                    }
                    break;
                default:
                    sender.sendMessage(Component.text("§c不支持的目标选择器: " + playerName));
                    return true;
            }
        } else {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(Component.text("§c玩家 '" + playerName + "' 不在线！"));
                return true;
            }
            targetPlayers.add(player);
        }

        if (targetPlayers.isEmpty()) {
            sender.sendMessage(Component.text("§c没有找到目标玩家！"));
            return true;
        }

        // 为目标玩家应用资源包
        for (Player player : targetPlayers) {
            applyResourcePacksToPlayer(player, shortName);
        }

        sender.sendMessage(Component.text("§a已为 " + targetPlayers.size() + " 个玩家应用资源包 '" + shortName + "'。"));
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mutbuildutils.resourcepack.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(Component.text("§c用法: /worldresourcepack delete <资源包缩写>"));
            return true;
        }

        String shortName = args[1];

        // 检查资源包是否存在
        ResourcePackConfig.ResourcePackInfo packInfo = ResourcePackConfig.getResourcePack(shortName);
        if (packInfo == null) {
            sender.sendMessage(Component.text("§c资源包 '" + shortName + "' 不存在！"));
            return true;
        }

        boolean success = ResourcePackConfig.deleteResourcePack(shortName);
        if (success) {
            sender.sendMessage(Component.text("§a成功删除资源包: " + shortName));
        } else {
            sender.sendMessage(Component.text("§c删除资源包失败！请检查控制台错误信息。"));
        }

        return true;
    }

    /**
     * @param player
     * @param shortName
     */
    private void applyResourcePacksToPlayer(Player player, String shortName) {
        ResourcePackConfig.ResourcePackInfo packInfo = ResourcePackConfig.getResourcePack(shortName);
        if (packInfo == null) {
            player.sendMessage(Component.text("§c资源包 '" + shortName + "' 不存在！"));
            return;
        }

        player.sendMessage(Component.text("§e正在为你应用资源包..."));
        
        // 先应用基础资源包（使用setResourcePack方法）
        String baseUrl = ResourcePackConfig.getBaseResourcePackUrl();
        String baseHash = ResourcePackConfig.getBaseResourcePackHash();
        String baseUuid = ResourcePackConfig.getBaseResourcePackUuid();
        
        if (!baseUrl.isEmpty()) {
            player.sendMessage(Component.text("§7正在应用基础资源包..."));
            if (!baseHash.isEmpty() && !baseUuid.isEmpty()) {
                // 使用1.21版本的setResourcePack API（带UUID）
                java.util.UUID resourcePackUuid = java.util.UUID.fromString(baseUuid);
                player.setResourcePack(baseUrl, baseHash, true, Component.text("请接受基础资源包以获得最佳游戏体验"));
            } else if (!baseHash.isEmpty()) {
                player.setResourcePack(baseUrl, baseHash, true, Component.text("请接受基础资源包以获得最佳游戏体验"));
            } else {
                player.setResourcePack(baseUrl);
            }
        }

        // 应用主资源包（使用addResourcePack方法）
        if (!baseUrl.isEmpty()) {
            // 如果有基础资源包，延迟应用主资源包确保基础资源包先加载
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MUTbuildUtils"), () -> {
                applyMainResourcePackToPlayer(player, packInfo, shortName);
            }, 40L); // 2秒延迟，给基础资源包更多时间加载
        } else {
            // 如果没有基础资源包，直接应用主资源包
            applyMainResourcePackToPlayer(player, packInfo, shortName);
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
     * 为玩家应用主资源包
     */
    private void applyMainResourcePackToPlayer(Player player, ResourcePackConfig.ResourcePackInfo packInfo, String shortName) {
        player.sendMessage(Component.text("§7正在应用主资源包: " + shortName));
        
        try {
            if (!packInfo.getHash().isEmpty() && !packInfo.getUuid().isEmpty()) {
                // 使用1.21版本的addResourcePack API（带UUID）
                java.util.UUID resourcePackUuid = java.util.UUID.fromString(packInfo.getUuid());
                player.addResourcePack(resourcePackUuid, packInfo.getUrl(), hexStringToByteArray(packInfo.getHash()), "请接受主资源包: " + shortName, true);
                System.out.println("[WorldResourcePack] 使用addResourcePack API应用主资源包: " + packInfo.getUrl());
            } else if (!packInfo.getHash().isEmpty()) {
                // 如果没有UUID，回退到setResourcePack方法
                player.setResourcePack(packInfo.getUrl(), packInfo.getHash(), true, Component.text("请接受主资源包: " + shortName));
                System.out.println("[WorldResourcePack] 使用setResourcePack API应用主资源包（无UUID）: " + packInfo.getUrl());
            } else {
                player.setResourcePack(packInfo.getUrl());
                System.out.println("[WorldResourcePack] 使用基础setResourcePack API应用主资源包: " + packInfo.getUrl());
            }
            player.sendMessage(Component.text("§a主资源包应用完成！"));
        } catch (Exception e) {
            System.err.println("[WorldResourcePack] 应用主资源包失败: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage(Component.text("§c主资源包应用失败，请检查控制台错误信息。"));
        }
    }

    private void saveWorldResourcePackConfig(String worldName, String shortName) {
        try {
            // 更新世界配置中的资源包设置
            WorldConfig.setWorldMainResourcePack(worldName, shortName);
            System.out.println("[WorldResourcePack] 已保存世界 " + worldName + " 的资源包配置: " + shortName);
        } catch (Exception e) {
            System.err.println("[WorldResourcePack] 保存世界资源包配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("§6=== WorldResourcePack 命令帮助 ==="));
        sender.sendMessage(Component.text("§e/worldresourcepack refresh §7- 刷新所有资源包的哈希值和UUID"));
        sender.sendMessage(Component.text("§e/worldresourcepack create <缩写> <URL> §7- 添加新的主资源包"));
        sender.sendMessage(Component.text("§e/worldresourcepack delete <缩写> §7- 删除指定的主资源包"));
        sender.sendMessage(Component.text("§e/worldresourcepack set <缩写> <世界名> §7- 为世界设置资源包"));
        sender.sendMessage(Component.text("§e/worldresourcepack add <缩写> <玩家名> §7- 为玩家添加资源包"));
        sender.sendMessage(Component.text("§7玩家名支持目标选择器: @a(所有玩家), @p(自己)"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 第一个参数：子命令
            List<String> subCommands = Arrays.asList("refresh", "create", "delete", "set", "add");
            for (String subCmd : subCommands) {
                if (subCmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("set".equals(subCommand) || "add".equals(subCommand) || "delete".equals(subCommand)) {
                // 第二个参数：资源包缩写
                for (String packName : ResourcePackConfig.getResourcePackNames()) {
                    if (packName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(packName);
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if ("set".equals(subCommand)) {
                // 第三个参数：世界名
                for (World world : Bukkit.getWorlds()) {
                    if (world.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(world.getName());
                    }
                }
            } else if ("add".equals(subCommand)) {
                // 第三个参数：玩家名或目标选择器
                List<String> targets = Arrays.asList("@a", "@p");
                for (String target : targets) {
                    if (target.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(target);
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }

        return completions;
    }
}