package inc.flide.vim8.ime.ui.floating

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import arrow.core.Option
import arrow.core.none
import arrow.core.some
import inc.flide.vim8.lib.android.offset

internal fun Rect.boundRectIntoScreen(screenSize: Size): Option<Rect> = if (!isEmpty) {
    val size = size.coerceIn(screenSize)
    val offset = offset.coerceIn(size, screenSize).let {
        if (it.y == -size.height) {
            it.copy(y = size.height * -0.9f)
        } else {
            it
        }
    }
    if (size != this.size || offset != this.offset) {
        Rect(offset, size).some()
    } else {
        this.some()
    }
} else {
    none()
}
