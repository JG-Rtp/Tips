# Tips 弹窗组件

## 简介

`Tips` 是一个基于 **Overlay** 的自定义卡片弹窗组件，用来替代传统 `Dialog` / `AlertDialog`，支持：

- 顶部 / 中间 / 底部 多位置弹出  
- 自定义圆角卡片 + 阴影 + 内容裁剪  
- 方向感知的出现 / 关闭动画 + 背景渐变 dim  
- 顶部上滑 / 底部下滑手势关闭 + 弹性回弹  
- 自定义图标、按钮文案  
- 支持 `setView()` 加载自定义布局到卡片内部  
- 自动拦截系统返回键，内部优先关闭弹窗  

无需 Material / AndroidX Dialog 组件，直接加在 `Activity` 根布局上，可自然延伸到状态栏区域。

---

## 主要类一览

- **`TipsCardContainer`**  
  自定义卡片容器（继承 `FrameLayout`）：
  - 圆角、阴影、内容裁剪
  - 用作弹窗内容的卡片外壳

- **`TipsOverlayManager`**  
  Overlay 版弹窗管理类：
  - 直接将一个全屏 `FrameLayout` 加到 `Activity` 的 `android.R.id.content`
  - 负责布局加载、动画、背景 dim、手势、返回键拦截等

- **`TipsDialogBuilder`**  
  类似 `MaterialAlertDialogBuilder` 的 Builder API：
  - `setTitle` / `setMessage` / `setPositiveButton` / `setNegativeButton`
  - `setPosition` 设置弹窗位置
  - `setIcon` / `setIconVisible` 控制图标
  - `setView` 注入自定义内容布局
  - 内部全部走 `TipsOverlayManager`，不再使用旧的 Dialog 方案

---

## 弹窗位置枚举

```kotlin
enum class TipsDialogPosition {
    TOP,
    CENTER,
    BOTTOM
}
```

- `TOP`：顶部卡片，从上方轻微下移 + 放大进入；支持**上滑关闭**。
- `CENTER`：中间卡片，中心缩放进入；**不支持滑动关闭**。
- `BOTTOM`：底部卡片，从下方轻微上移 + 放大进入；支持**下滑关闭**。

---

## 快速开始

### 依赖引入

在需要使用的 module 的 `build.gradle` 中添加依赖，例如：

```gradle
dependencies {
    implementation 'io.github.kawis:tips:1.0.0'
}
```

然后在 `Activity` 中直接使用 `TipsDialogBuilder`。

### 最简单示例

```kotlin
TipsDialogBuilder(this)
    .setTitle("主标题")
    .setMessage("这是一个提示内容")
    .setPosition(TipsDialogPosition.CENTER)
    .setPositiveButton("确定") {
        // TODO: 确认逻辑
    }
    .setNegativeButton("取消") {
        // TODO: 取消逻辑
    }
    .show()
```

---

## Builder API 说明

### 标题 / 文本 / 位置

```kotlin
setTitle("主标题")
setMessage("内容文案")
setPosition(TipsDialogPosition.TOP)   // TOP / CENTER / BOTTOM
setCancelable(true)                  // 是否允许点击背景 / 返回键关闭
```

### 按钮

```kotlin
setPositiveButton("确定") {
    // 点击“确定”
}

setNegativeButton("取消") {
    // 点击“取消”
}
```

- 默认布局下，这两个按钮分别绑定到布局里的 `@id/one_confirm_button` / `@id/one_cancel_button`。
- 如果使用自定义布局且仍然使用这两个 id，按钮文本和点击回调会自动绑定过去。

### 图标相关

```kotlin
// 设置自定义图标，并自动显示
setIcon(R.drawable.my_icon)

// 只控制显隐：true 时显示图标；如果未设置自定义图标，就加载默认图标
.setIconVisible(true)
```

行为规则：

- 默认：不调用任何图标相关方法 → 图标 `GONE`。
- 调用 `setIcon(resId)` → 显示指定图标。
- 调用 `setIconVisible(true)` 但未调用 `setIcon()` → 显示默认图标 `R.drawable.mouse_cursor`。

---

## 自定义内容布局：`setView()`

如果你想完全控制卡片内部 UI，可以使用 `setView()`：

```kotlin
TipsDialogBuilder(this)
    .setPosition(TipsDialogPosition.BOTTOM)
    .setView(R.layout.my_custom_content)
    .setPositiveButton("好的") {
        // 点击自定义布局里的“确定”按钮
    }
    .setNegativeButton("取消") {
        // 点击自定义布局里的“取消”按钮
    }
    .show()
```

基础布局 `tipsext_simple.xml` 结构简化如下：

```xml
<io.github.kawis.tips.TipsCardContainer
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- subview -->
    
</io.github.kawis.tips.TipsCardContainer>
```

使用 `setView(R.layout.xxx)` 时：

1. `content_container` 会被 `GONE`，默认标题 / 文本 / 按钮区域隐藏。
2. 你的 `R.layout.xxx` 会被 `inflate` 到 `TipsCardContainer` 里面。
3. **如果你的自定义布局里包含以下 id：**
   - `@id/one_confirm_button`
   - `@id/one_cancel_button`
   
   则：
   - 文案自动使用 `setPositiveButton` / `setNegativeButton` 传入的文本；
   - 点击事件自动走 Builder 提供的回调，并带关闭动画。

**如果自定义布局不用这两个 id：**

- 就完全由你自己处理按钮逻辑，不会影响其它功能。

---

## 总结

你可以把这个组件理解为一个：

- 有统一样式的「卡片弹窗框架」；
- 用 Builder 写法配置内容、按钮、位置、图标；
- 需要复杂 UI 时，用 `setView()` 把自己的布局塞进 `TipsCardContainer`，保留所有默认动画、阴影和手势
---
---
# Tips Dialog Component (English)

## Overview

`Tips` is an overlay-based custom card dialog component designed to replace traditional `Dialog` / `AlertDialog`. It supports:

- Top / center / bottom positions  
- Rounded card container with shadow and clipping  
- Position-aware show & dismiss animations with background dimming  
- Swipe-to-dismiss (top: swipe up, bottom: swipe down) with elastic rebound  
- Custom icon and button texts  
- `setView()` to inject your own layout into the card  
- Automatic interception of system back press to close the dialog first  

It does not depend on Material / AndroidX dialog components. The overlay is added directly to the `Activity`'s `android.R.id.content`, so it can extend under the status bar naturally.

---

## Core Classes

- **`TipsCardContainer`**  
  Custom card container (`FrameLayout`):
  - Rounded corners, shadow, and content clipping
  - Acts as the visual shell of the dialog card

- **`TipsOverlayManager`**  
  Overlay-based dialog manager:
  - Adds a full-screen `FrameLayout` into `Activity`'s root view
  - Handles layout inflation, animations, background dim, swipe, and back press

- **`TipsDialogBuilder`**  
  A builder-style API similar to `MaterialAlertDialogBuilder`:
  - `setTitle` / `setMessage` / `setPositiveButton` / `setNegativeButton`
  - `setPosition` for dialog position
  - `setIcon` / `setIconVisible` for the icon
  - `setView` to inject a custom layout into the card
  - Internally uses `TipsOverlayManager` only (no traditional Dialog)

---

## Positions

```kotlin
enum class TipsDialogPosition {
    TOP,
    CENTER,
    BOTTOM
}
```

- `TOP`: card at the top, slightly translating from above with scale-in, **supports swipe up to dismiss**.
- `CENTER`: centered card with scale-in, **no swipe-to-dismiss**.
- `BOTTOM`: bottom card, slightly translating from below with scale-in, **supports swipe down to dismiss**.

---

## Getting Started

### Dependency

Add the dependency to your module's `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.kawis:tips:1.0.0'
}
```

Then you can use `TipsDialogBuilder` in your `Activity`.

### Basic Example

```kotlin
TipsDialogBuilder(this)
    .setTitle("Title")
    .setMessage("This is a message")
    .setPosition(TipsDialogPosition.CENTER)
    .setPositiveButton("OK") {
        // TODO: positive action
    }
    .setNegativeButton("Cancel") {
        // TODO: negative action
    }
    .show()
```

---

## Builder API

### Title / Message / Position

```kotlin
setTitle("Title")
setMessage("Message")
setPosition(TipsDialogPosition.TOP)   // TOP / CENTER / BOTTOM
setCancelable(true)                   // Whether background click / back press can dismiss
```

### Buttons

```kotlin
setPositiveButton("OK") {
    // Positive button clicked
}

setNegativeButton("Cancel") {
    // Negative button clicked
}
```

In the default layout, these are wired to `@id/one_confirm_button` and `@id/one_cancel_button`. 
If you use a custom layout and keep the same ids, the text and callbacks will still be applied automatically.

### Icon

```kotlin
// Set a custom icon and show it
setIcon(R.drawable.my_icon)

// Only control visibility; when true and no custom icon set, a default icon is used
setIconVisible(true)
```

Behavior:

- By default (no icon APIs called), the icon view is `GONE`.
- Calling `setIcon(resId)` makes the icon visible with your resource.
- Calling `setIconVisible(true)` without `setIcon()` shows a default icon (`R.drawable.mouse_cursor`).

---

## Custom Content with `setView()`

If you want full control of the card content, use `setView()`:

```kotlin
TipsDialogBuilder(this)
    .setPosition(TipsDialogPosition.BOTTOM)
    .setView(R.layout.my_custom_content)
    .setPositiveButton("OK") {
        // Click on positive button inside custom layout
    }
    .setNegativeButton("Cancel") {
        // Click on negative button inside custom layout
    }
    .show()
```

The base layout contains a `TipsCardContainer`, simplified as:

```xml
<io.github.kawis.tips.TipsCardContainer
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- subview -->
    
</io.github.kawis.tips.TipsCardContainer>
```

When you call `setView(R.layout.xxx)`:

1. The default content area is hidden.  
2. Your `R.layout.xxx` gets inflated into `TipsCardContainer`.  
3. If your custom layout defines:
   - `@id/one_confirm_button`
   - `@id/one_cancel_button`

   Then:
   - Texts are taken from `setPositiveButton` / `setNegativeButton`.  
   - Click listeners trigger the builder callbacks and play the dismiss animation.

If your layout does **not** use these ids, you are fully responsible for your own button logic.

---

## Behavior Summary

- Overlay is added on top of `Activity` content with dimmed background.
- Background click (when `cancelable = true`) dismisses the dialog and triggers the negative callback.
- System back press is intercepted by an `OnBackPressedCallback` to close the Tips first.
- Top / bottom dialogs support direction-aware swipe-to-dismiss with an elastic rebound when not reaching the threshold.

This library acts as a unified "card-style dialog framework":

- Configure title, message, buttons, position, and icon via a simple builder.  
- For complex UI, use `setView()` to inject your own layout into `TipsCardContainer` while reusing all default animations, shadow, and gestures.