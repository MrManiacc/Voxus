package me.jraynor.client.render

import com.artemis.BaseSystem
import com.artemis.Manager
import me.jraynor.client.render.group.RenderGroup
import me.jraynor.client.render.group.RenderGroupBuilder

/**
 * This class oversees all rendering. It will store a collection of units that are to be considered the "renderable
 * methods" they will be registered upon startup and will subscribe to the correct units.
 */
class RenderMaster : BaseSystem() {
    /**This will be used as a way for registering new renderables**/
    private val renderGroupBuilders = HashMap<String, RenderGroupBuilder>()
    private val renderGroups = ArrayList<RenderGroup>()

    /**
     * This will get or create a new render group builder
     */
    operator fun get(name: String, priority: Int = RenderGroupBuilder.Priority.NORMAL): RenderGroupBuilder {
        if (!renderGroupBuilders.containsKey(name))
            renderGroupBuilders[name] = RenderGroupBuilder()
        val builder = renderGroupBuilders[name]!!
        builder.priority = priority
        return builder
    }

    /**
     * This should build all of the renderGroupBuilders
     */
    override fun initialize() {
        renderGroupBuilders.forEach { (name, builder) ->
            renderGroups.add(builder.build(name))
        }
        renderGroups.sortBy { it.priority }
    }


    /**
     * Here we will render our system, since our groups are sorted.
     */
    override fun processSystem() {
        renderGroups.forEach {
            it.renderPre() //Pre should be per render group
            it.renderMain() //Main render should also be per render group
            it.renderPost() //Post should again be per render grup
        }
    }

}