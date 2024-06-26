package inc.flide.vim8.ime.input

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.KeyEvent
import androidx.compose.runtime.staticCompositionLocalOf
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.systemServiceOrNull
import inc.flide.vim8.lib.android.systemVibratorOrNull
import inc.flide.vim8.lib.android.vibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

val LocalInputFeedbackController =
    staticCompositionLocalOf<InputFeedbackController> { error("not init") }

class InputFeedbackController private constructor(ims: InputMethodService) {
    companion object {
        fun new(ims: InputMethodService) = InputFeedbackController(ims)
    }

    private val prefs by appPreferenceModel()
    private val audioManager = ims.systemServiceOrNull(AudioManager::class)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val vibrator = ims.systemVibratorOrNull()

    fun keyPress(keySound: Int = 0, repeat: Boolean) {
        val (haptic, sound) = if (repeat) (0.05 to 0.4) else (1.0 to 1.0)
        if (prefs.inputFeedback.hapticEnabled.get()) performHapticFeedback(haptic)
        if (prefs.inputFeedback.soundEnabled.get()) performAudioFeedback(keySound(keySound), sound)
    }

    fun sectorCross() {
        if (prefs.inputFeedback.hapticEnabled.get() &&
            prefs.inputFeedback.hapticSectorCross.get() > 0
        ) {
            performHapticFeedback(prefs.inputFeedback.hapticSectorCross.get() / 100.0)
        }
        if (prefs.inputFeedback.soundEnabled.get() &&
            prefs.inputFeedback.soundSectorCross.get() > 0
        ) {
            performAudioFeedback(
                AudioManager.FX_KEYPRESS_STANDARD,
                prefs.inputFeedback.soundSectorCross.get() / 100.0
            )
        }
    }

    private fun keySound(keyCode: Int): Int {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> AudioManager.FX_KEYPRESS_DELETE
            KeyEvent.KEYCODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
    }

    fun performHapticFeedback(factor: Double) {
        if (vibrator == null) return
        scope.launch {
            vibrator.vibrate(
                duration = 50,
                strength = 50,
                factor = factor
            )
        }
    }

    internal fun performAudioFeedback(keySound: Int, factor: Double) {
        val prefVolume = prefs.inputFeedback.soundVolume.get()
        val volume = (
            if (prefVolume == 0 && audioManager != null) {
                val volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                val maxVolumeLevel = audioManager.getStreamMaxVolume(
                    AudioManager.STREAM_MUSIC
                ).toFloat()
                volumeLevel / maxVolumeLevel
            } else {
                prefVolume / 100.0f
            }
            ) * factor

        if (volume in 0.01..1.0) {
            audioManager?.playSoundEffect(keySound, volume.toFloat())
        }
    }
}
