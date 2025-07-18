package mut.buildup.mutbuildutils.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WorldMenuConfig {
    private FileConfiguration config;
    private File configFile;
    private final Map<String, MaterialInfo> materialMap = new HashMap<>();
    
    public WorldMenuConfig(File file) {
        this.configFile = file;
        loadConfig();
    }
    
    public void loadConfig() {
        if (configFile == null) {
            throw new IllegalStateException("世界菜单配置文件未初始化");
        }
        
        // 如果配置文件不存在，创建默认配置
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        loadMaterials();
    }
    
    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig() {
        try {
            // 确保父目录存在
            File parentDir = configFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 创建默认配置
            FileConfiguration defaultConfig = new YamlConfiguration();
            
            // 添加配置说明
            defaultConfig.set("# 世界菜单材料配置文件", null);
            defaultConfig.set("# 这个文件定义了世界菜单中使用的材料缩写和对应的材料信息", null);
            defaultConfig.set("# abbreviation: 缩写名称", null);
            defaultConfig.set("# material: Minecraft材料名称", null);
            defaultConfig.set("# custom_model_data: 自定义模型数据（可选，默认为0）", null);
            
            // 添加默认材料配置
            defaultConfig.set("materials.default.material", "GRASS_BLOCK");
            defaultConfig.set("materials.default.custom_model_data", 0);
            
            defaultConfig.set("materials.stone.material", "STONE");
            defaultConfig.set("materials.stone.custom_model_data", 0);
            
            defaultConfig.set("materials.dirt.material", "DIRT");
            defaultConfig.set("materials.dirt.custom_model_data", 0);
            
            defaultConfig.set("materials.sand.material", "SAND");
            defaultConfig.set("materials.sand.custom_model_data", 0);
            
            defaultConfig.set("materials.wood.material", "OAK_LOG");
            defaultConfig.set("materials.wood.custom_model_data", 0);
            
            defaultConfig.set("materials.water.material", "WATER_BUCKET");
            defaultConfig.set("materials.water.custom_model_data", 0);
            
            defaultConfig.set("materials.nether.material", "NETHERRACK");
            defaultConfig.set("materials.nether.custom_model_data", 0);
            
            defaultConfig.set("materials.end.material", "END_STONE");
            defaultConfig.set("materials.end.custom_model_data", 0);
            
            defaultConfig.set("materials.creative.material", "COMMAND_BLOCK");
            defaultConfig.set("materials.creative.custom_model_data", 0);
            
            defaultConfig.set("materials.survival.material", "IRON_SWORD");
            defaultConfig.set("materials.survival.custom_model_data", 0);
            
            // 保存配置文件
            defaultConfig.save(configFile);
            System.out.println("[WorldMenuConfig] 已创建默认世界菜单配置文件: " + configFile.getPath());
            
        } catch (Exception e) {
            System.err.println("[WorldMenuConfig] 创建默认配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadMaterials() {
        materialMap.clear();
        
        ConfigurationSection materialsSection = config.getConfigurationSection("materials");
        if (materialsSection == null) {
            return;
        }
        
        for (String key : materialsSection.getKeys(false)) {
            ConfigurationSection materialSection = materialsSection.getConfigurationSection(key);
            if (materialSection != null) {
                String materialName = materialSection.getString("material", "GRASS_BLOCK");
                int customModelData = materialSection.getInt("custom_model_data", 0);
                
                materialMap.put(key, new MaterialInfo(materialName, customModelData));
            }
        }
    }
    
    /**
     * 根据缩写获取材料信息
     */
    public MaterialInfo getMaterialInfo(String abbreviation) {
        MaterialInfo info = materialMap.get(abbreviation);
        if (info == null) {
            // 如果找不到对应的缩写，返回默认材料
            info = materialMap.get("default");
            if (info == null) {
                info = new MaterialInfo("GRASS_BLOCK", 0);
            }
        }
        return info;
    }
    
    /**
     * 检查缩写是否存在
     */
    public boolean hasAbbreviation(String abbreviation) {
        return materialMap.containsKey(abbreviation);
    }
    
    /**
     * 获取所有可用的缩写
     */
    public java.util.Set<String> getAllAbbreviations() {
        return materialMap.keySet();
    }
    
    /**
     * 材料信息类
     */
    public static class MaterialInfo {
        private final String materialName;
        private final int customModelData;
        
        public MaterialInfo(String materialName, int customModelData) {
            this.materialName = materialName;
            this.customModelData = customModelData;
        }
        
        public String getMaterialName() {
            return materialName;
        }
        
        public Material getMaterial() {
            try {
                return Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Material.GRASS_BLOCK; // 默认材料
            }
        }
        
        public int getCustomModelData() {
            return customModelData;
        }
    }
}