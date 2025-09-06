package com.tecvo.taxi.ui.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.tecvo.taxi.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DialogManager centralizes all dialog-related functionality in the application.
 * It provides reusable dialog templates for permissions, loading overlays, and other UI feedback.
 */
@Singleton
class DialogManager @Inject constructor() {
    // Reference to current loading dialog
    private var currentLoadingDialog: Dialog? = null
    private var dotsAnimatorSet: AnimatorSet? = null

    /**
     * Shows a modern loading overlay with circular progress indicator
     */
    fun showLoadingOverlay(activity: Activity, message: String, description: String? = null) {
        activity.runOnUiThread {
            val loadingDialog = Dialog(activity)
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog.setCancelable(false)
            loadingDialog.setContentView(R.layout.dialog_loading)
            loadingDialog.window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                attributes.windowAnimations = android.R.style.Animation_Dialog
                setDimAmount(0.0f) // No dimming for minimalist effect
                setGravity(Gravity.CENTER)
                // Clear flags for completely transparent experience
                clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            }
            // Set the message text
            loadingDialog.findViewById<TextView>(R.id.loading_message).text = message
            // Set description text if provided
            description?.let {
                val descriptionView = loadingDialog.findViewById<TextView>(R.id.loading_description)
                descriptionView.text = it
                descriptionView.visibility = android.view.View.VISIBLE
            } ?: run {
                // Hide description if not provided
                loadingDialog.findViewById<TextView>(R.id.loading_description).visibility =
                    android.view.View.GONE
            }
            loadingDialog.show()
            
            // Start animated dots
            startDotsAnimation(loadingDialog)
            
            // Store reference to dismiss later
            currentLoadingDialog = loadingDialog
        }
    }

    /**
     * Hides the currently displayed loading overlay
     */
    fun hideLoadingOverlay(activity: Activity) {
        activity.runOnUiThread {
            // Stop animations
            dotsAnimatorSet?.cancel()
            dotsAnimatorSet = null
            
            currentLoadingDialog?.dismiss()
            currentLoadingDialog = null
        }
    }

    /**
     * Creates breathing animation for the loading dots
     */
    private fun startDotsAnimation(dialog: Dialog) {
        try {
            val dot1 = dialog.findViewById<View>(R.id.dot1)
            val dot2 = dialog.findViewById<View>(R.id.dot2)
            val dot3 = dialog.findViewById<View>(R.id.dot3)

            if (dot1 != null && dot2 != null && dot3 != null) {
                // Create scale animations for each dot
                val animator1 = createDotAnimator(dot1, 0)
                val animator2 = createDotAnimator(dot2, 150)
                val animator3 = createDotAnimator(dot3, 300)

                dotsAnimatorSet = AnimatorSet().apply {
                    playTogether(animator1, animator2, animator3)
                    start()
                }
            }
        } catch (e: Exception) {
            // Fail silently if animation setup fails
        }
    }

    /**
     * Creates a breathing scale animation for a single dot
     */
    private fun createDotAnimator(dot: View, startDelay: Long): AnimatorSet {
        val scaleXDown = ObjectAnimator.ofFloat(dot, "scaleX", 1.0f, 0.3f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val scaleYDown = ObjectAnimator.ofFloat(dot, "scaleY", 1.0f, 0.3f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val scaleXUp = ObjectAnimator.ofFloat(dot, "scaleX", 0.3f, 1.0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val scaleYUp = ObjectAnimator.ofFloat(dot, "scaleY", 0.3f, 1.0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        val downSet = AnimatorSet().apply {
            playTogether(scaleXDown, scaleYDown)
        }
        
        val upSet = AnimatorSet().apply {
            playTogether(scaleXUp, scaleYUp)
        }

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(downSet, upSet)
        animatorSet.startDelay = startDelay
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // Restart the animation for continuous looping
                if (currentLoadingDialog?.isShowing == true) {
                    animatorSet.start()
                }
            }
        })
        return animatorSet
    }

    /**
     * Displays a standardized toast message
     */
    fun showToast(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            // Fail silently
        }
    }

}