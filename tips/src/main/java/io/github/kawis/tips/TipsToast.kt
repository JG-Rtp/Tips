package io.github.kawis.tips

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * A lightweight toast-like Tips card:
 * - Uses the same card layout and position/animation as TipsOverlayManager
 * - No background dim
 * - No manual dismiss (no background click / back press interception)
 * - Automatically dismisses after the given duration
 */
object TipsToast {

    private const val SHOW_DURATION = 220L
    private const val DISMISS_DURATION = 160L

    private var enableStack: Boolean = false
    private var maxStackCount: Int = 5

    private var baseTopOffsetDp: Float = 50f
    private var baseBottomOffsetDp: Float = 20f

    private data class ActiveToast(val overlay: ViewGroup, val cardView: View)

    private val activeTopToasts = mutableListOf<ActiveToast>()
    private val activeBottomToasts = mutableListOf<ActiveToast>()

    /**
     * Enable or disable stacking of multiple TipsToast instances.
     * When enabled, multiple toasts at the same edge (TOP/BOTTOM) will be offset
     * by 10dp per index to avoid overlap.
     */
    fun setStackEnabled(enabled: Boolean) {
        enableStack = enabled
    }

    /**
     * Set the maximum number of stacked toasts to keep.
     * When the limit is reached, the oldest toast will be removed.
     */
    fun setMaxStackCount(count: Int) {
        maxStackCount = count.coerceAtLeast(1)
    }

    /**
     * Set the base offset (in dp) from the top edge when position is TOP.
     */
    fun setTopOffsetDp(offsetDp: Float) {
        baseTopOffsetDp = offsetDp
    }

    /**
     * Set the base offset (in dp) from the bottom edge when position is BOTTOM.
     */
    fun setBottomOffsetDp(offsetDp: Float) {
        baseBottomOffsetDp = offsetDp
    }

    /**
     * Show a toast-like Tips card.
     *
     * @param activity host Activity
     * @param message message text
     * @param position TOP / CENTER / BOTTOM, same as TipsToastPosition
     * @param durationMillis how long to show before auto-dismiss
     * @param title optional title (null to hide title, non-null to show)
     */
    fun show(
        activity: Activity,
        message: CharSequence,
        position: TipsToastPosition = TipsToastPosition.BOTTOM,
        durationMillis: Long = 2000L,
        title: CharSequence? = null,
        customViewLayoutResId: Int? = null
    ) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return

        // Overlay container without dim background
        val overlay = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = false
        }

        val contentRoot = LayoutInflater.from(activity)
            .inflate(R.layout.tipsext_title, overlay, false) as LinearLayout

        contentRoot.gravity = when (position) {
            TipsToastPosition.TOP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            TipsToastPosition.CENTER -> Gravity.CENTER
            TipsToastPosition.BOTTOM -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        overlay.addView(
            contentRoot,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val titleText = contentRoot.findViewById<TextView>(R.id.titleText)
        val messageText = contentRoot.findViewById<TextView>(R.id.messageText)
        val confirmBtn = contentRoot.findViewById<TextView>(R.id.one_confirm_button)
        val cancelBtn = contentRoot.findViewById<TextView>(R.id.one_cancel_button)
        val defaultContentContainer = contentRoot.findViewById<LinearLayout>(R.id.content_container)

        // Configure title & message
        if (title.isNullOrEmpty()) {
            titleText.visibility = View.GONE
        } else {
            titleText.visibility = View.VISIBLE
            titleText.text = title
        }
        messageText.text = message

        // Hide action buttons for toast-style usage
        confirmBtn.visibility = View.GONE
        cancelBtn.visibility = View.GONE

        val cardView = contentRoot.getChildAt(0) as View

        if (customViewLayoutResId != null) {
            defaultContentContainer.visibility = View.GONE
            LayoutInflater.from(activity).inflate(customViewLayoutResId, cardView as ViewGroup, true)
        }

        // Vertical margins same as dialog default
        val lp = cardView.layoutParams as? LinearLayout.LayoutParams
        if (lp != null) {
            val topMarginPx: Int
            val bottomMarginPx: Int
            when (position) {
                TipsToastPosition.TOP -> {
                    topMarginPx = dpToPx(cardView, baseTopOffsetDp).toInt()
                    bottomMarginPx = 0
                }
                TipsToastPosition.BOTTOM -> {
                    topMarginPx = 0
                    bottomMarginPx = dpToPx(cardView, baseBottomOffsetDp).toInt()
                }
                TipsToastPosition.CENTER -> {
                    topMarginPx = 0
                    bottomMarginPx = dpToPx(cardView, 30f).toInt()
                }
            }
            lp.topMargin = topMarginPx
            lp.bottomMargin = bottomMarginPx

            if (enableStack) {
                val indexOffset = when (position) {
                    TipsToastPosition.TOP -> activeTopToasts.size
                    TipsToastPosition.BOTTOM -> activeBottomToasts.size
                    TipsToastPosition.CENTER -> 0
                }
                if (indexOffset > 0) {
                    val offsetPx = dpToPx(cardView, 10f) * indexOffset
                    when (position) {
                        TipsToastPosition.TOP -> lp.topMargin += offsetPx.toInt()
                        TipsToastPosition.BOTTOM -> lp.bottomMargin += offsetPx.toInt()
                        TipsToastPosition.CENTER -> Unit
                    }
                }
            }

            cardView.layoutParams = lp
        }

        if (enableStack) {
            val list = when (position) {
                TipsToastPosition.TOP -> activeTopToasts
                TipsToastPosition.BOTTOM -> activeBottomToasts
                TipsToastPosition.CENTER -> null
            }
            if (list != null) {
                if (list.size >= maxStackCount) {
                    val oldest = list.removeAt(0)
                    val parent = oldest.overlay.parent as? ViewGroup
                    parent?.removeView(oldest.overlay)
                }
            }
        }

        root.addView(overlay)

        if (enableStack) {
            when (position) {
                TipsToastPosition.TOP -> activeTopToasts.add(ActiveToast(overlay, cardView))
                TipsToastPosition.BOTTOM -> activeBottomToasts.add(ActiveToast(overlay, cardView))
                TipsToastPosition.CENTER -> Unit
            }
        }

        animateShow(overlay, cardView, position)

        // Auto dismiss after duration
        overlay.postDelayed({
            animateDismiss(overlay, cardView, position) {
                val parent = overlay.parent as? ViewGroup
                parent?.removeView(overlay)

                if (enableStack) {
                    val list = when (position) {
                        TipsToastPosition.TOP -> activeTopToasts
                        TipsToastPosition.BOTTOM -> activeBottomToasts
                        TipsToastPosition.CENTER -> null
                    }
                    list?.removeAll { it.overlay == overlay }
                }
            }
        }, durationMillis)
    }

    private fun animateShow(overlay: ViewGroup, cardView: View, position: TipsToastPosition) {
        overlay.setBackgroundColor(Color.TRANSPARENT)
        cardView.scaleX = 0.9f
        cardView.scaleY = 0.9f
        cardView.alpha = 0f

        cardView.translationY = when (position) {
            TipsToastPosition.CENTER -> 0f
            TipsToastPosition.TOP -> -dpToPx(cardView, 16f)
            TipsToastPosition.BOTTOM -> dpToPx(cardView, 16f)
        }

        cardView.post {
            when (position) {
                TipsToastPosition.CENTER -> { /* default center */ }
                TipsToastPosition.TOP -> cardView.pivotY = 0f
                TipsToastPosition.BOTTOM -> {
                    val h = cardView.height.takeIf { it > 0 } ?: cardView.measuredHeight
                    if (h > 0) cardView.pivotY = h.toFloat()
                }
            }
        }

        cardView.animate()
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(SHOW_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun animateDismiss(
        overlay: ViewGroup,
        cardView: View,
        position: TipsToastPosition,
        endAction: (() -> Unit)? = null
    ) {
        val baseOffset = dpToPx(cardView, 16f)
        val startTranslation = cardView.translationY
        val targetTranslationY = when (position) {
            TipsToastPosition.CENTER -> startTranslation
            TipsToastPosition.TOP ->
                if (startTranslation < 0f) startTranslation - baseOffset else -baseOffset
            TipsToastPosition.BOTTOM ->
                if (startTranslation > 0f) startTranslation + baseOffset else baseOffset
        }

        when (position) {
            TipsToastPosition.CENTER -> { /* center */ }
            TipsToastPosition.TOP -> cardView.pivotY = 0f
            TipsToastPosition.BOTTOM -> {
                val h = cardView.height.takeIf { it > 0 } ?: cardView.measuredHeight
                if (h > 0) cardView.pivotY = h.toFloat()
            }
        }

        cardView.animate()
            .translationY(targetTranslationY)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .alpha(0f)
            .setDuration(DISMISS_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                endAction?.invoke()
            }
            .start()
    }

    private fun dpToPx(view: View, dp: Float): Float {
        return dp * view.resources.displayMetrics.density
    }
}
