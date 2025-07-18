package mut.buildup.mutbuildutils.config;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.UUID;

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
        boolean doDaylightCycle = true;
        boolean doWeatherCycle = true;
        boolean doFireTick = true;
        boolean mobGriefing = true;
        boolean naturalRegeneration = true;
        boolean showDeathMessages = true;
        boolean commandBlockOutput = true;
        boolean logAdminCommands = true;
        boolean sendCommandFeedback = true;
        boolean reducedDebugInfo = false;
        int spawnRadius = 10;
        int randomTickSpeed = 3;
        int maxEntityCramming = 24;
        int maxCommandChainLength = 65536;
        
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
        config.set("gamerules.doDaylightCycle", doDaylightCycle);
        config.set("gamerules.doWeatherCycle", doWeatherCycle);
        config.set("gamerules.doMobSpawning", doMobSpawning);
        config.set("gamerules.announceAdvancements", announceAdvancements);
        config.set("gamerules.doImmediateRespawn", doImmediateRespawn);
        config.set("gamerules.doFireTick", doFireTick);
        config.set("gamerules.mobGriefing", mobGriefing);
        config.set("gamerules.naturalRegeneration", naturalRegeneration);
        config.set("gamerules.showDeathMessages", showDeathMessages);
        config.set("gamerules.commandBlockOutput", commandBlockOutput);
        config.set("gamerules.logAdminCommands", logAdminCommands);
        config.set("gamerules.sendCommandFeedback", sendCommandFeedback);
        config.set("gamerules.reducedDebugInfo", reducedDebugInfo);
        config.set("gamerules.spawnRadius", spawnRadius);
        config.set("gamerules.randomTickSpeed", randomTickSpeed);
        config.set("gamerules.maxEntityCramming", maxEntityCramming);
        config.set("gamerules.maxCommandChainLength", maxCommandChainLength);
        
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
        List<PlayerInfo> allowedPlayers = new ArrayList<>(); // 空列表表示所有玩家都可以进入
        Map<GameRule<?>, Object> gameRules = new HashMap<>();
        gameRules.put(GameRule.KEEP_INVENTORY, keepInventory);
        gameRules.put(GameRule.DO_DAYLIGHT_CYCLE, doDaylightCycle);
        gameRules.put(GameRule.DO_WEATHER_CYCLE, doWeatherCycle);
        gameRules.put(GameRule.DO_MOB_SPAWNING, doMobSpawning);
        gameRules.put(GameRule.ANNOUNCE_ADVANCEMENTS, announceAdvancements);
        gameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, doImmediateRespawn);
        gameRules.put(GameRule.DO_FIRE_TICK, doFireTick);
        gameRules.put(GameRule.MOB_GRIEFING, mobGriefing);
        gameRules.put(GameRule.NATURAL_REGENERATION, naturalRegeneration);
        gameRules.put(GameRule.SHOW_DEATH_MESSAGES, showDeathMessages);
        gameRules.put(GameRule.COMMAND_BLOCK_OUTPUT, commandBlockOutput);
        gameRules.put(GameRule.LOG_ADMIN_COMMANDS, logAdminCommands);
        gameRules.put(GameRule.SEND_COMMAND_FEEDBACK, sendCommandFeedback);
        gameRules.put(GameRule.REDUCED_DEBUG_INFO, reducedDebugInfo);
        gameRules.put(GameRule.SPAWN_RADIUS, spawnRadius);
        gameRules.put(GameRule.RANDOM_TICK_SPEED, randomTickSpeed);
        gameRules.put(GameRule.MAX_ENTITY_CRAMMING, maxEntityCramming);
        gameRules.put(GameRule.MAX_COMMAND_CHAIN_LENGTH, maxCommandChainLength);
        
        WorldSettings newSettings = new WorldSettings(
            defaultX, defaultY, defaultZ, defaultYaw, defaultPitch, 
            GameMode.valueOf(gameMode), gameRules, true, allowedPlayers, null,
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
            List<PlayerInfo> allowedPlayers = loadPlayersFromConfig(playersString);
            
            // 加载所有者信息
            String ownerString = config.getString("owner", "");
            PlayerInfo owner = loadOwnerFromConfig(ownerString);

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
            
            worldSettings.put(worldName, new WorldSettings(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, owner, mainResourcePack, baseResourcePack, menuMaterial, customModelData));
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
        if (settings.allowedPlayers.isEmpty()) {
            return true;
        }
        
        UUID playerUuid = getPlayerUUID(playerName);
        return settings.allowedPlayers.stream()
                .anyMatch(playerInfo -> playerInfo.getUuid().equals(playerUuid) || playerInfo.getName().equals(playerName));
    }

    public static void setWorldLoadStatus(String worldName, boolean load) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, load, settings.allowedPlayers, settings.owner,
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
        if (settings != null) {
            UUID playerUuid = getPlayerUUID(playerName);
            PlayerInfo newPlayerInfo = new PlayerInfo(playerUuid, playerName);
            
            // 检查玩家是否已在列表中
            boolean playerExists = settings.allowedPlayers.stream()
                    .anyMatch(playerInfo -> playerInfo.getUuid().equals(playerUuid));
            
            if (!playerExists) {
                List<PlayerInfo> newPlayers = new ArrayList<>(settings.allowedPlayers);
                newPlayers.add(newPlayerInfo);
                
                // 更新内存中的设置
                WorldSettings newSettings = new WorldSettings(
                    settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                    settings.gamemode, settings.gameRules, settings.load, newPlayers, settings.owner,
                    settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
                );
                worldSettings.put(worldName, newSettings);
                
                // 更新配置文件
                savePlayerInfoToConfig(worldName, newSettings);
            }
        }
    }

    public static void removePlayerFromWorld(String worldName, String playerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            UUID playerUuid = getPlayerUUID(playerName);
            
            // 检查玩家是否在列表中
            boolean playerExists = settings.allowedPlayers.stream()
                    .anyMatch(playerInfo -> playerInfo.getUuid().equals(playerUuid) || playerInfo.getName().equals(playerName));
            
            if (playerExists) {
                List<PlayerInfo> newPlayers = new ArrayList<>(settings.allowedPlayers);
                newPlayers.removeIf(playerInfo -> playerInfo.getUuid().equals(playerUuid) || playerInfo.getName().equals(playerName));
                
                // 更新内存中的设置
                WorldSettings newSettings = new WorldSettings(
                    settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                    settings.gamemode, settings.gameRules, settings.load, newPlayers, settings.owner,
                    settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
                );
                worldSettings.put(worldName, newSettings);
                
                // 更新配置文件
                savePlayerInfoToConfig(worldName, newSettings);
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
            
            List<PlayerInfo> allowedPlayers = new ArrayList<>();
            PlayerInfo owner = null;
            
            // 对于默认世界，允许所有玩家进入
            if (worldName.equals(mainWorldName) || worldName.equals(mainWorldName + "_nether") || worldName.equals(mainWorldName + "_the_end")) {
                // 默认世界不限制玩家，但仍需设置所有者
                if (ownerName != null && !ownerName.isEmpty()) {
                    UUID ownerUuid = getPlayerUUID(ownerName);
                    owner = new PlayerInfo(ownerUuid, ownerName);
                }
            } else {
                UUID ownerUuid = getPlayerUUID(ownerName);
                owner = new PlayerInfo(ownerUuid, ownerName);
                allowedPlayers.add(owner);
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
            gameRules.put(GameRule.DO_WEATHER_CYCLE, true);
            gameRules.put(GameRule.DO_MOB_SPAWNING, worldName.equals(mainWorldName) ? true : false);
            gameRules.put(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            gameRules.put(GameRule.DO_FIRE_TICK, true);
            gameRules.put(GameRule.MOB_GRIEFING, worldName.equals(mainWorldName) ? true : false);
            gameRules.put(GameRule.NATURAL_REGENERATION, true);
            gameRules.put(GameRule.SHOW_DEATH_MESSAGES, true);
            gameRules.put(GameRule.COMMAND_BLOCK_OUTPUT, true);
            gameRules.put(GameRule.LOG_ADMIN_COMMANDS, true);
            gameRules.put(GameRule.SEND_COMMAND_FEEDBACK, true);
            gameRules.put(GameRule.REDUCED_DEBUG_INFO, false);
            gameRules.put(GameRule.RANDOM_TICK_SPEED, 3);
            gameRules.put(GameRule.MAX_ENTITY_CRAMMING, 24);
            gameRules.put(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
            
            WorldSettings newSettings = new WorldSettings(
                spawnX, spawnY, spawnZ, yaw, pitch, gameMode, gameRules, true, allowedPlayers, owner,
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
            
            worldConfigs.put(worldName, config);
            
            // 使用新方法保存玩家和所有者信息
            savePlayerInfoToConfig(worldName, newSettings);
            
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
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.NATURAL_REGENERATION, true);
            world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
            world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, true);
            world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);
            world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
            world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 24);
            world.setGameRule(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
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
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers, settings.owner,
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
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers, settings.owner,
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
     * 设置世界的所有者
     */
    public static void setWorldOwner(String worldName, String ownerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings != null) {
            UUID ownerUuid = getPlayerUUID(ownerName);
            PlayerInfo newOwner = new PlayerInfo(ownerUuid, ownerName);
            
            // 更新内存中的设置
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers, newOwner,
                settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            FileConfiguration config = worldConfigs.get(worldName);
            if (config != null) {
                config.set("owner", ownerUuid.toString() + ":" + ownerName);
                saveWorldConfig(worldName, config);
            }
        }
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
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers, settings.owner,
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
     * 更新世界的游戏规则并同步到配置文件
     */
    public static void updateGameRule(String worldName, GameRule<?> gameRule, String newValue) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings == null) {
            return;
        }
        
        // 解析新值
        Object parsedValue;
        try {
            if (gameRule.getType() == Boolean.class) {
                parsedValue = Boolean.parseBoolean(newValue);
            } else if (gameRule.getType() == Integer.class) {
                parsedValue = Integer.parseInt(newValue);
            } else {
                parsedValue = newValue;
            }
        } catch (NumberFormatException e) {
            System.err.println("无法解析游戏规则值: " + newValue + " for rule " + gameRule.getName());
            return;
        }
        
        // 更新内存中的游戏规则
        Map<GameRule<?>, Object> newGameRules = new HashMap<>(settings.gameRules);
        newGameRules.put(gameRule, parsedValue);
        
        // 创建新的设置对象
        WorldSettings newSettings = new WorldSettings(
            settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
            settings.gamemode, newGameRules, settings.load, settings.allowedPlayers, settings.owner,
            settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
        );
        worldSettings.put(worldName, newSettings);
        
        // 更新配置文件
        FileConfiguration config = worldConfigs.get(worldName);
        if (config != null) {
            config.set("gamerules." + gameRule.getName(), parsedValue);
            saveWorldConfig(worldName, config);
        }
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
                settings.gamemode, settings.gameRules, settings.load, settings.allowedPlayers, settings.owner,
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

    /**
     * 玩家信息类，包含UUID和名称
     */
    public static class PlayerInfo {
        private final UUID uuid;
        private String name;
        
        public PlayerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PlayerInfo that = (PlayerInfo) obj;
            return Objects.equals(uuid, that.uuid);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(uuid);
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
        private final List<PlayerInfo> allowedPlayers;
        private final PlayerInfo owner;
        private final String mainResourcePack;
        private final String baseResourcePack;
        private final String menuMaterial;
        private final int customModelData;

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<PlayerInfo> allowedPlayers, PlayerInfo owner) {
            this(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, owner, null, null, "GRASS_BLOCK", 0);
        }

        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<PlayerInfo> allowedPlayers, PlayerInfo owner,
                           String mainResourcePack, String baseResourcePack) {
            this(x, y, z, yaw, pitch, gamemode, gameRules, load, allowedPlayers, owner, mainResourcePack, baseResourcePack, "GRASS_BLOCK", 0);
        }
        
        public WorldSettings(double x, double y, double z, float yaw, float pitch, GameMode gamemode, 
                           Map<GameRule<?>, Object> gameRules, boolean load, List<PlayerInfo> allowedPlayers, PlayerInfo owner,
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
            this.owner = owner;
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

        public List<PlayerInfo> getAllowedPlayers() {
            return new ArrayList<>(allowedPlayers);
        }
        
        public List<String> getPlayers() {
            return allowedPlayers.stream()
                    .map(PlayerInfo::getName)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        public PlayerInfo getOwner() {
            return owner;
        }
        
        public String getOwnerName() {
            return owner != null ? owner.getName() : "";
        }
        
        public UUID getOwnerUuid() {
            return owner != null ? owner.getUuid() : null;
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
     * 根据玩家名获取UUID
     */
    private static UUID getPlayerUUID(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return player.getUniqueId();
        }
        // 如果玩家不在线，尝试从离线玩家获取
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }
    
    /**
     * 根据UUID获取玩家名
     */
    private static String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        // 如果玩家不在线，从离线玩家获取
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
    
    /**
     * 更新玩家名称（如果UUID对应的玩家名发生了变化）
     */
    public static void updatePlayerNames(String worldName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings == null) return;
        
        boolean updated = false;
        List<PlayerInfo> updatedPlayers = new ArrayList<>();
        
        // 更新允许玩家列表中的名称
        for (PlayerInfo playerInfo : settings.getAllowedPlayers()) {
            String currentName = getPlayerName(playerInfo.getUuid());
            if (currentName != null && !currentName.equals(playerInfo.getName())) {
                playerInfo.setName(currentName);
                updated = true;
            }
            updatedPlayers.add(playerInfo);
        }
        
        // 更新所有者名称
        PlayerInfo updatedOwner = settings.getOwner();
        if (updatedOwner != null) {
            String currentOwnerName = getPlayerName(updatedOwner.getUuid());
            if (currentOwnerName != null && !currentOwnerName.equals(updatedOwner.getName())) {
                updatedOwner.setName(currentOwnerName);
                updated = true;
            }
        }
        
        if (updated) {
            // 创建新的设置对象
            WorldSettings newSettings = new WorldSettings(
                settings.x, settings.y, settings.z, settings.yaw, settings.pitch,
                settings.gamemode, settings.gameRules, settings.load, updatedPlayers, updatedOwner,
                settings.mainResourcePack, settings.baseResourcePack, settings.menuMaterial, settings.customModelData
            );
            worldSettings.put(worldName, newSettings);
            
            // 更新配置文件
            savePlayerInfoToConfig(worldName, newSettings);
        }
    }
    
    /**
     * 将玩家信息保存到配置文件
     */
    private static void savePlayerInfoToConfig(String worldName, WorldSettings settings) {
        FileConfiguration config = worldConfigs.get(worldName);
        if (config == null) return;
        
        // 保存玩家列表（格式：uuid:name,uuid:name）
        List<String> playerEntries = new ArrayList<>();
        for (PlayerInfo playerInfo : settings.getAllowedPlayers()) {
            playerEntries.add(playerInfo.getUuid().toString() + ":" + playerInfo.getName());
        }
        config.set("players", String.join(",", playerEntries));
        
        // 保存所有者信息（格式：uuid:name）
        if (settings.getOwner() != null) {
            config.set("owner", settings.getOwner().getUuid().toString() + ":" + settings.getOwner().getName());
        }
        
        saveWorldConfig(worldName, config);
    }
    
    /**
     * 从配置文件加载玩家信息
     */
    private static List<PlayerInfo> loadPlayersFromConfig(String playersString) {
        List<PlayerInfo> players = new ArrayList<>();
        if (playersString == null || playersString.trim().isEmpty()) {
            return players;
        }
        
        String[] playerEntries = playersString.split(",");
        for (String entry : playerEntries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            
            if (entry.contains(":")) {
                // 新格式：uuid:name
                String[] parts = entry.split(":", 2);
                try {
                    UUID uuid = UUID.fromString(parts[0]);
                    String name = parts[1];
                    players.add(new PlayerInfo(uuid, name));
                } catch (IllegalArgumentException e) {
                    System.err.println("无效的UUID格式: " + parts[0]);
                }
            } else {
                // 旧格式：仅玩家名，需要转换为UUID
                try {
                    UUID uuid = getPlayerUUID(entry);
                    players.add(new PlayerInfo(uuid, entry));
                } catch (Exception e) {
                    System.err.println("无法获取玩家 " + entry + " 的UUID: " + e.getMessage());
                }
            }
        }
        
        return players;
    }
    
    /**
     * 从配置文件加载所有者信息
     */
    private static PlayerInfo loadOwnerFromConfig(String ownerString) {
        if (ownerString == null || ownerString.trim().isEmpty()) {
            return null;
        }
        
        if (ownerString.contains(":")) {
            // 新格式：uuid:name
            String[] parts = ownerString.split(":", 2);
            try {
                UUID uuid = UUID.fromString(parts[0]);
                String name = parts[1];
                return new PlayerInfo(uuid, name);
            } catch (IllegalArgumentException e) {
                System.err.println("无效的UUID格式: " + parts[0]);
                return null;
            }
        } else {
            // 旧格式：仅玩家名，需要转换为UUID
            try {
                UUID uuid = getPlayerUUID(ownerString);
                return new PlayerInfo(uuid, ownerString);
            } catch (Exception e) {
                System.err.println("无法获取所有者 " + ownerString + " 的UUID: " + e.getMessage());
                return null;
            }
        }
    }
    
    /**
     * 检查玩家是否为世界拥有者
     */
    public static boolean isWorldOwner(String worldName, String playerName) {
        WorldSettings settings = worldSettings.get(worldName);
        if (settings == null || settings.getOwner() == null) {
            return false;
        }
        
        // 首先检查UUID
        UUID playerUuid = getPlayerUUID(playerName);
        if (settings.getOwner().getUuid().equals(playerUuid)) {
            return true;
        }
        
        // 备用检查：比较名称
        return settings.getOwner().getName().equals(playerName);
    }

    public static boolean isWorldConfigExists(String worldName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isWorldConfigExists'");
    }
}