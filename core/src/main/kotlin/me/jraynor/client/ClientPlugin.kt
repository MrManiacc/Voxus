package me.jraynor.client

import com.artemis.ArtemisPlugin
import com.artemis.BaseSystem
import com.artemis.WorldConfigurationBuilder
import me.jraynor.Voxus
import me.jraynor.client.opengl.camera.FirstPersonSystem
import me.jraynor.client.render.RenderMaster
import me.jraynor.client.window.WindowSystem
import me.jraynor.client.render.game.GridRenderer
import me.jraynor.client.render.editor.EditorLayer
import me.jraynor.client.render.game.GameLayer
import me.jraynor.client.render.game.TestRenderEnvironment

/***
 * This class will keep track of the rendering of the world. It's only for the client so it will only be registered if
 * we're on the client
 */
class ClientPlugin(private val voxus: Voxus) : ArtemisPlugin {

    /**
     * Here we will setup everything related to rendering.
     */
    override fun setup(builder: WorldConfigurationBuilder) {
        builder.with(
            WorldConfigurationBuilder.Priority.HIGHEST,
            WindowSystem(voxus = voxus)
        )
        builder.with(GameLayer())
        builder.with(TestRenderEnvironment())
        builder.with(EditorLayer())
        builder.with(GridRenderer())
        builder.with(FirstPersonSystem())
        builder.with(WorldConfigurationBuilder.Priority.LOW, RenderMaster())
        builder.with(WorldConfigurationBuilder.Priority.LOWEST, object : BaseSystem() {
            private lateinit var windowSystem: WindowSystem
            override fun processSystem() {
                windowSystem.poll()
            }
        })//Lowest priority because we want it to be called last, because that's when the poll.

    }

}