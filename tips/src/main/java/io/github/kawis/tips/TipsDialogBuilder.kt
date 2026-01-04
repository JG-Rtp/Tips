package io.github.kawis.tips

import android.app.Activity
import android.content.Context

/**
 * 类似 MaterialAlertDialogBuilder 的用法，但使用自定义的 Tips 弹窗样式和动画。
 * 不支持 setView，只支持标题、内容和正/负按钮。
 *
 * Usage is similar to MaterialAlertDialogBuilder, but with a custom Tips dialog style and animations.
 * Does not support setView in the traditional sense, only title, message and positive/negative buttons.
 */

enum class TipsDialogPosition {
    TOP,
    CENTER,
    BOTTOM
}

class TipsDialogBuilder(private val context: Context) {

    private var title: CharSequence? = null
    private var message: CharSequence? = null
    private var cancelable: Boolean = true

    private var topSwipeEnabled: Boolean = true
    private var bottomSwipeEnabled: Boolean = true

    private var positiveText: CharSequence? = null
    private var negativeText: CharSequence? = null

    private var iconResId: Int? = null
    private var showIcon: Boolean = false
    private var customViewLayoutResId: Int? = null

    private var positiveListener: (() -> Unit)? = null
    private var negativeListener: (() -> Unit)? = null

    private var position: TipsDialogPosition = TipsDialogPosition.CENTER

    fun setTitle(text: CharSequence): TipsDialogBuilder = apply {
        title = text
    }

    fun setMessage(text: CharSequence): TipsDialogBuilder = apply {
        message = text
    }

    fun setCancelable(flag: Boolean): TipsDialogBuilder = apply {
        cancelable = flag
    }

    /**
     * Deprecated. Swipe-to-dismiss behavior is now managed internally by the overlay.
     * This method is kept only for API compatibility and has no effect.
     */
    @Deprecated("Top swipe enable flag is no longer used; swipe behavior is managed internally.")
    fun setTopSwipeEnabled(enabled: Boolean): TipsDialogBuilder = apply {
        topSwipeEnabled = enabled
    }

    /**
     * Deprecated. Swipe-to-dismiss behavior is now managed internally by the overlay.
     * This method is kept only for API compatibility and has no effect.
     */
    @Deprecated("Bottom swipe enable flag is no longer used; swipe behavior is managed internally.")
    fun setBottomSwipeEnabled(enabled: Boolean): TipsDialogBuilder = apply {
        bottomSwipeEnabled = enabled
    }

    fun setPosition(pos: TipsDialogPosition): TipsDialogBuilder = apply {
        position = pos
    }

    /**
     * 设置自定义内容布局，将被添加到 TipsCardContainer 内部；
     * 使用该方法时，默认的标题/内容/按钮区域会被隐藏。
     *
     * Set a custom content layout that will be added inside TipsCardContainer.
     * When this is used, the default title/message/buttons area will be hidden.
     */
    fun setView(layoutResId: Int): TipsDialogBuilder = apply {
        customViewLayoutResId = layoutResId
    }

    /**
     * 设置图标资源，并自动显示图标。
     *
     * Set an icon resource and automatically show the icon.
     */
    fun setIcon(resId: Int): TipsDialogBuilder = apply {
        iconResId = resId
        showIcon = true
    }

    /**
     * 控制是否显示图标；当为 true 且未设置自定义图标时，将显示默认图标。
     *
     * Control whether the icon is visible; when true and no custom icon is set,
     * a default icon will be shown.
     */
    fun setIconVisible(visible: Boolean): TipsDialogBuilder = apply {
        showIcon = visible
    }

    fun setPositiveButton(text: CharSequence, listener: () -> Unit): TipsDialogBuilder = apply {
        positiveText = text
        positiveListener = listener
    }

    fun setNegativeButton(text: CharSequence, listener: () -> Unit): TipsDialogBuilder = apply {
        negativeText = text
        negativeListener = listener
    }

    fun show() {
        val activity = context as? Activity ?: return

        TipsOverlayManager.show(
            activity = activity,
            title = title ?: "",
            message = message ?: "",
            cancelable = cancelable,
            position = position,
            positiveText = positiveText,
            negativeText = negativeText,
            iconResId = iconResId,
            showIcon = showIcon,
            customViewLayoutResId = customViewLayoutResId,
            onConfirm = {
                positiveListener?.invoke()
            },
            onCancel = {
                negativeListener?.invoke()
            }
        )
    }
}
