# MUTbuildUtils - 世界资源包功能使用说明

## 功能概述

世界资源包功能允许为每个世界配置独立的资源包，包括一个主资源包和一个基础资源包。每个世界的基础资源包相同，但主资源包可以根据世界类型不同而不同。

## 配置文件说明

### resourcepack.yml

位置：`plugins/MUTbuildUtils/resourcepack.yml`

```yaml
# 基础资源包 - 所有世界都会使用的通用资源包
base_resource_pack:
  key: "base"
  url: "https://example.com/resourcepacks/base.zip"
  hash: ""  # SHA-1哈希值
  uuid: ""  # 资源包UUID，新版本Minecraft需要

# 主资源包 - 根据世界类型使用的特定资源包
main_resource_packs:
  normal:
    url: "https://example.com/resourcepacks/normal.zip"
    hash: ""
    uuid: ""
  flat:
    url: "https://example.com/resourcepacks/flat.zip"
    hash: ""
    uuid: ""
  # ... 其他世界类型
```

### 世界配置文件

位置：`plugins/MUTbuildUtils/world/config/<世界名>.yml`

```yaml
# 资源包配置
resourcepack:
  main: "normal"  # 主资源包缩写
  base: "base"    # 基础资源包缩写
```

## 命令使用说明

### /worldresourcepack (别名: /wrp)

#### 子命令

1. **refresh** - 刷新所有资源包的哈希值和UUID
   ```
   /worldresourcepack refresh
   ```
   - 权限：`mutbuildutils.resourcepack.admin`
   - 功能：重新计算并更新所有资源包的SHA-1哈希值，同时生成新的UUID

2. **create** - 添加新的主资源包
   ```
   /worldresourcepack create <缩写> <URL>
   ```
   - 权限：`mutbuildutils.resourcepack.admin`
   - 示例：`/worldresourcepack create custom https://example.com/custom.zip`
   - 功能：添加新的主资源包到配置中

3. **delete** - 删除指定的主资源包
   ```
   /worldresourcepack delete <缩写>
   ```
   - 权限：`mutbuildutils.resourcepack.admin`
   - 示例：`/worldresourcepack delete custom`
   - 功能：从配置中删除指定的主资源包

4. **set** - 为世界设置资源包
   ```
   /worldresourcepack set <缩写> <世界名>
   ```
   - 权限：`mutbuildutils.resourcepack.admin`
   - 示例：`/worldresourcepack set custom MyWorld`
   - 功能：为指定世界设置主资源包和基础资源包

5. **add** - 为玩家添加资源包
   ```
   /worldresourcepack add <缩写> <玩家名>
   ```
   - 权限：`mutbuildutils.resourcepack.admin`
   - 示例：`/worldresourcepack add custom @a` (支持目标选择器)
   - 功能：直接为玩家应用指定的资源包

## 自动功能

### 世界创建时自动设置

当使用 `/worldcreate` 创建世界时，系统会根据世界类型自动设置对应的主资源包：

- `normal` 世界 → `normal` 资源包
- `flat` 世界 → `flat` 资源包
- `nether` 世界 → `nether` 资源包
- `end` 世界 → `end` 资源包
- `ocean` 世界 → `ocean` 资源包
- `desert` 世界 → `desert` 资源包
- `snow` 世界 → `snow` 资源包

### 玩家切换世界时自动应用

当玩家切换到不同世界时，系统会自动：
1. 先应用基础资源包
2. 延迟0.5秒后应用主资源包

### 玩家加入服务器时自动应用

玩家加入服务器时，系统会延迟1秒后自动应用当前世界的资源包。

## 权限节点

- `mutbuildutils.resourcepack.admin` - 允许管理世界资源包 (默认: op)
- `mutbuildutils.resourcepack.use` - 允许使用资源包功能 (默认: true)

## 使用示例

### 1. 添加新的资源包

```bash
# 添加一个自定义资源包
/worldresourcepack create medieval https://cdn.example.com/medieval.zip

# 刷新哈希值
/worldresourcepack refresh
```

### 2. 为世界设置资源包

```bash
# 为城堡世界设置中世纪资源包
/worldresourcepack set medieval castle_world
```

### 3. 为玩家应用资源包

```bash
# 为特定玩家应用资源包
/worldresourcepack add medieval PlayerName

# 为所有在线玩家应用资源包
/worldresourcepack add medieval @a

# 为附近玩家应用资源包
/worldresourcepack add medieval @a[distance=..10]
```

## 注意事项

### 资源包要求

1. **URL格式**：必须是有效的HTTP/HTTPS链接
2. **文件格式**：必须是ZIP格式的资源包
3. **哈希值**：建议设置SHA-1哈希值以确保资源包完整性
4. **UUID**：新版本Minecraft（1.20.3+）需要UUID，系统会自动生成

### 性能考虑

1. **资源包大小**：建议单个资源包不超过100MB
2. **应用延迟**：系统会自动延迟应用资源包，避免冲突
3. **网络要求**：确保玩家能够访问资源包下载链接

### 故障排除

1. **资源包无法下载**：检查URL是否有效，服务器是否可访问
2. **哈希值错误**：使用 `refresh` 命令重新计算哈希值
3. **资源包不生效**：检查玩家客户端设置，确保允许服务器资源包

## 配置文件模板

### 完整的 resourcepack.yml 示例

```yaml
# 基础资源包配置
base_resource_pack:
  key: "base"
  url: "https://cdn.example.com/base-pack.zip"
  hash: "a1b2c3d4e5f6789..."
  uuid: "550e8400-e29b-41d4-a716-446655440000"

# 主资源包配置
main_resource_packs:
  normal:
    url: "https://cdn.example.com/normal-world.zip"
    hash: "1a2b3c4d5e6f789..."
    uuid: "550e8400-e29b-41d4-a716-446655440001"
  flat:
    url: "https://cdn.example.com/flat-world.zip"
    hash: "2b3c4d5e6f7a891..."
    uuid: "550e8400-e29b-41d4-a716-446655440002"
  nether:
    url: "https://cdn.example.com/nether-world.zip"
    hash: "3c4d5e6f7a8b912..."
    uuid: "550e8400-e29b-41d4-a716-446655440003"
  end:
    url: "https://cdn.example.com/end-world.zip"
    hash: "4d5e6f7a8b9c123..."
    uuid: "550e8400-e29b-41d4-a716-446655440004"
  ocean:
    url: "https://cdn.example.com/ocean-world.zip"
    hash: "5e6f7a8b9c1d234..."
    uuid: "550e8400-e29b-41d4-a716-446655440005"
  desert:
    url: "https://cdn.example.com/desert-world.zip"
    hash: "6f7a8b9c1d2e345..."
    uuid: "550e8400-e29b-41d4-a716-446655440006"
  snow:
    url: "https://cdn.example.com/snow-world.zip"
    hash: "7a8b9c1d2e3f456..."
    uuid: "550e8400-e29b-41d4-a716-446655440007"
  medieval:
    url: "https://cdn.example.com/medieval-pack.zip"
    hash: "8b9c1d2e3f4a567..."
    uuid: "550e8400-e29b-41d4-a716-446655440008"
  modern:
    url: "https://cdn.example.com/modern-pack.zip"
    hash: "9c1d2e3f4a5b678..."
    uuid: "550e8400-e29b-41d4-a716-446655440009"
```

这个功能为服务器提供了灵活的资源包管理系统，可以根据不同世界类型为玩家提供不同的游戏体验。