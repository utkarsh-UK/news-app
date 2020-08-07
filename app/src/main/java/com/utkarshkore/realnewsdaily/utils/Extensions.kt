package com.utkarshkore.realnewsdaily.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Utkarsh Kore on 8/6/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
@SuppressLint("ClickableViewAccessibility")
fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { view, motionEvent ->
        var hasConsumed = false
        if (view is EditText){
            if (motionEvent.x >= width - view.totalPaddingRight){
                if (motionEvent.action == MotionEvent.ACTION_UP) onClicked(this)
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(
    action: (T) -> Unit
) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}