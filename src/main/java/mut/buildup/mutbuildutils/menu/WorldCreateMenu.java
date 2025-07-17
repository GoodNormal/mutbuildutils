package mut.buildup.mutbuildutils.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import mut.buildup.mutbuildutils.config.MenuConfig;
import mut.buildup.mutbuildutils.config.MenuConfig.WorldMenuItem;

import java.util.Map;

public class WorldCreateMenu {

    public static void openMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, MenuConfig.getMenuSize(), Component.text("§6世界创建菜单"));

        // 设置装饰性物品
        ItemStack decorItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta decorMeta = decorItem.getItemMeta();
        decorMeta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        decorItem.setItemMeta(decorMeta);

        // 填充所有位置为装饰性物品
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, decorItem);
        }

        // 设置世界创建按钮
        for (Map.Entry<Integer, WorldMenuItem> entry : MenuConfig.getMenuItems().entrySet()) {
            int slot = entry.getKey();
            WorldMenuItem menuItem = entry.getValue();
            inventory.setItem(slot, menuItem.getItem());
        }

        player.openInventory(inventory);
    }
}