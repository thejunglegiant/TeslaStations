package com.thejunglegiant.teslastations.extensions

import android.view.View
import android.widget.ImageButton
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible

private const val DEFAULT_CHECKING_DURATION = 200L

fun View.showFromTop(duration: Long = 200, onAnimationFinished: (() -> Unit)? = null) {
    if (isVisible)
        return

    alpha = 0f
    translationY = -height.toFloat()
    show()

    animate()
        .setDuration(duration)
        .translationY(0f)
        .alpha(1f)
        .withEndAction {
            onAnimationFinished?.invoke()
        }
}

fun View.goneToTop(duration: Long = 200, onAnimationFinished: (() -> Unit)? = null) {
    if (!isVisible)
        return

    animate()
        .setDuration(duration)
        .translationY(-height.toFloat())
        .alpha(0f)
        .withEndAction {
            gone()
            onAnimationFinished?.invoke()
        }
}

fun ImageButton.addRemoveCheck(
    isChecked: Boolean,
    @DrawableRes uncheckedDrawable: Int,
    @DrawableRes checkedDrawable: Int,
    duration: Long = DEFAULT_CHECKING_DURATION
) {
    if (isChecked) {
        check(checkedDrawable, duration)
    } else {
        uncheck(uncheckedDrawable, duration)
    }
}

private fun ImageButton.check(
    @DrawableRes checkedDrawable: Int,
    duration: Long = DEFAULT_CHECKING_DURATION
) {
    animate()
        .setDuration(duration)
        .scaleX(1.2f)
        .scaleY(1.2f)
        .withEndAction {
            setImageResource(checkedDrawable)
            animate()
                .setDuration(duration)
                .scaleX(1f)
                .scaleY(1f)
        }
}

private fun ImageButton.uncheck(
    @DrawableRes uncheckedDrawable: Int,
    duration: Long = DEFAULT_CHECKING_DURATION
) {
    animate()
        .setDuration(duration)
        .scaleX(1.2f)
        .scaleY(1.2f)
        .withEndAction {
            setImageResource(uncheckedDrawable)
            animate()
                .setDuration(duration)
                .scaleX(1f)
                .scaleY(1f)
        }
}

fun View.goneWithSomeDelay(delay: Long) {
    animate()
        .setStartDelay(delay)
        .setDuration(200)
        .alpha(0f)
        .withEndAction {
            gone()
            alpha = 1f
        }
}