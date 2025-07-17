package mut.buildup.mutbuildutils.config;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class WorldConfig {
    private static File worldConfigDir;
    private static final Map<String, WorldSettings> worldSettings = new HashMap<>();
    private static final Map<String, FileConfiguration> worldConfigs = new HashMap<>();
    private static FileConfiguration mainConfig;

    public static void loadConfig(File file) {
        // file参数现在指向插件数据目录
        worldConfigDir = new File(file.getParentFile(), "world/config");
        if (!worldConfigDir.exists()) {
            worldConfigDir.mkdirs();
        }
        
        // 加载主配置文件
        File mainConfigFile = new File(file.getParentFile(), "config.yml");
        if (mainConfigFile.exists()) {
            mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        }
        
        // 确保默认世界配置文件存在
        createDefaultWorldConfigs();
        reloadConfig();
    }

    public static void reloadConfig() {
        if (worldConfigDir == null) {
            throw new IllegalStateException("世界配置目录未初始化");
        }
        
        // 重新加载主配置文件
        File mainConfigFile = new File(worldConfigDir.getParentFile(), "config.yml");
        if (mainConfigFile.exists()) {
            mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        }
        
        loadAllWorldSettings();
    }
    
    /**
     * 为默认世界创建配置文件
     */
    private static void createDefaultWorldConfigs() {
        // 默认世界列表
        String[] defaultWorlds = {"world", "world_nether", "world_the_end"};
        
        for (String worldName : defaultWorlds) {
            File configFile = new File(worldConfigDir, worldName + ".yml");
            if (!configFile.exists()) {
                System.out.println("[WorldConfig] 为默认世界创建配置文件: " + worldName);
                createDefaultWorldConfig(worldName, "Server");
            }
        }
    }
    
    /**
     * 创建默认世界配置
     */
    private static void createDefaultWorldConfig(String worldName, String ownerName) {
        File configFile = new File(worldConfigDir, worldName + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        // 设置配置内容
        config.set("spawnpoint.x", 0);
        config.set("spawnpoint.y", 64);
        config.set("spawnpoint.z", 0);
        config.set("spawnpoint.yaw", 0);
        config.set("spawnpoint.pitch", 0);
        config.set("gamemode", "SURVIVAL"); // 默认世界使用生存模式
        config.set("load", true); // 默认世界自动加载
        config.set("players", ""); // 默认世界允许所有玩家进入
        config.set("owner", ownerName);
        config.set("description", "默认世界 - " + worldName);
        config.set("created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        // 设置默认游戏规则
        config.set("gamerules.keepInventory", false);
        config.set("gamerules.doDaylightCycle", true);
        config.set("gamerules.doMobSpawning", true);
        config.set("gamerules.announceAdvancements", true);
        config.set("gamerules.doImmediateRespawn", false);
        config.set("gamerules.spawnRadius", 10);
        
        // 设置资源包配置 - 从主配置文件读取默认世界资源包设置
        String defaultMainPack = "";
        String defaultBasePack = "base";
        
        if (mainConfig != null) {
            defaultMainPack = mainConfig.getString("default-world-resourcepack.main", "");
            defaultBasePack = mainConfig.getString("default-world-resourcepack.base", "base");
        }
        
        config.set("resourcepack.main", defaultMainPack);
        config.set("resourcepack.base", defaultBasePack);
        
        worldConfigs.put(worldName, config);
        saveWorldConfig(worldName, config);
        
        // 创建内存中的设置
        List<String> allowedPlayers = new ArrayList<>(); // 空列表表示所有玩家都可以进入
        WorldSettings newSettings = new WorldSettings(
            0, 64, 0, 0, 0, GameMode.SURVIVAL, new HashMap<>(), true, allowedPlayers,
            defaultMainPack, defaultBasePack
        );
        worldSettings.put(worldName, newSettings);
    }

    private static void loadAllWorldSettings() {
        worldSettings.clear();
        worldConfigs.clear();
        
        if (!worldConfigDir.exists()) {
            return;
        }
        
        File[] configFiles = worldConfigDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (configFiles == null) {
            return;
        }
        
        for (File configFile : configFiles) {
            String worldName = configFile.getName().replace(".yml", "");
            loadWorldSettings(worldName, configFile);
        }
    }
    
    private static void loadWorldSettings(String worldName, File configFile) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            worldConfigs.put(worldName, config);
            
            ConfigurationSection spawnpoint = config.getConfigurationSection("spawnpoint");
            if (spawnpoint == null) {
                return;
            }

            double x = spawnpoint.getDouble("x", 0);
            double y = spawnpoint.getDouble("y", 64);
            double z = spawnpoint.getDouble("z", 0);
            float yaw = (float) spawnpoint.getDouble("yaw", 0);
            float pitch = (float) spawnpoint.getDouble("pitch", 0);

            GameMode gamemode = GameMode.valueOf(config.getString("gamemode", "CREATIVE"));
            
            // 加载 load 状态
            boolean load = config.getBoolean("load", false);
            
            // 加载允许进入的玩家列表
            String playersString = config.getString("players", "");
            List<String> allowedPlayers = new ArrayList<>();
            if (!playersString.isEmpty()) {
                allowedPlayers.addAll(Arrays.asList(playersString.split(",")));
                // 清理空白字符
                allowedPlayers.replaceAll(String::trim);
                allowedPlayers.removeIf(String::isEmpty);
            }

            // 加载游戏规则配置
            Map<GameRule<?>, Object> gameRules = new HashMap<>();
            ConfigurationSection gameRulesSection = config.getConfigurationSection("gamerules");
            if (gameRulesSection != null) {
                for (String ruleName : gameRulesSection.getKeys(false)) {
                    try {
                        GameRule<?> gameRule = GameRule.getByName(ruleName);
                        if (gameRule != null) {
                            if (gameRule.getType() == Boolean.class) {
                                gameRules.put(gameRule, gameRulesSection.getBoolean(ruleName));
                            } else if (gameRule.getType() == Integer.class) {
                                gameRules.put(gameRule, gameRulesSection.getInt(ruleName));
                            }
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            // 加载资源包配置
            String mainResourcePack = config.getString("resourcepack.main", "");
            String baseResourcePack = config.getString("resourcepack.base", "");
            
            worldSettings.put(worldName, new WorldSettings(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, mainResourcePack, baseResourcePack));
        } catch (Exception e) {
            System.err.println("加载世界配置失败: " + worldName + " - " + e.getMessage());
        }
    }

    public static WorldSettings getWorldSettings(String worldName) {
        return worldSettings.get(worldName);
    }
    
    public static Map<String, WorldSettings> getAllWorldSettings() {
        return new HashMap<>(worldSettings);
    }

    public static Location getSpawnLocation(World world) {
        WorldSettings settings = worldSettings.get(world.getName());
        if (settings == null) return world.getSpawnLocation();
        return new Location(world, settings.x, settings.y, settings.z, settings.yaw, settings.pitch);
    }

    public static GameMode getDefaultGameMode(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null ? settings.gamemode : GameMode.CREATIVE;
    }

    public static boolean shouldLoadOnStartup(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null && settings.load;
    }

    public static boolean canPlayerEnterWorld(String worldName, String playerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings == null) return true;
        return settings.allowedPlayers.isEmpty() || settings.allowedPlayers.contains(playerName);
    }

    public static void setWorldLoadStatus(String worldName, boolean load) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, load, settings.allowedPlayers,
                settings.mainResourcePack, settings.baseResourcePack
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("load", load);
                saveWorldConfig(worldName, config);
            }
        }
    }

    public static void addPlayerToWorld(String worldName, String playerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null && !settings.allowedPlayers.contains(playerName)) {
            List<String> newPlayers = new ArrayList<>(settings.allowedPlayers);
            newPlayers.add(playerName);
            
            // 更新内存中的设置
                WorldSettings newSettings = new WorldSettings(
                    settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                    settings.gamemode, settings.gameRules, settings.load, newPlayers,
                    settings.mainResourcePack, settings.baseResourcePack
                );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("players", String.join(",", newPlayers));
                saveWorldConfig(worldName, config);
            }
        }
    }

    public static void removePlayerFromWorld(String worldName, String playerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null && settings.allowedPlayers.contains(playerName)) {
            List<String> newPlayers = new ArrayList<>(settings.allowedPlayers);
            newPlayers.remove(playerName);
            
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, newPlayers,
                settings.mainResourcePack, settings.baseResourcePack
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("players", String.join(",", newPlayers));
                saveWorldConfig(worldName, config);
            }
        }
    }

    public static void createWorldSettings(String worldName, String ownerName) {
        createWorldSettings(worldName, ownerName, null);
    }

    public static void createWorldSettings(String worldName, String ownerName, String worldType) {
        if (!worldSettings.containsKey(worldName)) {
            List<String> allowedPlayers = new ArrayList<>();
            allowedPlayers.add(ownerName);
            
            // 根据世界类型确定资源包
            String mainResourcePack = getResourcePackKeyByWorldType(worldType);
            String baseResourcePack = "base";
            
            WorldSettings newSettings = new WorldSettings(
                0, 64, 0, 0, 0, GameMode.CREATIVE, new HashMap<>(), true, allowedPlayers,
                mainResourcePack, baseResourcePack
            );
            worldSettings.put(worldName, newSettings);
            
            // 创建新的配置文件
            File configFile = new File(worldConfigDir, worldName + ".yml");
            FileConfiguration config = new YamlConfiguration();
            
            // 设置配置内容
            config.set("spawnpoint.x", 0);
            config.set("spawnpoint.y", 64);
            config.set("spawnpoint.z", 0);
            config.set("spawnpoint.yaw", 0);
            config.set("spawnpoint.pitch", 0);
            config.set("gamemode", "CREATIVE");
            config.set("load", true);
            config.set("players", ownerName);
            config.set("owner", ownerName);
            config.set("description", "由 " + ownerName + " 创建的世界");
            config.set("created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // 设置默认游戏规则
            config.set("gamerules.keepInventory", true);
            config.set("gamerules.doDaylightCycle", true);
            config.set("gamerules.doMobSpawning", false);
            config.set("gamerules.announceAdvancements", false);
            config.set("gamerules.doImmediateRespawn", true);
            config.set("gamerules.spawnRadius", 0);
            
            // 设置资源包配置
            config.set("resourcepack.main", mainResourcePack != null ? mainResourcePack : "");
            config.set("resourcepack.base", baseResourcePack);
            
            worldConfigs.put(worldName, config);
            saveWorldConfig(worldName, config);
        }
    }

    /**
     * 根据世界类型获取对应的资源包缩写
     */
    private static String getResourcePackKeyByWorldType(String worldType) {
        if (worldType == null) return null;
        
        switch (worldType.toLowerCase()) {
            case "normal":
                return "normal";
            case "flat":
                return "flat";
            case "nether":
                return "nether";
            case "end":
                return "end";
            case "ocean":
                return "ocean";
            case "desert":
                return "desert";
            case "snow":
                return "snow";
            default:
                return worldType; // 默认使用世界类型作为资源包缩写
        }
    }

    private static void saveWorldConfig(String worldName, FileConfiguration config) {
        try {
            File configFile = new File(worldConfigDir, worldName + ".yml");
            config.save(configFile);
        } catch (IOException e) {
            System.err.println("保存世界配置失败: " + worldName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void saveAllConfigs() {
        for (Map.Entry<String, FileConfiguration> entry : worldConfigs.entrySet()) {
            saveWorldConfig(entry.getKey(), entry.getValue());
        }
    }

    public static void applyGameRules(World world, String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null && settings.gameRules != null) {
            for (Map.Entry<GameRule<?>, Object> entry : settings.gameRules.entrySet()) {
                setGameRule(world, entry.getKey(), entry.getValue());
            }
        } else {
            // 设置默认游戏规则
            world.setGameRule(GameRule.SPAWN_RADIUS, 0);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void setGameRule(World world, GameRule<T> gameRule, Object value) {
        world.setGameRule(gameRule, (T) value);
    }

    /**
     * 获取需要在服务器启动时自动加载的世界列表
     */
    public static List<String> getWorldsToLoadOnStartup() {
        List<String> worldsToLoad = new ArrayList<>();
        for (Map.Entry<String, WorldSettings> entry : worldSettings.entrySet()) {
            if (entry.getValue().load) {
                worldsToLoad.add(entry.getKey());
            }
        }
        return worldsToLoad;
    }

    /**
     * 获取需要加载的世界列表（别名方法）
     */
    public static List<String> getWorldsToLoad() {
        return getWorldsToLoadOnStartup();
    }

    /**
     * 使用Plugin实例加载配置
     */
    public static void loadConfig(org.bukkit.plugin.Plugin plugin) {
        worldConfigDir = new File(plugin.getDataFolder(), "world/config");
        if (!worldConfigDir.exists()) {
            worldConfigDir.mkdirs();
        }
        reloadConfig();
    }
    
    /**
     * 获取世界配置目录
     */
    public static File getWorldConfigDir() {
        return worldConfigDir;
    }
    
    /**
     * 检查世界是否已加载到内存中
     */
    public static boolean isWorldLoaded(String worldName) {
        return worldSettings.containsKey(worldName);
    }
    
    /**
     * 删除世界配置
     */
    public static void removeWorldConfig(String worldName) {
        worldSettings.remove(worldName);
        worldConfigs.remove(worldName);
        File configFile = new File(worldConfigDir, worldName + ".yml");
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    /**
     * 设置世界的主资源包
     */
    public static void setWorldMainResourcePack(String worldName, String resourcePackKey) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers,
                resourcePackKey, settings.baseResourcePack
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("resourcepack.main", resourcePackKey);
                saveWorldConfig(worldName, config);
            }
        }
    }

    /**
     * 设置世界的基础资源包
     */
    public static void setWorldBaseResourcePack(String worldName, String resourcePackKey) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers,
                settings.mainResourcePack, resourcePackKey
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("resourcepack.base", resourcePackKey);
                saveWorldConfig(worldName, config);
            }
        }
    }

    /**
     * 获取世界的主资源包
     */
    public static String getWorldMainResourcePack(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null ? settings.mainResourcePack : null;
    }

    /**
     * 获取世界的基础资源包
     */
    public static String getWorldBaseResourcePack(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null ? settings.baseResourcePack : null;
    }

    public static class WorldSettings {
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;
        private final GameMode gamemode;
        private final Map<GameRule<?>, Object> gameRules;
        private final boolean load;
        private final List<String> allowedPlayers;
        private final String mainResourcePack;
        private final String baseResourcePack;

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<String> allowedPlayers) {
            this(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, null, null);
        }

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<String> allowedPlayers,
                           String mainResourcePack, String baseResourcePack) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.gamemode = gamemode;
            this.gameRules = gameRules;
            this.load = load;
            this.allowedPlayers = new ArrayList<>(allowedPlayers);
            this.mainResourcePack = mainResourcePack;
            this.baseResourcePack = baseResourcePack;
        }

        public Location createLocation(World world) {
            return new Location(world, x, y, z, yaw, pitch);
        }

        public GameMode getGamemode() {
            return gamemode;
        }

        public Map<GameRule<?>, Object> getGameRules() {
            return gameRules;
        }

        public boolean shouldLoad() {
            return load;
        }

        public List<String> getAllowedPlayers() {
            return new ArrayList<>(allowedPlayers);
        }
        
        public List<String> getPlayers() {
            return getAllowedPlayers();
        }

        public String getMainResourcePack() {
            return mainResourcePack;
        }

        public String getBaseResourcePack() {
            return baseResourcePack;
        }
    }
}