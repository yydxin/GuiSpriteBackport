# GuiSpriteBackport

NeoForge 1.21.1 客户端模组：让 1.21.1 支持 1.21.2+ 资源包的提示框贴图
（`tooltip/background` 和 `tooltip/frame`）。

Backports 1.21.2+ tooltip GUI sprites to NeoForge 1.21.1. Client-side only.

## 使用

1. 安装 NeoForge 1.21.1（21.1.x）和 Java 21。
2. 把 jar 放进 `mods/`（在 Releases 页面下载）。
3. 启用带提示框贴图的资源包。资源包里没有这两张图时，会自动用原版提示框。

## 说明

- 本模组只读取你自己装的资源包，不包含任何资源包素材。
- 已用 CozyUI（作者：零雾〇五Fogg05）测试，提示框可以正常显示。
- 如果边框动画不动，先排查整合包里的渲染优化类模组。
- 代码由 AI 辅助编写，经人工整理和测试。
- 不是 Mojang 或微软的官方产品，与资源包作者无官方关系。

## 许可证

MIT
