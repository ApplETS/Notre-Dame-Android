package ca.etsmtl.etsmobile.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

/**
 * Created by Sonphil on 23-06-18.
 */

/**
 * Fades a [View] to the provided visibility state
 *
 * @param visibility [View.VISIBLE], [View.GONE] or [View.INVISIBLE]
 * @param shortAnimTime Fade duration
 */
fun View.fadeTo(
    visibility: Int,
    shortAnimTime: Long = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
) {
    animate().setDuration(shortAnimTime)
            .alpha((if (visibility == View.VISIBLE) 1 else 0).toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    this@fadeTo.visibility = visibility
                }
            })
}