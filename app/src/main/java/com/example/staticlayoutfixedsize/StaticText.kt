package com.example.staticlayoutfixedsize

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.util.AttributeSet
import android.view.View

class StaticText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mHasBigStartLetter = false

    private var mText: CharSequence = ""
    private val mTextPaint = TextPaint()
    private lateinit var mStaticLayout: StaticLayout

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StaticText,
            defStyleAttr, 0
        ).apply {
            try {
                mTextPaint.textSize = getDimension(
                    R.styleable.StaticText_android_textSize,
                    DEFAULT_TEXT_SIZE * context.resources.displayMetrics.scaledDensity
                )
                mTextPaint.color = getColor(R.styleable.StaticText_android_textColor, Color.BLACK)

                mHasBigStartLetter = getBoolean(R.styleable.StaticText_hasBigStartLetter, false)
                mText = getText(R.styleable.StaticText_android_text) ?: ""
                if (mHasBigStartLetter) {
                    val ss = SpannableString(mText)
                    ss.setSpan(
                        AbsoluteSizeSpan((mTextPaint.textSize * 2).toInt(), false),
                        0,
                        1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    mText = ss
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mStaticLayout = makeStaticLayout(mText, width, getMaxLines(height))
    }

    private fun getMaxLines(maxHeight: Int): Int {
        if (mHasBigStartLetter) {
            return getMaxLinesByInspection(
                makeStaticLayout(mText, width, Integer.MAX_VALUE),
                maxHeight
            )
        }

        // Build a dummy StaticLayout to get the internal measurements.
        return makeStaticLayout("", width, 1).run {
            val lineHeight = getLineBottom(0) - getLineTop(0) + topPadding - bottomPadding
            (maxHeight - topPadding - bottomPadding) / lineHeight
        }
    }

    private fun getMaxLinesByInspection(staticLayout: StaticLayout, maxHeight: Int): Int {
        var line = staticLayout.lineCount - 1
        while (line >= 0 && staticLayout.getLineBottom(line) >= maxHeight) {
            line--
        }
        return line + 1
    }

    override fun onDraw(canvas: Canvas?) {
        mStaticLayout.draw(canvas)
    }

    private fun makeStaticLayout(
        newText: CharSequence,
        maxWidth: Int,
        maxLines: Int
    ): StaticLayout {
        return StaticLayout.Builder
            .obtain(newText, 0, newText.length, mTextPaint, maxWidth)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
    }

    companion object {
        const val DEFAULT_TEXT_SIZE = 18f // 18sp
    }
}