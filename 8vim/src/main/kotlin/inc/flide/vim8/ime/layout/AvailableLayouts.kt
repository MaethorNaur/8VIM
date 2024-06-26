package inc.flide.vim8.ime.layout

import android.content.Context
import arrow.core.elementAtOrNone
import arrow.core.firstOrNone
import arrow.core.getOrNone
import inc.flide.vim8.AppPrefs
import inc.flide.vim8.appPreferenceModel

class AvailableLayouts(private val layoutLoader: LayoutLoader, private val context: Context) {
    private val prefs: AppPrefs by appPreferenceModel()
    private val defaultIndex: Int
    private val embeddedLayoutsSize: Int
    private val layoutsWithKeyboardData: MutableMap<Layout<*>, String> = linkedMapOf()

    val displayNames: List<String>
        get() = layoutsWithKeyboardData.values.toList()
    var index = -1
        private set

    init {
        val embeddedLayoutsWithName = embeddedLayouts(layoutLoader, context)
        embeddedLayoutsSize = embeddedLayoutsWithName.size
        layoutsWithKeyboardData.putAll(embeddedLayoutsWithName)
        defaultIndex =
            layoutsWithKeyboardData.keys.indexOf(prefs.layout.current.default as EmbeddedLayout)
        reloadCustomLayouts()

        prefs.layout.custom.history.observe {
            if (it.size > layoutsWithKeyboardData.size - embeddedLayoutsSize) {
                reloadCustomLayouts()
            }
        }
    }

    private fun removeFromHistory(path: String) {
        val historyPref = prefs.layout.custom.history
        val history = LinkedHashSet(historyPref.get())
        history.remove(path)
        historyPref.set(history)
        prefs.layout.current.reset()
        layoutsWithKeyboardData
            .toList()
            .firstOrNone { (layout, _) -> layout.path.toString() == path }
            .onSome { (layout, _) ->
                layoutsWithKeyboardData.remove(layout)
            }
        findIndex()
    }

    fun reloadCustomLayouts() {
        listCustomLayoutHistory()
        findIndex()
    }

    fun updateKeyboardData(layout: Layout<*>): Boolean {
        return layoutsWithKeyboardData
            .getOrNone(layout)
            .flatMap { layout.loadKeyboardData(layoutLoader, context).getOrNone() }
            .onSome {
                prefs.layout.current.set(layout)
            }
            .isSome()
    }

    fun selectLayout(which: Int) {
        layoutsWithKeyboardData.keys.elementAtOrNone(which)
            .onSome { layout ->
                layout
                    .loadKeyboardData(layoutLoader, context)
                    .getOrNone()
                    .onNone { removeFromHistory(layout.path.toString()) }
                    .onSome {
                        prefs.layout.current.set(layout)
                        index = which
                    }
                    .onNone {
                        prefs.layout.current.reset()
                    }
            }
    }

    private fun listCustomLayoutHistory() {
        val uris = LinkedHashSet(prefs.layout.custom.history.get())
        val customLayouts = layoutsWithKeyboardData
            .toList()
            .filter { it.first is CustomLayout }
            .toMap()

        customLayouts
            .filter { !uris.contains(it.key.toString()) }
            .forEach { (layout, _) -> layoutsWithKeyboardData.remove(layout) }

        uris.toList().flatMap { customLayoutUriString ->
            val layout = customLayoutUriString.toCustomLayout()
            if (customLayouts.containsKey(layout)) {
                emptyList()
            } else {
                layout.loadKeyboardData(layoutLoader, context)
                    .getOrNone()
                    .filterNot { it.totalLayers == 0 }
                    .map { layout to it.toString() }
                    .onNone { uris.remove(customLayoutUriString) }
                    .toList()
            }
        }.let { layoutsWithKeyboardData.putAll(it) }
        prefs.layout.custom.history.set(uris)
    }

    private fun findIndex() {
        index = layoutsWithKeyboardData.keys.indexOf(prefs.layout.current.get())
        if (index == -1) {
            index = defaultIndex
            prefs.layout.current.reset()
        }
    }
}
