package mut.buildup.mutbuildutils.listeners;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import mut.buildup.mutbuildutils.config.WorldConfig;

/**
 * 监听游戏规则变化并同步到配置文件
 */
public class GameRuleListener implements Listener {

    @EventHandler
    public void onGameRuleChange(WorldGameRuleChangeEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        GameRule<?> gameRule = event.getGameRule();
        String newValue = event.getValue();
        
        // 同步游戏规则变化到配置文件
        WorldConfig.updateGameRule(worldName, gameRule, newValue);
        
        // 输出日志信息
        System.out.println("[GameRuleListener] 世界 '" + worldName + "' 的游戏规则 '" + gameRule.getName() + "' 已更新为: " + newValue);
    }
}