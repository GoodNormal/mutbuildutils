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

        for (Map<?, ?> worldMap : worldsList) {
            Object slotObj = worldMap.get("slot");
            if (!(slotObj instanceof Integer)) continue;
            int slot = (Integer) slotObj;
            
            if (slot < 0 || slot >= menuSize) continue;

            Object materialObj = worldMap.get("material");
            if (!(materialObj instanceof String)) continue;
            String materialName = (String) materialObj;

            Material material = Material.getMaterial(materialName.toUpperCase());
            if (material == null) continue;

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

            ItemStack itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(displayName));
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(Component.text(line));
                }
                meta.lore(loreComponents);
                
                // 支持CustomModelData (1.21.4兼容)
                Object customModelDataObj = worldMap.get("custom_model_data");
                if (customModelDataObj != null) {
                    try {
                        if (customModelDataObj instanceof Integer) {
                            meta.setCustomModelData((Integer) customModelDataObj);
                        } else if (customModelDataObj instanceof String) {
                            // 支持字符串形式的CustomModelData
                            String customModelDataStr = (String) customModelDataObj;
                            try {
                                int customModelDataInt = Integer.parseInt(customModelDataStr);
                                meta.setCustomModelData(customModelDataInt);
                            } catch (NumberFormatException e) {
                                // 如果字符串不是数字，忽略此设置
                                System.out.println("警告: CustomModelData值 '" + customModelDataStr + "' 不是有效的整数");
                            }
                        } else if (customModelDataObj instanceof Double || customModelDataObj instanceof Float) {
                            // 支持浮点数形式的CustomModelData (转换为整数)
                            Number customModelDataNum = (Number) customModelDataObj;
                            meta.setCustomModelData(customModelDataNum.intValue());
                        }
                    } catch (Exception e) {
                        System.out.println("设置CustomModelData时出错: " + e.getMessage());
                    }
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