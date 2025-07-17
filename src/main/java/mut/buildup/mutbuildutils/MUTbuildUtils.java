package mut.buildup.mutbuildutils;

import mut.buildup.mutbuildutils.commands.WorldCommand;
import mut.buildup.mutbuildutils.commands.WorldCreateCommand;
import mut.buildup.mutbuildutils.commands.ReloadCommand;
import mut.buildup.mutbuildutils.commands.WorldResourcePackCommand;
import mut.buildup.mutbuildutils.commands.ResourcePackTestCommand;
import mut.buildup.mutbuildutils.config.WorldConfig;
import mut.buildup.mutbuildutils.config.MenuConfig;
import mut.buildup.mutbuildutils.config.ResourcePackConfig;
import mut.buildup.mutbuildutils.listeners.WorldAccessListener;
import mut.buildup.mutbuildutils.listeners.MenuListener;
import mut.buildup.mutbuildutils.listeners.PlayerMenuListener;
import mut.buildup.mutbuildutils.menu.OwnWorldMenuListener;
import mut.buildup.mutbuildutils.menu.WorldPlayerMenuListener;
import mut.buildup.mutbuildutils.world.WorldTemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class MUTbuildUtils extends JavaPlugin {

    private static MUTbuildUtils instance;
    private WorldTemplateManager worldTemplateManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 创建配置文件
        saveDefaultConfig();
        
        // 加载世界配置
        WorldConfig.loadConfig(this);
        
        // 加载菜单配置
        File menuConfigFile = new File(getDataFolder(), "menu.yml");
        if (!menuConfigFile.exists()) {
            saveResource("menu.yml", false);
        }
        MenuConfig.loadConfig(menuConfigFile);
        
        // 加载资源包配置
        ResourcePackConfig.loadConfig(getDataFolder());
        
        // 初始化世界模板管理器
        worldTemplateManager = new WorldTemplateManager(this);
        
        // 注册命令
        getCommand("worldcreate").setExecutor(new WorldCreateCommand());
        
        WorldCommand worldCommand = new WorldCommand();
        getCommand("world").setExecutor(worldCommand);
        getCommand("world").setTabCompleter(worldCommand);
        
        ReloadCommand reloadCommand = new ReloadCommand();
        getCommand("mutbuild").setExecutor(reloadCommand);
        getCommand("mutbuild").setTabCompleter(reloadCommand);
        
        WorldResourcePackCommand resourcePackCommand = new WorldResourcePackCommand();
        getCommand("worldresourcepack").setExecutor(resourcePackCommand);
        getCommand("worldresourcepack").setTabCompleter(resourcePackCommand);
        
        ResourcePackTestCommand resourcePackTestCommand = new ResourcePackTestCommand();
        getCommand("resourcepacktest").setExecutor(resourcePackTestCommand);
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new WorldAccessListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(worldTemplateManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMenuListener(), this);
        getServer().getPluginManager().registerEvents(new OwnWorldMenuListener(), this);
        getServer().getPluginManager().registerEvents(new WorldPlayerMenuListener(), this);
        getServer().getPluginManager().registerEvents(new mut.buildup.mutbuildutils.listener.ResourcePackListener(), this);
        
        // 延迟自动加载配置中标记的世界
        Bukkit.getScheduler().runTaskLater(this, this::loadConfiguredWorlds, 20L); // 延迟1秒加载
        
        getLogger().info("MUTbuildUtils 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("MUTbuildUtils 插件已禁用！");
        instance = null;
    }
    
    /**
     * 获取插件实例
     */
    public static MUTbuildUtils getInstance() {
        return instance;
    }
    
    public WorldTemplateManager getWorldTemplateManager() {
        return worldTemplateManager;
    }

    private void loadConfiguredWorlds() {
        List<String> worldsToLoad = WorldConfig.getWorldsToLoad();
        getLogger().info("检测到 " + worldsToLoad.size() + " 个需要自动加载的世界: " + worldsToLoad);
        
        for (String worldName : worldsToLoad) {
            if (Bukkit.getWorld(worldName) == null) {
                try {
                    getLogger().info("正在加载世界: " + worldName);
                    World world = Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));
                    if (world != null) {
                        // 应用游戏规则
                        WorldConfig.applyGameRules(world, worldName);
                        getLogger().info("已成功加载世界: " + worldName);
                    } else {
                        getLogger().warning("世界创建失败: " + worldName);
                    }
                } catch (Exception e) {
                    getLogger().warning("无法加载世界 " + worldName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                getLogger().info("世界 " + worldName + " 已经加载，跳过");
            }
        }
    }
}
