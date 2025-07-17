package mut.buildup.mutbuildutils.commands;

import mut.buildup.mutbuildutils.config.MenuConfig;
import mut.buildup.mutbuildutils.config.WorldConfig;
import mut.buildup.mutbuildutils.menu.PlayerManagementMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "player":
                return handlePlayer(sender, args);
            default:
                sendUsage(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mutbuild.reload")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        try {
            // 重新加载配置
            MenuConfig.reloadConfig();
            WorldConfig.reloadConfig();
            sender.sendMessage(Component.text("§a配置重载成功！"));
        } catch (Exception e) {
            sender.sendMessage(Component.text("§c配置重载失败：" + e.getMessage()));
            e.printStackTrace();
        }
        return true;
    }

    private boolean handlePlayer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("§c该命令只能由玩家执行！"));
            return true;
        }

        if (!sender.hasPermission("mutbuild.player") && !sender.isOp()) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令！"));
            return true;
        }

        Player player = (Player) sender;
        PlayerManagementMenu.openPlayerMenu(player, 1);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("§6=== Mutbuild命令帮助 ==="));
        if (sender.hasPermission("mutbuild.reload")) {
            sender.sendMessage(Component.text("§f/mutbuild reload §7- 重新加载配置文件"));
        }
        if (sender.hasPermission("mutbuild.player") || sender.isOp()) {
            sender.sendMessage(Component.text("§f/mutbuild player §7- 打开玩家管理菜单"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("mutbuild.reload")) {
                subCommands.add("reload");
            }
            if (sender.hasPermission("mutbuild.player") || sender.isOp()) {
                subCommands.add("player");
            }
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}