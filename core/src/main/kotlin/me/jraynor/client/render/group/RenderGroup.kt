package me.jraynor.client.render.group

import me.jraynor.client.render.util.IRenderable

/**
 * This will store all of the immutable renderables. They should be built fro the [RenderGroupBuilder]
 */
class RenderGroup(
    val name: String,
    val priority: Int,
    private val preRenders: List<IRenderable> = ArrayList(),
    private val mainRenders: List<IRenderable> = ArrayList(),
    private val postRenders: List<IRenderable> = ArrayList(),
) {
    /**
     * This will render all of the pre renderables, because they're sorted already thanks to the [RenderGroupBuilder]
     */
    fun renderPre() {
        preRenders.forEach(IRenderable::render)
    }

    /**
     * This will render all of the
     */
    fun renderMain() {
        mainRenders.forEach(IRenderable::render)
    }

    /**
     * This will render all of the
     */
    fun renderPost() {
        postRenders.forEach(IRenderable::render)
    }

}
