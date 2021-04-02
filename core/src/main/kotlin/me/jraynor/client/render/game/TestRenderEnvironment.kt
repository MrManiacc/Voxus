package me.jraynor.client.render.game

import com.artemis.Aspect
import com.artemis.ComponentMapper
import me.jraynor.client.opengl.light.Light
import me.jraynor.client.opengl.model.mesh.MeshFactory
import me.jraynor.client.opengl.model.Model
import me.jraynor.client.opengl.model.ModelData
import me.jraynor.client.opengl.model.material.Material
import me.jraynor.client.opengl.shader.Shader
import me.jraynor.client.opengl.shader.ShaderData
import me.jraynor.client.render.group.RenderGroupBuilder
import me.jraynor.client.render.systems.AbstractEntityRenderer
import me.jraynor.common.Transform
import me.jraynor.common.asset.Assets
import me.jraynor.util.radians
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import java.text.DecimalFormat
import java.util.*

/**
 * This is used to testing lighting
 *
 * TODO: figure out a better way to create test objects
 */
class TestRenderEnvironment :
    AbstractEntityRenderer(Aspect.all(Model::class.java, Transform::class.java, Material::class.java)) {
    private lateinit var shader: Shader
    private val position = Vector3f()

    /**
     * This should initialize our shader
     */
    override fun initialize() {
        master["viewport", RenderGroupBuilder.Priority.HIGH].main(this::renderGrid, RenderGroupBuilder.Priority.HIGH)
        this.shader = Assets.new<ShaderData, Shader>("voxel", true)!!
        val voxTest = Assets.new<ModelData, Model>("castle", true)!!
        world.createEntity().edit()
            .add(voxTest)
            .add(Transform(Vector3f(10f, 5f, 20f), scale = Vector3f(100f), rotation = Vector3f(270f, 0f, 0f)))
            .add(Material(Vector4f(0.3f, 0.7f, 0.321f, 1f)))
        tagManager.register(
            "test_light", world.createEntity()
                .edit()
                .add(Light(Vector3f(-10f, 40f, 25f), Vector3f(1f)))
                .entity

        )
    }

    /**
     * This will render a triangle in the middle of the world
     */
    private fun renderGrid() {
        doRender { projection, view, pos ->
            shader.start()
            shader.loadMat4("projectionMatrix", projection)
            shader.loadMat4("viewMatrix", view)
            entities.forEach {
                val light = lights.get(tagManager.getEntity("test_light"))
                val transform = transforms.get(it)!!
                val model = models.get(it)!!
                val material = materials.get(it)!!
                material.load("material", shader)
                light.load("light", shader)
                shader.loadVec3("camera", pos)
                shader.loadMat4("modelMatrix", transform.matrix(true))
                model.render()
            }
            shader.stop()
        }
    }
}