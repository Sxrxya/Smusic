package com.smusic.app.player

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrossfadeHandler @Inject constructor() {

    private var fadeOutAnimator: ValueAnimator? = null
    private var fadeInAnimator: ValueAnimator? = null
    private var crossfadeDurationMs: Long = 0L
    private var isActive = false

    var onFadeOut: ((Float) -> Unit)? = null
    var onFadeIn: ((Float) -> Unit)? = null
    var onCrossfadeStart: (() -> Unit)? = null
    var onCrossfadeEnd: (() -> Unit)? = null

    fun setCrossfadeDuration(seconds: Int) {
        crossfadeDurationMs = seconds * 1000L
    }

    fun isEnabled(): Boolean = crossfadeDurationMs > 0

    fun shouldStartCrossfade(currentPositionMs: Long, durationMs: Long): Boolean {
        if (!isEnabled() || isActive || durationMs <= 0) return false
        val remainingMs = durationMs - currentPositionMs
        return remainingMs in 1..crossfadeDurationMs
    }

    fun startCrossfade() {
        if (isActive) return
        isActive = true
        onCrossfadeStart?.invoke()

        fadeOutAnimator?.cancel()
        fadeInAnimator?.cancel()

        fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = crossfadeDurationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                onFadeOut?.invoke(value)
            }
            start()
        }

        fadeInAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = crossfadeDurationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                onFadeIn?.invoke(value)
            }
            start()
        }
    }

    fun endCrossfade() {
        fadeOutAnimator?.cancel()
        fadeInAnimator?.cancel()
        fadeOutAnimator = null
        fadeInAnimator = null
        isActive = false
        onCrossfadeEnd?.invoke()
    }

    fun isInCrossfade(): Boolean = isActive

    fun release() {
        endCrossfade()
        onFadeOut = null
        onFadeIn = null
        onCrossfadeStart = null
        onCrossfadeEnd = null
    }
}
