package mut.buildup.mutbuildutils.world;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.GameRule;

import mut.buildup.mutbuildutils.MUTbuildUtils;
import mut.buildup.mutbuildutils.config.WorldConfig;
import mut.buildup.mutbuildutils.config.MenuConfig;
import mut.buildup.mutbuildutils.config.MenuConfig.WorldMenuItem;
import org.bukkit.Material;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WorldTemplateManager {

    private final File templateDir;
    private final File serverDir;
    private final MUTbuildUtils plugin;

    public WorldTemplateManager(MUTbuildUtils plugin) {
        this.plugin = plugin;
        this.templateDir = new File(plugin.getDataFolder(), "world_template");
        this.serverDir = new File(".");
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }
    }

    public MUTbuildUtils getPlugin() {
        return plugin;
    }

    public World createWorld(String templateName, String gameName, String playerName) throws IOException {
        // 检查模板是否存在
        File templateFile = new File(templateDir, templateName + ".zip");
        if (!templateFile.exists()) {
            throw new FileNotFoundException("找不到世界模板: " + templateName);
        }

        // 生成世界名称：模板名_游戏名_玩家名
        String worldName = String.format("%s_%s_%s", templateName, gameName, playerName);

        // 检查世界是否已存在
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            return existingWorld;
        }

        // 解压世界文件
        unzipWorld(templateFile, worldName);

        // 检查世界文件夹是否成功创建
        File worldFolder = new File(serverDir, worldName);
        if (!worldFolder.exists() || !new File(worldFolder, "level.dat").exists()) {
            throw new IOException("世界文件创建失败");
        }

        // 加载世界
        WorldCreator worldCreator = new WorldCreator(worldName);
        World world = worldCreator.createWorld();
        
        if (world != null) {
            // 应用世界配置中的游戏规则，如果没有配置则使用默认值
            WorldConfig.applyGameRules(world, worldName);
            
            // 创建世界配置（传递模板名称作为世界类型）
            WorldConfig.createWorldSettings(worldName, playerName, templateName);
            
            // 从菜单配置中获取对应的材料设置并应用到世界配置
            applyMenuMaterialToWorld(worldName, templateName);
            
            // 发送世界创建成功的反馈
            Bukkit.broadcast(net.kyori.adventure.text.Component.text("§a世界 §e" + worldName + " §a已成功创建！"), "mutbuildutils.world.create");
        } else {
            throw new IOException("世界加载失败");
        }

        return world;
    }
    
    /**
     * 从菜单配置中获取对应的材料设置并应用到世界配置
     */
    private void applyMenuMaterialToWorld(String worldName, String templateName) {
        // 遍历菜单配置，查找匹配的世界类型
        for (WorldMenuItem menuItem : MenuConfig.getMenuItems().values()) {
            if (menuItem.getWorldType().equals(templateName)) {
                // 获取材料和自定义模型数据
                Material material = menuItem.getItem().getType();
                int customModelData = 0;
                
                if (menuItem.getItem().hasItemMeta() && menuItem.getItem().getItemMeta().hasCustomModelData()) {
                    customModelData = menuItem.getItem().getItemMeta().getCustomModelData();
                }
                
                // 应用到世界配置
                WorldConfig.setWorldMenuMaterial(worldName, material.name(), customModelData);
                break;
            }
        }
    }

    private void unzipWorld(File zipFile, String worldName) throws IOException {
        File worldDir = new File(serverDir, worldName);
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(worldDir, entry.getName());

                // 创建目录
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }

                // 确保父目录存在
                new File(newFile.getParent()).mkdirs();

                // 写入文件
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}