package mut.buildup.mutbuildutils.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuConfig {
    private static FileConfiguration config;
    private static final Map<Integer, WorldMenuItem> menuItems = new HashMap<>();
    private static int menuSize = 54;
    private static File configFile;

    public static void loadConfig(File file) {
        configFile = file;
        reloadConfig();
    }

    public static void reloadConfig() {
        if (configFile == null) {
            throw new IllegalStateException("配置文件未初始化");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        menuItems.clear();

        // 加载菜单大小
        menuSize = config.getInt("menu_size", 54);
        // 确保菜单大小是9的倍数且不超过54
        if (menuSize % 9 != 0 || menuSize <= 0 || menuSize > 54) {
            menuSize = 54;
        }

        loadMenuItems();
    }

    private static void loadMenuItems() {
        List<Map<?, ?>> worldsList = config.getMapList("worlds");
        if (worldsList == null) return;

        // 获取世界菜单配置实例
        WorldMenuConfig worldMenuConfig = WorldConfig.getWorldMenuConfig();
        if (worldMenuConfig == null) {
            System.err.println("[MenuConfig] 世界菜单配置未初始化，尝试手动初始化");
            // 尝试手动初始化WorldMenuConfig
            File menuConfigFile = new File(configFile.getParentFile(), "world/menu.yml");
            worldMenuConfig = new WorldMenuConfig(menuConfigFile);
            System.out.println("[MenuConfig] 手动初始化世界菜单配置完成");
        }

        for (Map<?, ?> worldMap : worldsList) {
            Object slotObj = worldMap.get("slot");
            if (!(slotObj instanceof Integer)) continue;
            int slot = (Integer) slotObj;
            
            if (slot < 0 || slot >= menuSize) continue;

            Object nameObj = worldMap.get("name");
            String displayName = nameObj instanceof String ? (String) nameObj : "未命名";
            
            Object loreObj = worldMap.get("lore");
            List<String> lore = new ArrayList<>();
            if (loreObj instanceof List) {
                List<?> loreList = (List<?>) loreObj;
                for (Object line : loreList) {
                    if (line instanceof String) {
                        lore.add((String) line);
                    }
                }
            }
            
            Object worldObj = worldMap.get("world");
            String worldType = worldObj instanceof String ? (String) worldObj : "default";

            // 从world/menu.yml配置中获取材料信息
            WorldMenuConfig.MaterialInfo materialInfo = worldMenuConfig.getMaterialInfo(worldType);
            Material material = materialInfo.getMaterial();
            int customModelData = materialInfo.getCustomModelData();

            ItemStack itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(displayName));
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(Component.text(line));
                }
                meta.lore(loreComponents);
                
                // 设置自定义模型数据
                if (customModelData != 0) {
                    meta.setCustomModelData(customModelData);
                }
                
                itemStack.setItemMeta(meta);
            }

            menuItems.put(slot, new WorldMenuItem(itemStack, worldType));
        }
    }

    public static Map<Integer, WorldMenuItem> getMenuItems() {
        return menuItems;
    }

    public static int getMenuSize() {
        return menuSize;
    }

    public static class WorldMenuItem {
        private final ItemStack item;
        private final String worldType;

        public WorldMenuItem(ItemStack item, String worldType) {
            this.item = item;
            this.worldType = worldType;
        }

        public ItemStack getItem() {
            return item;
        }

        public String getWorldType() {
            return worldType;
        }
    }
}