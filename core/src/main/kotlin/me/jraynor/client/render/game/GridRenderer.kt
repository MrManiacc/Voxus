package me.jraynor.client.render.game

import me.jraynor.client.opengl.shader.Shader
import me.jraynor.client.opengl.internal.Vao
import me.jraynor.client.opengl.model.mesh.MeshFactory
import me.jraynor.client.opengl.shader.ShaderData
import me.jraynor.client.render.group.RenderGroupBuilder
import me.jraynor.client.render.systems.AbstractRenderer
import me.jraynor.common.Transform
import me.jraynor.common.asset.Assets
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * This will be used to test the viewport rendering
 */
class GridRenderer : AbstractRenderer() {
    private lateinit var gridModel: Vao
    private lateinit var gridTransform: Matrix4f
    private lateinit var shader: Shader
    private val gridColor = Vector4f(0.2f, 0.2f, 0.2f, 0.65f)

    /**
     * This should initialize our shader
     */
    override fun initialize() {
        master["viewport", RenderGroupBuilder.Priority.HIGH].main(this::renderGrid, RenderGroupBuilder.Priority.NORMAL)
        this.shader = Assets.new<ShaderData, Shader>("basic", true)!!
        this.gridModel = MeshFactory.generateGrid(128, 800f).make().load()
        this.gridTransform = Transform(Vector3f(-400f, 0f, -400f)).matrix()
    }

    /**
     * This will render a triangle in the middle of the world
     */
    private fun renderGrid() {
        doRender { projection, view, pos ->
            val light = lights.get(tagManager.getEntity("test_light"))
            shader.start()
            shader.loadVec4("material.color", gridColor)
            light.load("light", shader)
            shader.loadMat4("viewMatrix", view)
            shader.loadMat4("projectionMatrix", projection)
            shader.loadMat4("modelMatrix", gridTransform)
            shader.loadVec3("camera", pos)
            gridModel.draw(true)
            shader.stop()
        }
    }
}