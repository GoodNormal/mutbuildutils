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
        // 获取主世界名称（从server.properties中的level-name读取）
        String mainWorldName = "world";
        if (!org.bukkit.Bukkit.getWorlds().isEmpty()) {
            mainWorldName = org.bukkit.Bukkit.getWorlds().get(0).getName();
        }
        System.out.println("[WorldConfig] 检测到主世界名称: " + mainWorldName);
        
        // 构建默认世界列表（基于主世界名称），但只为实际存在的世界创建配置
        String[] potentialDefaultWorlds = {mainWorldName, mainWorldName + "_nether", mainWorldName + "_the_end"};
        
        for (String worldName : potentialDefaultWorlds) {
            // 检查世界文件夹是否存在，确保服务器真的开启了这个世界
            File worldFolder = new File(org.bukkit.Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists() && worldFolder.isDirectory()) {
                File configFile = new File(worldConfigDir, worldName + ".yml");
                if (!configFile.exists()) {
                    System.out.println("[WorldConfig] 为默认世界创建配置文件: " + worldName);
                    createDefaultWorldConfig(worldName, "Server");
                }
            } else {
                System.out.println("[WorldConfig] 跳过不存在的世界: " + worldName);
            }
        }
    }
    
    /**
     * 创建默认世界配置
     * @param worldName 世界名称
     * @param ownerName 创建者
     */
    private static void createDefaultWorldConfig(String worldName, String ownerName) {
        File configFile = new File(worldConfigDir, worldName + ".yml");
        FileConfiguration config = new YamlConfiguration();
        
        // 获取主世界名称（从已加载的世界列表中获取第一个世界）
        String mainWorldName = "world";
        if (!org.bukkit.Bukkit.getWorlds().isEmpty()) {
            mainWorldName = org.bukkit.Bukkit.getWorlds().get(0).getName();
        }
        
        // 尝试获取世界的真实出生点
        World world = org.bukkit.Bukkit.getWorld(worldName);
        double defaultX = 0;
        double defaultY = 64;
        double defaultZ = 0;
        float defaultYaw = 0;
        float defaultPitch = 0;
        
        if (world != null) {
            // 如果世界已加载，使用真实出生点
            Location spawnLoc = world.getSpawnLocation();
            defaultX = spawnLoc.getX();
            defaultY = spawnLoc.getY();
            defaultZ = spawnLoc.getZ();
            defaultYaw = spawnLoc.getYaw();
            defaultPitch = spawnLoc.getPitch();
        } else if (mainConfig != null) {
            // 否则从主配置文件读取默认出生点设置
            defaultX = mainConfig.getDouble("default-settings.spawn.x", 0);
            defaultY = mainConfig.getDouble("default-settings.spawn.y", 64);
            defaultZ = mainConfig.getDouble("default-settings.spawn.z", 0);
            defaultYaw = (float) mainConfig.getDouble("default-settings.spawn.yaw", 0);
            defaultPitch = (float) mainConfig.getDouble("default-settings.spawn.pitch", 0);
        }
        
        // 设置配置内容
        config.set("spawnpoint.x", defaultX);
        config.set("spawnpoint.y", defaultY);
        config.set("spawnpoint.z", defaultZ);
        config.set("spawnpoint.yaw", defaultYaw);
        config.set("spawnpoint.pitch", defaultPitch);
        
        // 根据世界类型设置游戏模式
        String gameMode = "SURVIVAL";
        if (worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
            gameMode = "SURVIVAL"; // 下界和末地使用生存模式
        } else if (worldName.startsWith(mainWorldName + "_")) {
            gameMode = "CREATIVE"; // 其他自定义世界使用创造模式
        }
        
        config.set("gamemode", gameMode);
        config.set("load", true); // 默认世界自动加载
        config.set("players", ""); // 默认世界允许所有玩家进入
        config.set("owner", ownerName);
        config.set("description", "默认世界 - " + worldName);
        config.set("created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        // 设置默认游戏规则 - 根据世界类型设置不同的规则
        boolean keepInventory = false;
        boolean doMobSpawning = true;
        boolean announceAdvancements = true;
        boolean doImmediateRespawn = false;
        int spawnRadius = 10;
        
        if (worldName.equals(mainWorldName)) {
            // 主世界的特殊设置
            keepInventory = mainConfig != null ? mainConfig.getBoolean("default-settings.keep-inventory", false) : false;
            spawnRadius = mainConfig != null ? mainConfig.getInt("default-settings.spawn-protection", 10) : 10;
        } else if (worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
            // 下界和末地的特殊设置
            keepInventory = true;
            doImmediateRespawn = true;
        }
        
        config.set("gamerules.keepInventory", keepInventory);
        config.set("gamerules.doDaylightCycle", true);
        config.set("gamerules.doMobSpawning", doMobSpawning);
        config.set("gamerules.announceAdvancements", announceAdvancements);
        config.set("gamerules.doImmediateRespawn", doImmediateRespawn);
        config.set("gamerules.spawnRadius", spawnRadius);
        
        // 设置资源包配置 - 从主配置文件读取默认世界资源包设置
        String defaultMainPack = "";
        String defaultBasePack = "base";
        
        if (mainConfig != null) {
            defaultMainPack = mainConfig.getString("default-world-resourcepack.main", "");
            defaultBasePack = mainConfig.getString("default-world-resourcepack.base", "base");
        }
        
        config.set("resourcepack.main", defaultMainPack);
        config.set("resourcepack.base", defaultBasePack);
        
        // 设置默认菜单材料配置
        String menuMaterial = "GRASS_BLOCK";
        if (worldName.equals(mainWorldName + "_nether")) {
            menuMaterial = "NETHERRACK";
        } else if (worldName.equals(mainWorldName + "_the_end")) {
            menuMaterial = "END_STONE";
        }
        
        config.set("menu_material.material", menuMaterial);
        config.set("menu_material.custom_model_data", 0);
        
        worldConfigs.put(worldName, config);
        saveWorldConfig(worldName, config);
        
        // 创建内存中的设置
        List<String> allowedPlayers = new ArrayList<>(); // 空列表表示所有玩家都可以进入
        Map<GameRule<?>, Object> gameRules = new HashMap<>();
        gameRules.put(GameRule.KEEP_INVENTORY, keepInventory);
        gameRules.put(GameRule.DO_DAYLIGHT_CYCLE, true);
        gameRules.put(GameRule.DO_MOB_SPAWNING, doMobSpawning);
        gameRules.put(GameRule.ANNOUNCE_ADVANCEMENTS, announceAdvancements);
        gameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, doImmediateRespawn);
        gameRules.put(GameRule.SPAWN_RADIUS, spawnRadius);
        
        WorldSettings newSettings = new WorldSettings(
            defaultX, defaultY, defaultZ, defaultYaw, defaultPitch, 
            GameMode.valueOf(gameMode), gameRules, true, allowedPlayers,
            defaultMainPack, defaultBasePack, menuMaterial, 0
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
            
            // 加载菜单材料配置
            String menuMaterial = config.getString("menu_material.material", "GRASS_BLOCK");
            int customModelData = config.getInt("menu_material.custom_model_data", 0);
            
            worldSettings.put(worldName, new WorldSettings(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, mainResourcePack, baseResourcePack, menuMaterial, customModelData));
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
        
        // 检查玩家是否是世界所有者
        if (isWorldOwner(worldName, playerName)) {
            return true;
        }
        
        // 检查玩家是否在允许列表中
        return settings.allowedPlayers.isEmpty() || settings.allowedPlayers.contains(playerName);
    }

    public static void setWorldLoadStatus(String worldName, boolean load) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, load, settings.allowedPlayers,
                settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
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
                    settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
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
                settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
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
        createWorldSettings(worldName, ownerName, worldType, 0, 64, 0, 0, 0);
    }
    
    public static void createWorldSettings(String worldName, String ownerName, String worldType, 
                                         double spawnX, double spawnY, double spawnZ, float yaw, float pitch) {
        if (!worldSettings.containsKey(worldName)) {
            // 获取主世界名称
            String mainWorldName = "world";
            if (!org.bukkit.Bukkit.getWorlds().isEmpty()) {
                mainWorldName = org.bukkit.Bukkit.getWorlds().get(0).getName();
            }
            
            List<String> allowedPlayers = new ArrayList<>();
            // 对于默认世界，允许所有玩家进入
            if (worldName.equals(mainWorldName) || worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
                // 默认世界不限制玩家
            } else {
                allowedPlayers.add(ownerName);
            }
            
            // 根据世界类型确定资源包
            String mainResourcePack = getResourcePackKeyByWorldType(worldType);
            String baseResourcePack = "base";
            
            // 根据世界类型设置默认菜单材料
            String defaultMenuMaterial = getDefaultMenuMaterialByWorldType(worldType);
            int defaultCustomModelData = 0;
            
            // 根据世界类型设置游戏模式
            GameMode gameMode = GameMode.CREATIVE;
            if (worldName.equals(mainWorldName) || worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
                gameMode = GameMode.SURVIVAL; // 默认世界使用生存模式
            }
            
            // 设置游戏规则
            Map<GameRule<?>, Object> gameRules = new HashMap<>();
            if (worldName.equals(mainWorldName)) {
                // 主世界
                gameRules.put(GameRule.KEEP_INVENTORY, mainConfig != null ? mainConfig.getBoolean("default-settings.keep-inventory", false) : false);
                gameRules.put(GameRule.SPAWN_RADIUS, 16);
                gameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, false);
            } else if (worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
                // 下界和末地
                gameRules.put(GameRule.KEEP_INVENTORY, true);
                gameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, true);
                gameRules.put(GameRule.SPAWN_RADIUS, 0);
            } else {
                // 自定义世界
                gameRules.put(GameRule.KEEP_INVENTORY, true);
                gameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, true);
                gameRules.put(GameRule.SPAWN_RADIUS, 0);
            }
            
            // 通用游戏规则
            gameRules.put(GameRule.DO_DAYLIGHT_CYCLE, true);
            gameRules.put(GameRule.DO_MOB_SPAWNING, worldName.equals(mainWorldName) ? true : false);
            gameRules.put(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            
            WorldSettings newSettings = new WorldSettings(
                spawnX, spawnY, spawnZ, yaw, pitch, gameMode, gameRules, true, allowedPlayers,
                mainResourcePack, baseResourcePack, defaultMenuMaterial, defaultCustomModelData
            );
            worldSettings.put(worldName, newSettings);
            
            // 创建新的配置文件
            File configFile = new File(worldConfigDir, worldName + ".yml");
            FileConfiguration config = new YamlConfiguration();
            
            // 设置配置内容
            config.set("spawnpoint.x", spawnX);
            config.set("spawnpoint.y", spawnY);
            config.set("spawnpoint.z", spawnZ);
            config.set("spawnpoint.yaw", yaw);
            config.set("spawnpoint.pitch", pitch);
            config.set("gamemode", gameMode.toString());
            config.set("load", true);
            
            // 设置玩家和所有者信息
            if (!allowedPlayers.isEmpty()) {
                config.set("players", String.join(",", allowedPlayers));
            } else {
                config.set("players", ""); // 空字符串表示所有玩家都可以进入
            }
            
            config.set("owner", ownerName);
            
            // 设置描述和创建时间
            if (worldName.equals(mainWorldName)) {
                config.set("description", "主世界");
            } else if (worldName.equals(mainWorldName + "_nether")) {
                config.set("description", "下界");
            } else if (worldName.equals(mainWorldName + "_the_end")) {
                config.set("description", "末地");
            } else {
                config.set("description", "由 " + ownerName + " 创建的世界");
            }
            
            config.set("created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // 设置游戏规则
            for (Map.Entry<GameRule<?>, Object> entry : gameRules.entrySet()) {
                config.set("gamerules." + entry.getKey().getName(), entry.getValue());
            }
            
            // 设置资源包配置
            config.set("resourcepack.main", mainResourcePack != null ? mainResourcePack : "");
            config.set("resourcepack.base", baseResourcePack);
            
            // 设置菜单材料配置
            config.set("menu_material.material", defaultMenuMaterial);
            config.set("menu_material.custom_model_data", defaultCustomModelData);
            
            worldConfigs.put(worldName, config);
            saveWorldConfig(worldName, config);
        }
    }

    /**
     * 根据世界类型获取对应的资源包缩写
     */
    private static String getResourcePackKeyByWorldType(String worldType) {
        if (worldType == null) return null;
        
        // 获取主世界名称
        String mainWorldName = "world";
        if (!org.bukkit.Bukkit.getWorlds().isEmpty()) {
            mainWorldName = org.bukkit.Bukkit.getWorlds().get(0).getName();
        }
        
        // 检查是否为默认世界（主世界、下界、末地）
        if (worldType.equals(mainWorldName) || worldType.equals(mainWorldName + "_nether") || worldType.equals(mainWorldName + "_the_end")) {
            return null; // 默认世界不设置特定资源包
        }
        
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
    
    /**
     * 根据世界类型获取对应的默认菜单材料
     */
    private static String getDefaultMenuMaterialByWorldType(String worldType) {
        if (worldType == null) return "GRASS_BLOCK";
        
        // 获取主世界名称
        String mainWorldName = "world";
        if (!org.bukkit.Bukkit.getWorlds().isEmpty()) {
            mainWorldName = org.bukkit.Bukkit.getWorlds().get(0).getName();
        }
        
        // 检查是否为默认世界（主世界、下界、末地）
        if (worldType.equals(mainWorldName)) {
            return "GRASS_BLOCK";
        } else if (worldType.equals(mainWorldName + "_nether")) {
            return "NETHERRACK";
        } else if (worldType.equals(mainWorldName + "_the_end")) {
            return "END_STONE";
        }
        
        switch (worldType.toLowerCase()) {
            case "normal":
                return "GRASS_BLOCK";
            case "flat":
                return "STONE";
            case "nether":
                return "NETHERRACK";
            case "end":
                return "END_STONE";
            case "ocean":
                return "WATER_BUCKET";
            case "desert":
                return "SAND";
            case "snow":
                return "SNOW_BLOCK";
            case "forest":
                return "OAK_SAPLING";
            default:
                return "GRASS_BLOCK"; // 默认材料
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
        
        // 加载主配置文件
        File mainConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (mainConfigFile.exists()) {
            mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);
        }
        
        // 等待服务器完成世界加载，确保能获取到正确的主世界名称
        // 注意：此时可能还没有完全加载所有世界，所以在MUTbuildUtils.java中会再次检查
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 确保默认世界配置文件存在
            createDefaultWorldConfigs();
            reloadConfig();
        }, 1L); // 延迟1tick执行，确保世界已加载
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
                resourcePackKey, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
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
                settings.mainResourcePack, resourcePackKey, settings.menuMaterial, settings.customModelData
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
    
    /**
     * 设置世界的菜单材料
     */
    public static void setWorldMenuMaterial(String worldName, String material, int customModelData) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers,
                settings.mainResourcePack, settings.baseResourcePack, material, customModelData
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("menu_material.material", material);
                config.set("menu_material.custom_model_data", customModelData);
                saveWorldConfig(worldName, config);
            }
        }
    }
    
    /**
     * 获取世界的菜单材料
     */
    public static String getWorldMenuMaterial(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null ? settings.menuMaterial : "GRASS_BLOCK";
    }
    
    /**
     * 获取世界的自定义模型数据
     */
    public static int getWorldCustomModelData(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        return settings != null ? settings.customModelData : 0;
    }

    /**
     * 更新世界的出生点位置
     */
    public static void updateSpawnLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                x, y, z, yaw, pitch,
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers,
                settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("spawnpoint.x", x);
                config.set("spawnpoint.y", y);
                config.set("spawnpoint.z", z);
                config.set("spawnpoint.yaw", yaw);
                config.set("spawnpoint.pitch", pitch);
                saveWorldConfig(worldName, config);
            }
        }
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
        private final String menuMaterial;
        private final int customModelData;

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<String> allowedPlayers) {
            this(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, null, null, "GRASS_BLOCK", 0);
        }

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<String> allowedPlayers,
                           String mainResourcePack, String baseResourcePack) {
            this(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, mainResourcePack, baseResourcePack, "GRASS_BLOCK", 0);
        }
        
        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<String> allowedPlayers,
                           String mainResourcePack, String baseResourcePack, String menuMaterial, int customModelData) {
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
            this.menuMaterial = menuMaterial;
            this.customModelData = customModelData;
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
        
        public String getMenuMaterial() {
            return menuMaterial;
        }
        
        public int getCustomModelData() {
            return customModelData;
        }
    }

    /**
     * 检查玩家是否为世界拥有者
     */
    public static boolean isWorldOwner(String worldName, String playerName) {
        FileConfiguration config = worldConfigs.get(worldName);
        if (config == null) {
            return false;
        }
        
        String owner = config.getString("owner", "");
        return owner.equals(playerName);
    }
}