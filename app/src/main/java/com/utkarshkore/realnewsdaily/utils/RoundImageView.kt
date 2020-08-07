package com.utkarshkore.realnewsdaily.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ImageView
import com.utkarshkore.realnewsdaily.R

/**
 * Created by Utkarsh Kore on 8/4/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
open class RoundImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultAttrSet: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet, defaultAttrSet) {
    companion object {
        const val CORNER_NONE: Int = 0
        const val CORNER_TOP_LEFT: Int = 1
        const val CORNER_TOP_RIGHT: Int = 1
        const val CORNER_BOTTOM_LEFT: Int = 1
        const val CORNER_BOTTOM_RIGHT: Int = 1
        val CORNER_ALL: Int = 5

    }

    private val cornerRect: RectF = RectF()
    private val path: Path = Path()
    private var cornerRadius: Int
    private var roundedCorners: Int
    private var arr: TypedArray =
        context.obtainStyledAttributes(attributeSet, R.styleable.RoundImageView)

    init {
        try {
            cornerRadius = arr.getDimensionPixelSize(R.styleable.RoundImageView_cornerRadius, 0)
            roundedCorners =
                arr.getInt(R.styleable.RoundImageView_roundedCorners, CORNER_NONE)
        } finally {
            arr.recycle()
        }
    }

    private fun setPath() {
        path.rewind()

        if (cornerRadius >= 1f && roundedCorners != CORNER_NONE) {
            val twiceRad = cornerRadius * 2

            if (isRounded(CORNER_TOP_LEFT)) {
                cornerRect.offset(0f, 0f)
                path.arcTo(cornerRect, 180f, 90f)
            } else path.moveTo(0f, 0f)

            if (isRounded(CORNER_TOP_RIGHT)) {
                cornerRect.offsetTo((width - twiceRad).toFloat(), 0f)
                path.arcTo(cornerRect, 270f, 90f)
            } else path.lineTo(width.toFloat(), 0f)

            if (isRounded(CORNER_BOTTOM_RIGHT)) {
                cornerRect.offsetTo((width - twiceRad).toFloat(), (height - twiceRad).toFloat())
                path.arcTo(cornerRect, 0f, 90f)
            } else path.lineTo(width.toFloat(), height.toFloat())

            if (isRounded(CORNER_BOTTOM_LEFT)) {
                cornerRect.offsetTo(0f, (height - twiceRad).toFloat())
                path.arcTo(cornerRect, 90f, 90f)
            } else path.lineTo(0f, height.toFloat())

            path.close()
        }
    }

    private fun isRounded(corner: Int): Boolean {
        return (roundedCorners and corner) == corner
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!path.isEmpty)
            canvas?.clipPath(path)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        setPath()
    }
}