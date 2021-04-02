package me.jraynor.client.render.game

import me.jraynor.client.opengl.internal.Fbo
import me.jraynor.client.window.WindowSystem
import me.jraynor.client.render.group.RenderGroupBuilder
import me.jraynor.client.render.systems.AbstractRenderer
import org.lwjgl.opengl.GL11

/**
 * This will render all of the layered viewport. In the future i would like to bind an fbo to this
 */
class GameLayer : AbstractRenderer() {
    var window: WindowSystem? = null//This should be injected
    val fbo: Fbo = Fbo(1920, 1080, true)

    /**
     * This will setup our initial viewport vbo
     */
    override fun initialize() {
        val builder = master["viewport", RenderGroupBuilder.Priority.HIGH]
        builder.pre(this::begin)
        builder.post(this::end)
        fbo.initialize()
    }

    /**
     * This is just to keep it clean, this could go inside the process system.
     */
    override fun begin() {
        window ?: return
        fbo.bind()
        GL11.glClearColor(0.2423f, 0.4223f, 0.7534f, 1.0f)
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glViewport(0, 0, fbo.width, fbo.height)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glCullFace(GL11.GL_BACK)
        val camera = cameras.get(localPlayer)
        camera.aspect = fbo.width.toFloat() / fbo.height.toFloat()
    }

    /**
     * Unbind our fbo at the end of rendering
     */
    override fun end() {
        fbo.unbindFrameBuffer()
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }

    /**
     * We need to dispose of the frame buffer when done
     */
    override fun dispose() {
        fbo.dispose()
    }

}