package me.jraynor.client.render.systems

import com.artemis.Aspect
import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.artemis.Entity
import com.artemis.managers.TagManager
import me.jraynor.client.opengl.FirstPersonCamera
import me.jraynor.client.opengl.light.Light
import me.jraynor.client.opengl.model.Model
import me.jraynor.client.render.RenderMaster
import me.jraynor.client.render.game.GameLayer
import me.jraynor.common.Transform
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * This allows us to register renderable methods to given groups and later render them
 */
abstract class AbstractRenderer() : BaseSystem() {
    protected lateinit var master: RenderMaster
    protected lateinit var transforms: ComponentMapper<Transform>
    protected lateinit var models: ComponentMapper<Model>
    protected lateinit var cameras: ComponentMapper<FirstPersonCamera>
    protected lateinit var tagManager: TagManager
    protected lateinit var gameLayer: GameLayer
    protected lateinit var lights: ComponentMapper<Light>
    protected val localPlayer: Entity
        get() = tagManager.getEntity("local_player")

    /**
     * This makes it so we can render our renderables in the correct place.
     */
    final override fun processSystem() {
    }

    /**
     * This will allow for easy rendering
     */
    protected fun doRender(renderable: (projection: Matrix4f, view: Matrix4f, viewPos: Vector3f) -> Unit) {
        val localPlayer = tagManager.getEntity("local_player")
        localPlayer ?: return
        val playerCamera = cameras.get(localPlayer)
        val playerTransform = transforms.get(localPlayer)
        val width = gameLayer.fbo.width
        val height = gameLayer.fbo.height
        val viewMatrix = playerCamera.model(playerTransform)
        val projectMatrix = playerCamera.projection((width / height).toFloat())
        renderable(projectMatrix, viewMatrix, playerTransform.position!!)
    }

    /**
     * We don't want to check processing
     */
    override fun checkProcessing(): Boolean {
        return false
    }
}