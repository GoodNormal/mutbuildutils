# MUTbuildUtils - 世界配置功能使用说明

## 配置系统重构说明

### 新的配置结构
现在每个世界都有独立的配置文件，存储在 `world/config/` 目录中：
1. **独立配置文件**：每个世界的配置存储在 `world/config/<世界名>.yml` 文件中
2. **自动创建配置**：创建新世界时会自动生成对应的配置文件
3. **分离存储**：世界配置不再与主 `config.yml` 混合
4. **更好的管理**：每个世界的配置独立管理，便于维护

### 重构内容
1. ✅ 创建了独立的 `world/config/` 目录
2. ✅ 每个世界都有独立的 `.yml` 配置文件
3. ✅ 自动创建世界配置功能
4. ✅ 世界访问控制功能完全实现
5. ✅ 支持默认世界跳过权限检查

## 如何使用世界配置功能

### 1. 配置文件结构
每个世界都有独立的配置文件，位于 `world/config/<世界名>.yml`：

```yaml
# world/config/example_world.yml
spawnpoint:      # 出生点坐标
  x: 0
  y: 64
  z: 0
  yaw: 0         # 视角方向
  pitch: 0       # 视角俯仰
gamemode: CREATIVE # 游戏模式 (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)
load: false        # 是否在服务器启动时自动加载
allowed_players:   # 允许进入的玩家列表
  - player1
  - player2
owner: admin       # 世界所有者
description: "示例世界"  # 世界描述
created_time: "2024-01-01 12:00:00"  # 创建时间
gamerules:         # 游戏规则设置
  keepInventory: true
  doDaylightCycle: false
  doMobSpawning: false
  announceAdvancements: false
  doImmediateRespawn: true
  spawnRadius: 0
```

### 2. 功能说明

#### 自动配置文件创建
- 使用 `/world load <世界名>` 加载世界时，如果配置文件不存在会自动创建
- 使用 `/worldcreate` 创建世界时会自动生成对应的配置文件
- 服务器启动时自动加载的世界也会自动创建配置文件（如果不存在）
- 配置文件包含默认设置：出生点、游戏模式、所有者信息等

#### 自动加载世界
- 设置 `load: true` 的世界会在服务器启动时自动加载
- 自动应用配置的游戏规则和设置

#### 玩家权限控制
- `allowed_players` 字段控制哪些玩家可以进入该世界
- 空列表表示所有玩家都可以进入
- 默认世界（主世界、下界、末地）跳过权限检查
- 管理员和OP玩家可以进入任何世界

#### 出生点设置
- 玩家进入世界时会传送到配置的出生点
- 包含坐标和视角方向

#### 游戏规则
- 支持所有Minecraft游戏规则
- 布尔类型和整数类型规则都支持

### 3. 相关命令

```
/mutbuild reload     # 重新加载所有配置文件
/world <世界名>      # 传送到指定世界
/worldcreate         # 创建新世界（会自动添加配置）
```

### 4. 使用示例

#### 创建一个创造模式世界
创建文件：`world/config/creative_world.yml`
```yaml
spawnpoint:
  x: 0
  y: 100
  z: 0
  yaw: 0
  pitch: 0
gamemode: CREATIVE
load: true
allowed_players:
  - admin
  - builder1
  - builder2
owner: admin
description: "创造模式建筑世界"
created_time: "2024-01-01 12:00:00"
gamerules:
  keepInventory: true
  doDaylightCycle: false
  doWeatherCycle: false
  doMobSpawning: false
  announceAdvancements: false
  doImmediateRespawn: true
  spawnRadius: 0
```

#### 创建一个生存模式世界
创建文件：`world/config/survival_world.yml`
```yaml
spawnpoint:
  x: 0
  y: 64
  z: 0
  yaw: 0
  pitch: 0
gamemode: SURVIVAL
load: false
allowed_players: []  # 空列表表示所有玩家都可以进入
owner: admin
description: "生存模式世界"
created_time: "2024-01-01 12:00:00"
gamerules:
  keepInventory: false
  doDaylightCycle: true
  doMobSpawning: true
  announceAdvancements: true
  doImmediateRespawn: false
  spawnRadius: 10
```

### 5. 注意事项

1. **独立配置文件**：每个世界的配置文件位于 `world/config/<世界名>.yml`
2. **自动创建配置**：使用 `/worldcreate` 创建世界时会自动生成配置文件
3. **配置修改后需要重载**：修改配置文件后使用 `/mutbuild reload` 重新加载
4. **世界名称要准确**：配置文件名必须与实际世界文件夹名称一致
5. **权限检查**：确保玩家有相应的权限才能使用命令
6. **备份配置**：修改配置前建议备份原文件
7. **默认世界**：主世界、下界、末地不受访问控制限制

### 6. 故障排除

- **配置不生效**：检查YAML格式是否正确，使用 `/mutbuild reload` 重新加载
- **世界无法加载**：检查世界文件是否存在，权限是否正确
- **玩家无法进入**：检查 `allowed_players` 字段配置和玩家权限
- **配置文件丢失**：检查 `world/config/` 目录是否存在，重新创建世界会自动生成配置

现在 MUTbuildUtils 插件的世界配置功能已经完全重构并可以正常使用了！

### 7. 新功能特性

- ✅ **独立配置存储**：每个世界都有独立的配置文件
- ✅ **智能配置创建**：在以下情况下自动生成配置文件：
  - 使用 `/world load` 命令加载世界时
  - 使用 `/worldcreate` 创建新世界时
  - 服务器启动时自动加载配置中的世界时
- ✅ **完整访问控制**：实现了世界进入权限检查
- ✅ **默认世界跳过**：主世界等默认世界不受访问限制
- ✅ **管理员权限**：OP和管理员可以进入任何世界
- ✅ **详细配置信息**：包含所有者、描述、创建时间等信息
- ✅ **配置文件检测**：自动检测世界是否已有配置文件，避免重复创建