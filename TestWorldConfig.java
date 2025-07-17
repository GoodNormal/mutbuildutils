import mut.buildup.mutbuildutils.config.WorldConfig;
import java.io.File;
import java.util.List;

public class TestWorldConfig {
    public static void main(String[] args) {
        try {
            // 创建测试目录结构
            File testDir = new File("test_plugin_data");
            File worldConfigDir = new File(testDir, "world/config");
            worldConfigDir.mkdirs();
            
            // 复制测试配置文件
            File testConfig = new File("test_world_config.yml");
            File targetConfig = new File(worldConfigDir, "testworld.yml");
            
            if (testConfig.exists()) {
                java.nio.file.Files.copy(testConfig.toPath(), targetConfig.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("已复制测试配置文件到: " + targetConfig.getAbsolutePath());
            }
            
            // 模拟插件加载配置
            WorldConfig.loadConfig(testDir);
            
            // 测试获取需要加载的世界
            List<String> worldsToLoad = WorldConfig.getWorldsToLoad();
            System.out.println("需要加载的世界数量: " + worldsToLoad.size());
            System.out.println("需要加载的世界列表: " + worldsToLoad);
            
            // 测试特定世界的加载状态
            boolean shouldLoad = WorldConfig.shouldLoadOnStartup("testworld");
            System.out.println("testworld 是否应该加载: " + shouldLoad);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}