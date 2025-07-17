# CustomModelData 使用说明

## 概述

MUTbuildUtils 插件现在支持在世界创建菜单中使用 CustomModelData，这允许您为菜单项目使用自定义的物品模型。此功能完全兼容 Minecraft 1.21.4。

## 配置方式

在 `menu.yml` 文件中，您可以为任何世界模板添加 `custom_model_data` 字段。插件支持多种数据类型：

### 1. 整数形式（推荐）

```yaml
- slot: 19
  material: DIAMOND
  name: "§b自定义模型世界"
  lore:
    - "§7使用自定义模型的世界"
    - "§e点击创建"
  world: "custom1"
  custom_model_data: 1001
```

### 2. 字符串形式

```yaml
- slot: 21
  material: EMERALD
  name: "§a自定义模型世界"
  lore:
    - "§7使用自定义模型的世界"
    - "§e点击创建"
  world: "custom2"
  custom_model_data: "2002"
```

### 3. 浮点数形式（自动转换为整数）

```yaml
- slot: 23
  material: GOLD_INGOT
  name: "§6自定义模型世界"
  lore:
    - "§7使用自定义模型的世界"
    - "§e点击创建"
  world: "custom3"
  custom_model_data: 3003.5  # 将被转换为 3003
```

## 注意事项

1. **兼容性**: 此功能完全兼容 Minecraft 1.21.4 的 CustomModelData API
2. **数据类型**: 支持整数、字符串和浮点数，浮点数会自动转换为整数
3. **错误处理**: 如果提供的字符串不是有效数字，插件会输出警告并忽略该设置
4. **可选字段**: `custom_model_data` 是可选的，不添加此字段不会影响现有功能
5. **资源包**: 要看到自定义模型效果，您需要安装相应的资源包

## 资源包配置

要使用 CustomModelData，您需要在资源包中配置相应的模型文件。例如：

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "item/diamond"
  },
  "overrides": [
    {
      "predicate": {
        "custom_model_data": 1001
      },
      "model": "item/custom_world_icon_1"
    }
  ]
}
```

## 示例配置

完整的示例配置已包含在 `menu.yml` 文件中，您可以参考这些示例来创建自己的自定义模型世界模板。

## 故障排除

- 如果自定义模型没有显示，请检查资源包是否正确安装
- 确保 CustomModelData 值与资源包中定义的值匹配
- 查看控制台输出，插件会在设置 CustomModelData 时输出相关信息