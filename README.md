# Qz-UILibAddon-FontRender - 基于Skija的次世代字体渲染扩展

## 🚀 核心功能

**TrueType高清渲染引擎**
- 动态字符页生成技术（自动处理0x0000-0x10FFFF码位）
- 亚像素抗锯齿文本渲染（4倍于原版的分辨率）
- 支持OpenType高级排版特性（连字、替代字形）

**突破性文字支持**
```java
// 示例：渲染任意Unicode字符
skijaFont.drawText("𓀀𓀁𓀂𓀃 ㊙🈲🉐", x, y); // 古埃及象形文字 + 异体汉字
```

**革命性工作流​​**

-游戏内剪切板直通支持（Ctrl+V粘贴UTF-32文本）
-动态字体Fallback系统（自动匹配系统字体库）

##📥 安装要求
**前置依赖**
- 必须安装 Qz-UILib
- Minecraft 1.7.10
