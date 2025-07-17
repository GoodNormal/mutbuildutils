# MUTbuildUtils Plugin

一个用于创建和管理世界模板的Minecraft插件。

## 功能特性

- **世界模板系统**: 支持从ZIP文件创建世界
- **邀请系统**: 玩家可以邀请其他玩家进入私人世界
- **权限管理**: 细粒度的权限控制
- **GUI菜单**: 直观的世界创建界面
- **自动加载**: 服务器启动时自动加载配置的世界

## 命令

### /worldcreate (别名: /wc)
打开世界创建菜单
- 权限: `mutbuildutils.worldcreate`

### /world (别名: /w)
世界管理命令
- `/world tp <世界名>` - 传送到指定世界
- `/world invite <玩家> <世界> <审核OP>` - 邀请玩家进入世界
- `/world approve <邀请者>` - 同意邀请申请
- `/world deny <邀请者>` - 拒绝邀请申请
- `/world load <世界名>` - 加载世界
- `/world unload <世界名>` - 卸载世界

### /mutbuild (别名: /mb)
插件管理命令
- `/mutbuild reload` - 重载插件配置
- 权限: `mutbuildutils.admin`

## 配置文件

### config.yml
主配置文件，包含默认世界设置、加载设置、玩家权限等。

### menu.yml
世界创建菜单配置，定义菜单大小和世界模板选项。

## 世界模板

将世界模板ZIP文件放置在 `plugins/MUTbuildUtils/world_template/` 目录中。

模板文件命名格式: `<模板名>.zip`

创建的世界命名格式: `<模板名>_<游戏名>_<玩家名>`

## 权限节点

- `mutbuildutils.worldcreate` - 允许创建新世界
- `mutbuildutils.world` - 允许使用世界传送命令
- `mutbuildutils.world.invite` - 允许邀请玩家进入世界
- `mutbuildutils.world.admin` - 允许管理世界邀请
- `mutbuildutils.world.load` - 允许加载世界
- `mutbuildutils.world.unload` - 允许卸载世界
- `mutbuildutils.admin` - 插件管理权限
- `mutbuildutils.world.create` - 允许接收世界创建成功的广播消息

## 安装说明

1. 将插件JAR文件放入服务器的 `plugins` 目录
2. 重启服务器或使用 `/reload` 命令
3. 在 `plugins/MUTbuildUtils/world_template/` 目录中放置世界模板ZIP文件
4. 根据需要修改配置文件

## 开发信息

- 作者: good_normal
- 版本: 1.0-SNAPSHOT
- API版本: 1.21
- 网站: www.mutmc.live

## 注意事项

- 确保服务器有足够的磁盘空间来存储世界文件
- 世界模板ZIP文件应包含完整的世界数据（level.dat等）
- 邀请系统需要OP在线进行审核
- 建议定期备份世界数据