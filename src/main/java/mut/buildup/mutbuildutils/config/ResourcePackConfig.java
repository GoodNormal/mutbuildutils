package mut.buildup.mutbuildutils.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ResourcePackConfig {
    private static File configFile;
    private static FileConfiguration config;
    private static final Map<String, ResourcePackInfo> resourcePacks = new HashMap<>();
    private static String baseResourcePackUrl;
    private static String baseResourcePackHash;
    private static String baseResourcePackUuid;
    
    public static void loadConfig(File dataFolder) {
        configFile = new File(dataFolder, "resourcepacks.yml");
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        reloadConfig();
    }
    
    private static void createDefaultConfig() {
        try {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
            
            // 设置基础资源包为空，等待用户配置
            defaultConfig.set("base_resource_pack.url", "");
            defaultConfig.set("base_resource_pack.hash", "");
            defaultConfig.set("base_resource_pack.uuid", "");
            
            // 创建空的主资源包配置节点
            defaultConfig.createSection("main_resource_packs");
            
            defaultConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadResourcePacks();
    }
    
    private static void loadResourcePacks() {
        resourcePacks.clear();
        
        // 调试信息：打印配置文件内容
        System.out.println("[ResourcePackConfig] 正在加载配置文件...");
        
        // 加载基础资源包
        baseResourcePackUrl = config.getString("base_resource_pack.url", "");
        baseResourcePackHash = config.getString("base_resource_pack.hash", "");
        baseResourcePackUuid = config.getString("base_resource_pack.uuid", "");
        
        System.out.println("[ResourcePackConfig] 基础资源包URL: " + baseResourcePackUrl);
        System.out.println("[ResourcePackConfig] 基础资源包Hash: " + baseResourcePackHash);
        System.out.println("[ResourcePackConfig] 基础资源包UUID: " + baseResourcePackUuid);
        
        // 如果基础资源包有URL但没有hash或UUID，自动生成
        if (!baseResourcePackUrl.isEmpty()) {
            boolean needSave = false;
            if (baseResourcePackHash.isEmpty()) {
                baseResourcePackHash = calculateHash(baseResourcePackUrl);
                config.set("base_resource_pack.hash", baseResourcePackHash);
                needSave = true;
            }
            if (baseResourcePackUuid.isEmpty()) {
                baseResourcePackUuid = UUID.randomUUID().toString();
                config.set("base_resource_pack.uuid", baseResourcePackUuid);
                needSave = true;
            }
            if (needSave) {
                saveConfig();
            }
        }
        
        // 加载主资源包
        System.out.println("[ResourcePackConfig] 主资源包配置节点: " + (config.getConfigurationSection("main_resource_packs") != null ? "存在" : "不存在"));
        if (config.getConfigurationSection("main_resource_packs") != null) {
            System.out.println("[ResourcePackConfig] 找到主资源包数量: " + config.getConfigurationSection("main_resource_packs").getKeys(false).size());
            for (String key : config.getConfigurationSection("main_resource_packs").getKeys(false)) {
                String url = config.getString("main_resource_packs." + key + ".url");
                String hash = config.getString("main_resource_packs." + key + ".hash", "");
                String uuid = config.getString("main_resource_packs." + key + ".uuid", "");
                
                System.out.println("[ResourcePackConfig] 加载资源包 " + key + ": URL=" + url + ", Hash=" + hash + ", UUID=" + uuid);
                
                if (url != null && !url.isEmpty()) {
                    boolean needSave = false;
                    // 如果没有hash，自动生成
                    if (hash.isEmpty()) {
                        hash = calculateHash(url);
                        config.set("main_resource_packs." + key + ".hash", hash);
                        needSave = true;
                    }
                    // 如果没有UUID，生成一个新的
                    if (uuid.isEmpty()) {
                        uuid = UUID.randomUUID().toString();
                        config.set("main_resource_packs." + key + ".uuid", uuid);
                        needSave = true;
                    }
                    if (needSave) {
                        saveConfig();
                    }
                    
                    resourcePacks.put(key, new ResourcePackInfo(url, hash, uuid));
                }
            }
        }
    }
    
    public static void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean addResourcePack(String shortName, String url) {
        try {
            config.set("main_resource_packs." + shortName + ".url", url);
            config.set("main_resource_packs." + shortName + ".hash", "");
            config.set("main_resource_packs." + shortName + ".uuid", "");
            saveConfig();
            // 重新加载配置以自动生成hash和uuid
            reloadConfig();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteResourcePack(String shortName) {
        try {
            if (resourcePacks.containsKey(shortName)) {
                config.set("main_resource_packs." + shortName, null);
                saveConfig();
                resourcePacks.remove(shortName);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean refreshHashes() {
        try {
            // 重新加载配置以确保获取最新的URL
            reloadConfig();
            
            // 刷新基础资源包hash和UUID
            if (!baseResourcePackUrl.isEmpty()) {
                String newHash = calculateHash(baseResourcePackUrl);
                String newUuid = UUID.randomUUID().toString();
                config.set("base_resource_pack.hash", newHash);
                config.set("base_resource_pack.uuid", newUuid);
                baseResourcePackHash = newHash;
                baseResourcePackUuid = newUuid;
            }
            
            // 刷新主资源包hash和UUID
            for (Map.Entry<String, ResourcePackInfo> entry : resourcePacks.entrySet()) {
                String shortName = entry.getKey();
                ResourcePackInfo info = entry.getValue();
                String newHash = calculateHash(info.getUrl());
                String newUuid = UUID.randomUUID().toString();
                config.set("main_resource_packs." + shortName + ".hash", newHash);
                config.set("main_resource_packs." + shortName + ".uuid", newUuid);
                info.setHash(newHash);
                info.setUuid(newUuid);
            }
            
            saveConfig();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static String calculateHash(String urlString) {
        try {
            URL url = new URL(urlString);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            
            try (InputStream is = url.openStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hash = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("计算资源包哈希值失败: " + urlString + " - " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    public static ResourcePackInfo getResourcePack(String shortName) {
        return resourcePacks.get(shortName);
    }
    
    public static Map<String, ResourcePackInfo> getAllResourcePacks() {
        return new HashMap<>(resourcePacks);
    }
    
    public static Set<String> getResourcePackNames() {
        return resourcePacks.keySet();
    }
    
    public static String getBaseResourcePackUrl() {
        return baseResourcePackUrl;
    }
    
    public static String getBaseResourcePackHash() {
        return baseResourcePackHash;
    }
    
    public static String getBaseResourcePackUuid() {
        return baseResourcePackUuid;
    }
    
    public static class ResourcePackInfo {
        private String url;
        private String hash;
        private String uuid;
        
        public ResourcePackInfo(String url, String hash, String uuid) {
            this.url = url;
            this.hash = hash;
            this.uuid = uuid;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getHash() {
            return hash;
        }
        
        public String getUuid() {
            return uuid;
        }
        
        public void setHash(String hash) {
            this.hash = hash;
        }
        
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
}