package inc.flide.vim8.ime.clipboard

import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import inc.flide.vim8.appPreferenceModel
import inc.flide.vim8.lib.android.systemService

class ClipboardManager(context: Context) : OnPrimaryClipChangedListener {
    private val prefs by appPreferenceModel()
    private val systemClipboardManager =
        context.systemService(android.content.ClipboardManager::class)
    private val _history = MutableLiveData<List<String>>()
    val history: LiveData<List<String>> get() = _history

    init {
        _history.value = clipHistoryFromHistory()
        systemClipboardManager.addPrimaryClipChangedListener(this)
        prefs.clipboard.maxHistory.observe {
            updateHistory(
                prefs.clipboard.history.get().asSequence(),
                it
            )
        }
    }

    override fun onPrimaryClipChanged() {
        systemClipboardManager.primaryClip?.let {
            if (prefs.clipboard.enabled.get()) {
                val clip = it.getItemAt(0)
                val newClip = clip.text.toString()
                addClipToHistory(newClip)
            }
        }
    }

    private fun getTimestampFromTimestampedClip(timestampedClip: String): Long {
        val timestampString = timestampedClip.substring(1, timestampedClip.indexOf("] "))
        return timestampString.toLong()
    }

    private fun getClipFromTimestampedClip(timestampedClip: String): String {
        return timestampedClip.substring(timestampedClip.indexOf("] ") + 2)
    }

    private fun addClipToHistory(newClip: String) {
        if (newClip.isNotEmpty()) {
            val timestampedClip = "[${System.currentTimeMillis()}] $newClip"
            updateHistory(
                prefs.clipboard.history.get().asSequence() + timestampedClip,
                prefs.clipboard.maxHistory.get()
            )
        }
    }

    private fun updateHistory(history: Sequence<String>, maxHistory: Int) {
        val result = history
            .fold(mapOf<String, Long>()) { acc, clip ->
                val cleanedClip = getClipFromTimestampedClip(clip)
                val timestamp = getTimestampFromTimestampedClip(clip)
                val toAdd = mapOf(cleanedClip to timestamp)
                acc + (
                    acc[cleanedClip]?.let {
                        if (timestamp > it) {
                            toAdd
                        } else {
                            emptyMap()
                        }
                    } ?: toAdd
                    )
            }
            .toList()
            .sortedByDescending { it.second }
            .take(maxHistory)
        result.map { "[${it.second}] ${it.first}" }
            .toSet()
            .let {
                prefs.clipboard.history.set(it)
            }
        _history.postValue(result.map { it.first })
    }

    private fun clipHistoryFromHistory(): List<String> = prefs.clipboard.history.get()
        .toList()
        .sortedByDescending { getTimestampFromTimestampedClip(it) }
        .map { getClipFromTimestampedClip(it) }
}
