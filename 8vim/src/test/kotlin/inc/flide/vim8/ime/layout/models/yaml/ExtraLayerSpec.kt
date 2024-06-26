package inc.flide.vim8.ime.layout.models.yaml

import inc.flide.vim8.ime.layout.models.LayerLevel
import inc.flide.vim8.ime.layout.models.yaml.versions.common.ExtraLayer
import inc.flide.vim8.ime.layout.models.yaml.versions.common.toLayerLevel
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class ExtraLayerSpec : FunSpec({
    context("Convert ExtraLayer to LayerLevel") {
        withData(
            nameFn = { "${it.first} -> ${it.second}" },
            ExtraLayer.FIRST to LayerLevel.SECOND,
            ExtraLayer.SECOND to LayerLevel.THIRD,
            ExtraLayer.THIRD to LayerLevel.FOURTH,
            ExtraLayer.FOURTH to LayerLevel.FIFTH,
            ExtraLayer.FIFTH to LayerLevel.SIXTH
        ) { (extraLayer, layerLevel) ->
            extraLayer.toLayerLevel() shouldBe layerLevel
        }
    }
})
