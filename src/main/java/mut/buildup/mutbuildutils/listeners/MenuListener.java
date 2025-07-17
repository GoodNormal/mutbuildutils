package mut.buildup.mutbuildutils.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import mut.buildup.mutbuildutils.config.MenuConfig;
import mut.buildup.mutbuildutils.config.MenuConfig.WorldMenuItem;
import mut.buildup.mutbuildutils.world.WorldTemplateManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MenuListener implements Listener {

    private final WorldTemplateManager worldTemplateManager;
    private final Map<Player, WorldMenuItem> pendingWorldCreation = new HashMap<>();

    public MenuListener(WorldTemplateManager worldTemplateManager) {
        this.worldTemplateManager = worldTemplateManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("§6世界创建菜单"))) {
            return;
        }

        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int clickedSlot = event.getSlot();
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            return;
        }

        WorldMenuItem menuItem = MenuConfig.getMenuItems().get(clickedSlot);
        if (menuItem != null) {
            player.closeInventory();
            pendingWorldCreation.put(player, menuItem);
            player.sendMessage(Component.text("§e请在聊天栏中输入游戏名缩写（例如：MC）"));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        WorldMenuItem menuItem = pendingWorldCreation.get(player);

        if (menuItem != null) {
            event.setCancelled(true);
            String gameName = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

            // 移除玩家的待创建状态
            pendingWorldCreation.remove(player);

            // 在主线程中执行世界创建
            Bukkit.getScheduler().runTask(worldTemplateManager.getPlugin(), () -> {
                player.sendMessage(Component.text("§a正在为您创建").append(menuItem.getItem().getItemMeta().displayName()).append(Component.text("...")));
                
                try {
                    World world = worldTemplateManager.createWorld(menuItem.getWorldType(), gameName, player.getName());
                    player.teleport(world.getSpawnLocation());
                    player.sendMessage(Component.text("§a世界创建成功！已将您传送至新世界。"));
                } catch (IOException e) {
                    player.sendMessage(Component.text("§c创建世界失败：" + e.getMessage()));
                    e.printStackTrace();
                }
            });
        }
    }
}