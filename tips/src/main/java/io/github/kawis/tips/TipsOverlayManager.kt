package io.github.kawis.tips

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

/**
 * 使用 Overlay（直接加到 Activity 根布局）方式展示的 Tips 弹窗。
 * 好处：和 Activity 内容同一层级，可以在沉浸式状态栏下顶到状态栏背后。
 *
 * Tips dialog implemented via overlay (attached directly to the Activity root view).
 * It shares the same level with Activity content so it can extend under the status bar in immersive mode.
 */

object TipsOverlayManager {

    private const val SHOW_DURATION = 220L
    private const val DISMISS_DURATION = 160L
    private const val TARGET_DIM = 0.25f

    private var currentOverlay: ViewGroup? = null
    private var currentPosition: TipsDialogPosition = TipsDialogPosition.CENTER
    private var backCallback: OnBackPressedCallback? = null

    /**
     * - 支持 TOP / CENTER / BOTTOM 位置
     * - 相同的缩放/方向动画
     * - 背景 dim + 点击背景/返回键关闭
     * - 顶部上滑 / 底部下滑关闭
     *
     * - Supports TOP / CENTER / BOTTOM positions
     * - Same scale and directional animations
     * - Background dim with tap-to-dismiss / back-press-to-dismiss
     * - Swipe up to dismiss from TOP, swipe down to dismiss from BOTTOM
     */
    fun show(
        activity: Activity,
        title: CharSequence,
        message: CharSequence,
        cancelable: Boolean = true,
        position: TipsDialogPosition = TipsDialogPosition.CENTER,
        positiveText: CharSequence? = null,
        negativeText: CharSequence? = null,
        iconResId: Int? = null,
        showIcon: Boolean = false,
        customViewLayoutResId: Int? = null,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        dismiss(immediate = true)

        val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        currentPosition = position
        val overlay = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
        }

        val contentRoot = LayoutInflater.from(activity)
            .inflate(R.layout.tipsext_title, overlay, false) as LinearLayout

        contentRoot.gravity = when (position) {
            TipsDialogPosition.TOP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            TipsDialogPosition.CENTER -> Gravity.CENTER
            TipsDialogPosition.BOTTOM -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
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
        val iconView = contentRoot.findViewById<android.widget.ImageView>(R.id.imageView)
        val defaultContentContainer = contentRoot.findViewById<LinearLayout>(R.id.content_container)

        titleText.text = title
        messageText.text = message

        if (showIcon) {
            iconView.visibility = View.VISIBLE
            if (iconResId != null) {
                iconView.setImageResource(iconResId)
            } else {
                iconView.setImageResource(R.drawable.mouse_cursor)
            }
        } else {
            iconView.visibility = View.GONE
        }

        val cardView = contentRoot.getChildAt(0) as ViewGroup

        if (customViewLayoutResId != null) {
            defaultContentContainer.visibility = View.GONE
            LayoutInflater.from(activity).inflate(customViewLayoutResId, cardView, true)
        }

        val confirmBtn = cardView.findViewById<TextView>(R.id.one_confirm_button)
        val cancelBtn = cardView.findViewById<TextView>(R.id.one_cancel_button)

        if (confirmBtn != null && positiveText != null) {
            confirmBtn.text = positiveText
        }
        if (cancelBtn != null && negativeText != null) {
            cancelBtn.text = negativeText
        }

        val lp = cardView.layoutParams as? LinearLayout.LayoutParams
        if (lp != null) {
            val topMarginPx: Int
            val bottomMarginPx: Int
            when (position) {
                TipsDialogPosition.TOP -> {
                    topMarginPx = dpToPx(cardView, 50f).toInt()
                    bottomMarginPx = 0
                }
                TipsDialogPosition.BOTTOM -> {
                    topMarginPx = 0
                    bottomMarginPx = dpToPx(cardView, 20f).toInt()
                }
                TipsDialogPosition.CENTER -> {
                    topMarginPx = 0
                    bottomMarginPx = dpToPx(cardView, 30f).toInt()
                }
            }
            lp.topMargin = topMarginPx
            lp.bottomMargin = bottomMarginPx
            cardView.layoutParams = lp
        }

        confirmBtn?.setOnClickListener {
            animateDismiss(overlay, cardView, position) {
                onConfirm?.invoke()
            }
        }

        cancelBtn?.setOnClickListener {
            animateDismiss(overlay, cardView, position) {
                onCancel?.invoke()
            }
        }

        if (cancelable) {
            overlay.setOnClickListener {
                animateDismiss(overlay, cardView, position) {
                    onCancel?.invoke()
                }
            }

            if (activity is ComponentActivity) {
                backCallback?.remove()
                val callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        dismiss()
                    }
                }
                activity.onBackPressedDispatcher.addCallback(callback)
                backCallback = callback
            }
        }

        root.addView(overlay)
        currentOverlay = overlay

        setupSwipeDismiss(overlay, cardView, position)
        animateShow(overlay, cardView, position)
    }

    /**
     * 外部调用关闭当前 Overlay
     *
     * Close the current overlay from outside.
     */
    fun dismiss(immediate: Boolean = false) {
        val overlay = currentOverlay ?: return
        // val activity = overlay.context as? Activity ?: return

        val cardView = (overlay.findViewById<View>(R.id.root_layout) as? LinearLayout)?.getChildAt(0)
        if (immediate || cardView == null) {
            removeOverlay(overlay)
        } else {
            animateDismiss(overlay, cardView, currentPosition, null)
        }
    }

    private fun animateShow(overlay: ViewGroup, cardView: View, position: TipsDialogPosition) {
        overlay.setBackgroundColor(Color.TRANSPARENT)
        cardView.scaleX = 0.9f
        cardView.scaleY = 0.9f
        cardView.alpha = 0f

        cardView.translationY = when (position) {
            TipsDialogPosition.CENTER -> 0f
            TipsDialogPosition.TOP -> -dpToPx(cardView, 16f)
            TipsDialogPosition.BOTTOM -> dpToPx(cardView, 16f)
        }

        cardView.post {
            when (position) {
                TipsDialogPosition.CENTER -> { /* default center */ }
                TipsDialogPosition.TOP -> cardView.pivotY = 0f
                TipsDialogPosition.BOTTOM -> {
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

        val endAlpha = (255 * TARGET_DIM).toInt()
        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SHOW_DURATION + 80L
            addUpdateListener { va ->
                val f = va.animatedValue as Float
                val a = (endAlpha * f).toInt().coerceIn(0, 255)
                overlay.setBackgroundColor(Color.argb(a, 0, 0, 0))
            }
            start()
        }
    }

    private fun animateDismiss(
        overlay: ViewGroup,
        cardView: View,
        position: TipsDialogPosition,
        endAction: (() -> Unit)? = null
    ) {
        val baseOffset = dpToPx(cardView, 16f)
        val startTranslation = cardView.translationY
        val targetTranslationY = when (position) {
            TipsDialogPosition.CENTER -> startTranslation
            TipsDialogPosition.TOP ->
                if (startTranslation < 0f) startTranslation - baseOffset else -baseOffset
            TipsDialogPosition.BOTTOM ->
                if (startTranslation > 0f) startTranslation + baseOffset else baseOffset
        }

        when (position) {
            TipsDialogPosition.CENTER -> { /* center */ }
            TipsDialogPosition.TOP -> cardView.pivotY = 0f
            TipsDialogPosition.BOTTOM -> {
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
                removeOverlay(overlay)
            }
            .start()

        val startColor = (overlay.background as? android.graphics.drawable.ColorDrawable)?.color
            ?: Color.argb((255 * TARGET_DIM).toInt(), 0, 0, 0)
        val startAlpha = Color.alpha(startColor)

        android.animation.ValueAnimator.ofFloat(1f, 0f).apply {
            duration = DISMISS_DURATION
            addUpdateListener { va ->
                val f = va.animatedValue as Float
                val a = (startAlpha * f).toInt().coerceAtLeast(0)
                val color = Color.argb(a, 0, 0, 0)
                overlay.setBackgroundColor(color)
            }
            start()
        }
    }

    private fun removeOverlay(overlay: ViewGroup) {
        val parent = overlay.parent as? ViewGroup
        parent?.removeView(overlay)
        if (currentOverlay === overlay) {
            currentOverlay = null
            backCallback?.remove()
            backCallback = null
        }
    }

    private fun setupSwipeDismiss(
        overlay: ViewGroup,
        cardView: View,
        position: TipsDialogPosition
    ) {
        val enableSwipe = when (position) {
            TipsDialogPosition.TOP, TipsDialogPosition.BOTTOM -> true
            TipsDialogPosition.CENTER -> false
        }
        if (!enableSwipe) return

        val threshold = dpToPx(cardView, 80f)

        cardView.setOnTouchListener(object : View.OnTouchListener {
            var downY = 0f
            var dragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downY = event.rawY
                        dragging = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dy = event.rawY - downY
                        when (position) {
                            TipsDialogPosition.TOP -> {
                                if (dy < 0) {
                                    dragging = true
                                    v.translationY = dy

                                    val progress = kotlin.math.min(1f, kotlin.math.abs(dy) / threshold)
                                    val scale = 1f - 0.05f * progress
                                    v.scaleX = scale
                                    v.scaleY = scale

                                    val alpha = (255 * TARGET_DIM * (1f - 0.5f * progress)).toInt()
                                    overlay.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
                                    return true
                                } else if (dy > 0) {
                                    dragging = true
                                    val clampedDy = kotlin.math.min(threshold * 1.5f, dy)
                                    v.translationY = clampedDy
                                    return true
                                }
                            }
                            TipsDialogPosition.BOTTOM -> {
                                if (dy > 0) {
                                    dragging = true
                                    v.translationY = dy

                                    val progress = kotlin.math.min(1f, kotlin.math.abs(dy) / threshold)
                                    val scale = 1f - 0.05f * progress
                                    v.scaleX = scale
                                    v.scaleY = scale

                                    val alpha = (255 * TARGET_DIM * (1f - 0.5f * progress)).toInt()
                                    overlay.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
                                    return true
                                } else if (dy < 0) {
                                    dragging = true
                                    val clampedDy = kotlin.math.max(-threshold * 1.5f, dy)
                                    v.translationY = clampedDy
                                    return true
                                }
                            }
                            TipsDialogPosition.CENTER -> { /* No processed*/ }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (dragging) {
                            val finalTranslation = v.translationY
                            val shouldDismiss = when (position) {
                                TipsDialogPosition.TOP -> -finalTranslation >= threshold
                                TipsDialogPosition.BOTTOM -> finalTranslation >= threshold
                                TipsDialogPosition.CENTER -> false
                            }

                            if (shouldDismiss) {
                                animateDismiss(overlay, cardView, position, null)
                            } else {
                                v.animate()
                                    .translationY(0f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300L)
                                    .setInterpolator(OvershootInterpolator(2.0f))
                                    .start()

                                val alpha = (255 * TARGET_DIM).toInt()
                                overlay.setBackgroundColor(Color.argb(alpha, 0, 0, 0))
                            }
                            dragging = false
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun dpToPx(view: View, dp: Float): Float {
        return dp * view.resources.displayMetrics.density
    }
}
