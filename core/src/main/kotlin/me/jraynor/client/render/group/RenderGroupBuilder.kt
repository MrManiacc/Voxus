package me.jraynor.client.render.group

import com.artemis.annotations.UnstableApi
import me.jraynor.client.render.util.IRenderable
import kotlin.collections.ArrayList

/**
 * This will store the group of renderables, as well as their begin and ends.
 */
class RenderGroupBuilder(
    var priority: Int = Priority.NORMAL,
    private val preRenders: MutableList<PrioritizedRenderable> = ArrayList(),
    private val renders: MutableList<PrioritizedRenderable> = ArrayList(),
    private val postRenders: MutableList<PrioritizedRenderable> = ArrayList()
) {

    /**
     * This will add a pre renderer that can be sorted
     */
    fun pre(renderable: IRenderable, priority: Int = Priority.NORMAL): RenderGroupBuilder {
        this.preRenders.add(PrioritizedRenderable(renderable, priority))
        return this
    }

    /**
     * This adds a renderable to the main renderables. It will be sorted eventually
     */
    fun main(renderable: IRenderable, priority: Int = Priority.NORMAL): RenderGroupBuilder {
        this.renders.add(PrioritizedRenderable(renderable, priority))
        return this
    }

    /**
     * This adds a post renderable
     */
    fun post(renderable: IRenderable, priority: Int = Priority.NORMAL): RenderGroupBuilder {
        this.postRenders.add(PrioritizedRenderable(renderable, priority))
        return this
    }

    /**
     * This will sort all of the renderables
     */
    fun build(name: String, priority: Int = Priority.NORMAL): RenderGroup {
        return RenderGroup(
            name,
            priority,
            this.preRenders.sortedBy { it.priority }.map { it.renderable },
            this.renders.sortedBy { it.priority }.map { it.renderable },
            this.postRenders.sortedBy { it.priority }.map { it.renderable }
        )
    }

    /**
     * Guideline constants for priority, higher values has more priority. Will probably change.
     */
    @UnstableApi
    object Priority {
        const val LOWEST = Int.MIN_VALUE
        const val LOW = -10000
        const val OPERATIONS = -1000
        const val NORMAL = 0
        const val HIGH = 10000
        const val HIGHEST = Int.MAX_VALUE
    }

    /**
     * This allows us to store the renderable with a priority
     */
    data class PrioritizedRenderable(val renderable: IRenderable, val priority: Int)

}
