package mut.buildup.mutbuildutils.commands;

import mut.buildup.mutbuildutils.config.WorldConfig;
import mut.buildup.mutbuildutils.invite.InviteManager;
import mut.buildup.mutbuildutils.invite.InviteRequest;
import mut.buildup.mutbuildutils.menu.OwnWorldMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§c该命令只能由玩家执行！"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "tp":
            case "teleport":
                return handleTeleport(player, args);
            case "invite":
                return handleInvite(player, args);
            case "kick":
                return handleKick(player, args);
            case "approve":
                return handleApprove(player, args);
            case "deny":
                return handleDeny(player, args);
            case "load":
                return handleLoad(player, args);
            case "unload":
                return handleUnload(player, args);
            case "ownlist":
                return handleOwnList(player, args);
            default:
                sendUsage(player);
                return true;
        }
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("§c用法: /world tp <世界名>"));
            return true;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 不存在或未加载！"));
            return true;
        }

        // 检查玩家是否有权限进入该世界
        if (!WorldConfig.canPlayerEnterWorld(worldName, player.getName()) && !player.hasPermission("mutbuildutils.world.admin")) {
            player.sendMessage(Component.text("§c你没有权限进入世界 '" + worldName + "'！"));
            return true;
        }

        // 获取世界的出生点
        org.bukkit.Location spawnLocation = world.getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.sendMessage(Component.text("§a已传送到世界 '" + worldName + "'！"));
        } else {
            player.teleport(world.getSpawnLocation());
            player.sendMessage(Component.text("§a已传送到世界 '" + worldName + "'！"));
        }
        return true;
    }

    private boolean handleInvite(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.invite")) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }
        
        if (args.length < 4) {
            player.sendMessage(Component.text("§c用法: /world invite <被邀请玩家> <目标世界> <审核OP>"));
            return true;
        }

        String targetPlayerName = args[1];
        String worldName = args[2];
        String reviewerName = args[3];
        String inviterName = player.getName();
        
        // 检查目标世界是否存在
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 不存在或未加载！"));
            return true;
        }
        
        // 检查被邀请的玩家是否在线且不是OP
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            player.sendMessage(Component.text("§c玩家 '" + targetPlayerName + "' 不在线！"));
            return true;
        }
        
        if (targetPlayer.isOp()) {
            player.sendMessage(Component.text("§c不能邀请OP玩家！"));
            return true;
        }
        
        // 检查审核OP是否在线且是OP
        Player reviewer = Bukkit.getPlayer(reviewerName);
        if (reviewer == null) {
            player.sendMessage(Component.text("§c审核OP '" + reviewerName + "' 不在线！"));
            return true;
        }
        
        if (!reviewer.isOp()) {
            player.sendMessage(Component.text("§c玩家 '" + reviewerName + "' 不是OP，无法审核邀请！"));
            return true;
        }
        
        // 存储邀请请求
        InviteManager.addInviteRequest(inviterName, targetPlayerName, worldName);
        
        // 发送邀请信息给审核OP
        reviewer.sendMessage(Component.text("§6=== 新的邀请申请 ==="));
        reviewer.sendMessage(Component.text("§f邀请者: §e" + inviterName));
        reviewer.sendMessage(Component.text("§f被邀请玩家: §e" + targetPlayerName));
        reviewer.sendMessage(Component.text("§f目标世界: §e" + worldName));
        reviewer.sendMessage(Component.text("§f请使用以下命令进行审核:"));
        reviewer.sendMessage(Component.text("§a/world approve " + inviterName + " §7- 同意邀请"));
        reviewer.sendMessage(Component.text("§c/world deny " + inviterName + " §7- 拒绝邀请"));
        
        // 通知邀请者
        player.sendMessage(Component.text("§a邀请申请已发送！"));
        player.sendMessage(Component.text("§7被邀请玩家: §e" + targetPlayerName));
        player.sendMessage(Component.text("§7目标世界: §e" + worldName));
        player.sendMessage(Component.text("§7审核OP: §e" + reviewerName));
        player.sendMessage(Component.text("§7请等待审核OP的审批。"));
        
        // 通知被邀请的玩家
        targetPlayer.sendMessage(Component.text("§6你收到了一个世界邀请！"));
        targetPlayer.sendMessage(Component.text("§7邀请者: §e" + inviterName));
        targetPlayer.sendMessage(Component.text("§7目标世界: §e" + worldName));
        targetPlayer.sendMessage(Component.text("§7审核OP: §e" + reviewerName));
        targetPlayer.sendMessage(Component.text("§7请等待审核OP的审批。"));
        
        return true;
    }

    private boolean handleKick(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.invite")) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage(Component.text("§c用法: /world kick <玩家名> <世界名>"));
            return true;
        }

        String targetPlayerName = args[1];
        String worldName = args[2];
        
        // 检查世界是否存在
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 不存在或未加载！"));
            return true;
        }
        
        // 获取世界设置
        WorldConfig.WorldSettings settings = WorldConfig.getWorldSettings(worldName);
        if (settings == null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 的配置未找到！"));
            return true;
        }
        
        // 权限检查：OP可以管理所有世界，普通玩家只能管理自己拥有的世界
        boolean isOp = player.isOp() || player.hasPermission("mutbuildutils.world.admin");
        boolean isWorldOwner = false;
        
        if (!settings.getPlayers().isEmpty()) {
            // 检查玩家是否是世界所有者（第一个玩家）
            isWorldOwner = settings.getPlayers().get(0).equals(player.getName());
        }
        
        if (!isOp && !isWorldOwner) {
            player.sendMessage(Component.text("§c你没有权限管理世界 '" + worldName + "'！只能管理自己拥有的世界。"));
            return true;
        }
        
        // 检查目标玩家是否在世界的允许列表中
        if (!settings.getPlayers().contains(targetPlayerName)) {
            player.sendMessage(Component.text("§c玩家 '" + targetPlayerName + "' 不在世界 '" + worldName + "' 的邀请列表中！"));
            return true;
        }
        
        // 不能踢出世界所有者
        if (!settings.getPlayers().isEmpty() && settings.getPlayers().get(0).equals(targetPlayerName)) {
            player.sendMessage(Component.text("§c不能踢出世界所有者！"));
            return true;
        }
        
        // 从世界允许列表中移除玩家
        WorldConfig.removePlayerFromWorld(worldName, targetPlayerName);
        
        // 如果目标玩家在线且在该世界中，将其传送到初始世界
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null && targetPlayer.getWorld().getName().equals(worldName)) {
            World mainWorld = Bukkit.getWorlds().get(0); // 获取初始世界（通常是world）
            targetPlayer.teleport(mainWorld.getSpawnLocation());
            targetPlayer.sendMessage(Component.text("§c你已被踢出世界 '" + worldName + "' 并传送到初始世界！"));
            targetPlayer.sendMessage(Component.text("§7操作者: §e" + player.getName()));
        }
        
        player.sendMessage(Component.text("§a已成功将玩家 '" + targetPlayerName + "' 踢出世界 '" + worldName + "'！"));
        
        // 通知目标玩家（如果在线）
        if (targetPlayer != null) {
            targetPlayer.sendMessage(Component.text("§c你已被踢出世界 '" + worldName + "'！"));
            targetPlayer.sendMessage(Component.text("§7操作者: §e" + player.getName()));
        }
        
        return true;
    }

    private boolean handleApprove(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.admin") && !player.isOp()) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§c用法: /world approve <邀请者>"));
            return true;
        }

        String inviterName = args[1];
        
        // 获取邀请请求
        InviteRequest request = InviteManager.getInviteRequest(inviterName);
        if (request == null) {
            player.sendMessage(Component.text("§c没有找到来自 '" + inviterName + "' 的邀请申请！"));
            return true;
        }
        
        String targetPlayerName = request.getTargetPlayerName();
        String worldName = request.getWorldName();

        // 将被邀请玩家添加到世界的允许列表中
        WorldConfig.addPlayerToWorld(worldName, targetPlayerName);
        
        // 移除邀请请求
        InviteManager.removeInviteRequest(inviterName);
        
        player.sendMessage(Component.text("§a已同意邀请申请！"));
        player.sendMessage(Component.text("§7邀请者: §e" + inviterName));
        player.sendMessage(Component.text("§7被邀请玩家: §e" + targetPlayerName));
        player.sendMessage(Component.text("§7目标世界: §e" + worldName));
        
        // 通知邀请者
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter != null) {
            inviter.sendMessage(Component.text("§a你的邀请申请已被审核通过！"));
            inviter.sendMessage(Component.text("§7被邀请玩家 §e" + targetPlayerName + "§7 现在可以进入世界 §e" + worldName + "§7 了。"));
        }
        
        // 通知被邀请的玩家
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(Component.text("§a你已被批准进入世界 §e" + worldName + "§a！"));
            targetPlayer.sendMessage(Component.text("§7邀请者: §e" + inviterName));
            targetPlayer.sendMessage(Component.text("§7你现在可以使用 §f/world tp " + worldName + "§7 进入该世界。"));
        }
        
        return true;
    }

    private boolean handleDeny(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.admin") && !player.isOp()) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§c用法: /world deny <邀请者>"));
            return true;
        }

        String inviterName = args[1];
        
        // 获取并移除邀请请求
        InviteRequest request = InviteManager.removeInviteRequest(inviterName);
        if (request == null) {
            player.sendMessage(Component.text("§c没有找到来自 '" + inviterName + "' 的邀请申请！"));
            return true;
        }
        
        String targetPlayerName = request.getTargetPlayerName();
        String worldName = request.getWorldName();
        
        player.sendMessage(Component.text("§c已拒绝邀请申请！"));
        player.sendMessage(Component.text("§7邀请者: §e" + inviterName));
        player.sendMessage(Component.text("§7被邀请玩家: §e" + targetPlayerName));
        player.sendMessage(Component.text("§7目标世界: §e" + worldName));
        
        // 通知邀请者
        Player inviter = Bukkit.getPlayer(inviterName);
        if (inviter != null) {
            inviter.sendMessage(Component.text("§c你的邀请申请已被拒绝！"));
            inviter.sendMessage(Component.text("§7被邀请玩家: §e" + targetPlayerName));
            inviter.sendMessage(Component.text("§7目标世界: §e" + worldName));
        }
        
        // 通知被邀请的玩家
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(Component.text("§c你进入世界 §e" + worldName + "§c 的邀请申请已被拒绝！"));
            targetPlayer.sendMessage(Component.text("§7邀请者: §e" + inviterName));
        }
        
        return true;
    }

    private boolean handleLoad(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.load")) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§c用法: /world load <世界名>"));
            return true;
        }

        String worldName = args[1];
        if (Bukkit.getWorld(worldName) != null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 已经加载！"));
            return true;
        }

        try {
            World world = Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));
            if (world != null) {
                // 检查是否已有配置文件，如果没有则创建
                if (!WorldConfig.isWorldLoaded(worldName)) {
                    WorldConfig.createWorldSettings(worldName, player.getName());
                    player.sendMessage(Component.text("§e已为世界 '" + worldName + "' 创建配置文件"));
                }
                
                WorldConfig.applyGameRules(world, worldName); // 应用游戏规则
                player.sendMessage(Component.text("§a世界 '" + worldName + "' 加载成功！"));
            } else {
                player.sendMessage(Component.text("§c世界 '" + worldName + "' 加载失败！"));
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("§c加载世界时发生错误：" + e.getMessage()));
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleUnload(Player player, String[] args) {
        if (!player.hasPermission("mutbuildutils.world.unload")) {
            player.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§c用法: /world unload <世界名>"));
            return true;
        }

        String worldName = args[1];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(Component.text("§c世界 '" + worldName + "' 不存在或未加载！"));
            return true;
        }

        if (world.equals(Bukkit.getWorlds().get(0))) {
            player.sendMessage(Component.text("§c无法卸载主世界！"));
            return true;
        }

        // 将该世界中的所有玩家传送到主世界
        World mainWorld = Bukkit.getWorlds().get(0);
        world.getPlayers().forEach(p -> {
            p.teleport(mainWorld.getSpawnLocation());
            p.sendMessage(Component.text("§e由于世界卸载，你已被传送至主世界。"));
        });

        if (Bukkit.unloadWorld(world, true)) {
            // 将世界的 load 状态设为 false
            WorldConfig.setWorldLoadStatus(worldName, false);
            player.sendMessage(Component.text("§a世界 '" + worldName + "' 已成功卸载！"));
        } else {
            player.sendMessage(Component.text("§c卸载世界 '" + worldName + "' 失败！"));
        }
        return true;
    }

    private boolean handleOwnList(Player player, String[] args) {
        // 打开自己创建的世界菜单
        OwnWorldMenu.openOwnWorldMenu(player);
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("§6=== 世界命令帮助 ==="));
        player.sendMessage(Component.text("§f/world tp <世界名> §7- 传送到指定世界"));
        if (player.hasPermission("mutbuildutils.world.invite")) {
            player.sendMessage(Component.text("§f/world invite <被邀请玩家> <目标世界> <审核OP> §7- 邀请玩家进入指定世界"));
            player.sendMessage(Component.text("§f/world kick <玩家名> <世界名> §7- 踢出玩家的世界邀请权限"));
        }
        if (player.hasPermission("mutbuildutils.world.load")) {
            player.sendMessage(Component.text("§f/world load <世界名> §7- 加载世界"));
        }
        if (player.hasPermission("mutbuildutils.world.unload")) {
            player.sendMessage(Component.text("§f/world unload <世界名> §7- 卸载世界"));
        }
        if (player.hasPermission("mutbuildutils.world.admin") || player.isOp()) {
            player.sendMessage(Component.text("§f/world approve <邀请者> §7- 同意邀请申请"));
            player.sendMessage(Component.text("§f/world deny <邀请者> §7- 拒绝邀请申请"));
        }
        player.sendMessage(Component.text("§f/world ownlist §7- 查看自己创建的世界"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("tp");
            subCommands.add("ownlist"); // 所有玩家都可以使用ownlist
            if (sender.hasPermission("mutbuildutils.world.invite")) {
                subCommands.add("invite");
            }
            if (sender.hasPermission("mutbuildutils.world.load")) {
                subCommands.add("load");
            }
            if (sender.hasPermission("mutbuildutils.world.unload")) {
                subCommands.add("unload");
            }
            if (sender.hasPermission("mutbuildutils.world.admin") || sender.isOp()) {
                subCommands.add("approve");
                subCommands.add("deny");
            }
            subCommands.add("kick"); // 所有玩家都可以使用kick（但需要是世界所有者）
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            List<String> worlds = new ArrayList<>();
            switch (args[0].toLowerCase()) {
                case "tp":
                case "unload":
                    // 只显示已加载的世界
                    worlds.addAll(Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .collect(Collectors.toList()));
                    break;
                case "load":
                    // 显示可加载的世界文件夹
                    File worldContainer = Bukkit.getWorldContainer();
                    File[] files = worldContainer.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory() && new File(file, "level.dat").exists()) {
                                String worldName = file.getName();
                                // 排除已加载的世界
                                if (Bukkit.getWorld(worldName) == null) {
                                    worlds.add(worldName);
                                }
                            }
                        }
                    }
                    break;
                case "invite":
                    // 显示非OP的在线玩家
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !p.isOp())
                            .map(Player::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "approve":
                case "deny":
                    // 显示在线玩家名（邀请者）
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "kick":
                    // 显示在线玩家名
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
            return worlds.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "invite":
                    // 显示已加载的世界
                    return Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                case "kick":
                    // 显示已加载的世界名
                    return Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "invite":
                    // 显示OP玩家
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.isOp())
                            .map(Player::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}