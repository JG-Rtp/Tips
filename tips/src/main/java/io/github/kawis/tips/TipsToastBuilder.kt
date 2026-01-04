package io.github.kawis.tips

import android.app.Activity
import android.content.Context

/**
 * Builder-style API for showing a toast-like Tips card.
 * Similar to TipsDialogBuilder, but:
 * - no background dim
 * - no buttons / manual dismiss
 * - auto dismiss after a given duration
 */
class TipsToastBuilder(private val context: Context) {

    private var title: CharSequence? = null
    private var message: CharSequence? = null
    private var position: TipsToastPosition = TipsToastPosition.BOTTOM
    private var durationMillis: Long = 2000L
    private var stackEnabled: Boolean? = null
    private var stackMaxCount: Int? = null
    private var customViewLayoutResId: Int? = null
    private var topOffsetDp: Float? = null
    private var bottomOffsetDp: Float? = null

    fun setTitle(text: CharSequence): TipsToastBuilder = apply {
        title = text
    }

    fun setMessage(text: CharSequence): TipsToastBuilder = apply {
        message = text
    }

    fun setPosition(pos: TipsToastPosition): TipsToastBuilder = apply {
        position = pos
    }

    /**
     * Set a custom content layout for the toast card. When this is used,
     * the default title/message/buttons area in the template will be hidden
     * and the custom layout will be added into the TipsCardContainer.
     */
    fun setView(layoutResId: Int): TipsToastBuilder = apply {
        customViewLayoutResId = layoutResId
    }

    /**
     * Set how long (in milliseconds) the toast card will be shown
     * before it auto dismisses.
     */
    fun setDuration(durationMillis: Long): TipsToastBuilder = apply {
        this.durationMillis = durationMillis
    }

    /**
     * Set the base offset (in dp) from the top edge when position is TOP.
     */
    fun setTopOffsetDp(offsetDp: Float): TipsToastBuilder = apply {
        topOffsetDp = offsetDp
    }

    /**
     * Set the base offset (in dp) from the bottom edge when position is BOTTOM.
     */
    fun setBottomOffsetDp(offsetDp: Float): TipsToastBuilder = apply {
        bottomOffsetDp = offsetDp
    }

    /**
     * Enable or disable stacking for TipsToast globally.
     * If not called, the default value in TipsToast will be used.
     */
    fun setStackEnabled(enabled: Boolean): TipsToastBuilder = apply {
        stackEnabled = enabled
    }

    /**
     * Set the maximum number of stacked toasts to keep globally.
     * If not called, the default value in TipsToast will be used.
     */
    fun setMaxStackCount(count: Int): TipsToastBuilder = apply {
        stackMaxCount = count
    }

    fun show() {
        val activity = context as? Activity ?: return

        val msg = message ?: ""

        stackEnabled?.let { TipsToast.setStackEnabled(it) }
        stackMaxCount?.let { TipsToast.setMaxStackCount(it) }
        topOffsetDp?.let { TipsToast.setTopOffsetDp(it) }
        bottomOffsetDp?.let { TipsToast.setBottomOffsetDp(it) }

        TipsToast.show(
            activity = activity,
            message = msg,
            position = position,
            durationMillis = durationMillis,
            title = title,
            customViewLayoutResId = customViewLayoutResId
        )
    }
}
