package io.github.kawis.tips

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TipsCardContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val outerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val outerShadowRect = RectF()
    private val outerContentRect = RectF()
    private val innerRect = RectF()
    private val innerClipPath = Path()

    private var cardCornerRadius = dpToPx(16f)
    private val innerExtraRadius = dpToPx(2f)
    private var shadowRadius = dpToPx(8f)
    private var innerMargin = dpToPx(8f)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        var outerBgColor = runCatching {
            context.getColorResourceByName("card_default_background")
        }.getOrElse {
            Color.parseColor("#D6DAE3")
        }

        var shadowColor = runCatching {
            context.getColorResourceByName("card_default_shadow")
        }.getOrElse {
            Color.parseColor("#66000000")
        }

        var innerBgColor = runCatching {
            context.getColorResourceByName("white")
        }.getOrElse {
            Color.WHITE
        }
        
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.TipsCardContainer, defStyleAttr, 0)

            if (a.hasValue(R.styleable.TipsCardContainer_tipsCardRadius)) {
                val radiusPx = a.getDimension(R.styleable.TipsCardContainer_tipsCardRadius, cardCornerRadius)
                cardCornerRadius = radiusPx
            }

            if (a.hasValue(R.styleable.TipsCardContainer_tipsCardBackgroundColor)) {
                innerBgColor = a.getColor(R.styleable.TipsCardContainer_tipsCardBackgroundColor, innerBgColor)
            }

            if (a.hasValue(R.styleable.TipsCardContainer_tipsStrokeColor)) {
                outerBgColor = a.getColor(R.styleable.TipsCardContainer_tipsStrokeColor, outerBgColor)
            }

            if (a.hasValue(R.styleable.TipsCardContainer_tipsStrokeWidth)) {
                innerMargin = a.getDimension(R.styleable.TipsCardContainer_tipsStrokeWidth, innerMargin)
            }

            if (a.hasValue(R.styleable.TipsCardContainer_tipsElevation)) {
                shadowRadius = a.getDimension(R.styleable.TipsCardContainer_tipsElevation, shadowRadius)
            }

            a.recycle()
        }

        outerBackgroundPaint.color = outerBgColor

        outerShadowPaint.color = shadowColor
        outerShadowPaint.style = Paint.Style.FILL
        outerShadowPaint.setShadowLayer(shadowRadius, 0f, 0f, shadowColor)

        innerBackgroundPaint.color = innerBgColor

        val padding = innerMargin.toInt()
        setPadding(padding, padding, padding, padding)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        innerRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )

        outerContentRect.set(
            innerRect.left - innerMargin,
            innerRect.top - innerMargin,
            innerRect.right + innerMargin,
            innerRect.bottom + innerMargin
        )

        val outerInset = shadowRadius / 3f
        outerShadowRect.set(
            outerContentRect.left - outerInset,
            outerContentRect.top - outerInset,
            outerContentRect.right + outerInset,
            outerContentRect.bottom + outerInset
        )
    }

    override fun dispatchDraw(canvas: android.graphics.Canvas) {
        val outerShadowCorner = cardCornerRadius + shadowRadius / 2f
        canvas.drawRoundRect(outerShadowRect, outerShadowCorner, outerShadowCorner, outerShadowPaint)
        canvas.drawRoundRect(outerContentRect, cardCornerRadius, cardCornerRadius, outerBackgroundPaint)

        val innerCornerRadius = (cardCornerRadius - innerExtraRadius).coerceAtLeast(0f)
        canvas.drawRoundRect(innerRect, innerCornerRadius, innerCornerRadius, innerBackgroundPaint)

        innerClipPath.reset()
        innerClipPath.addRoundRect(innerRect, innerCornerRadius, innerCornerRadius, Path.Direction.CW)

        val save = canvas.save()
        canvas.clipPath(innerClipPath)

        super.dispatchDraw(canvas)

        canvas.restoreToCount(save)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}

private fun Context.getColorResourceByName(name: String): Int {
    val id = resources.getIdentifier(name, "color", packageName)
    if (id == 0) {
        throw IllegalArgumentException("Color resource not found: $name")
    }
    return resources.getColor(id, theme)
}

