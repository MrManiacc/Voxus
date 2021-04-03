package me.jraynor.client.opengl.camera

import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.Entity
import com.artemis.managers.TagManager
import com.artemis.systems.EntityProcessingSystem
import me.jraynor.client.input.Input
import me.jraynor.client.window.WindowSystem
import me.jraynor.common.Transform
import me.jraynor.common.util.degrees
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * This object stores all of the camera related systems
 */
class FirstPersonSystem() :
    EntityProcessingSystem(
        Aspect.all(
            Transform::class.java,
            FirstPersonCamera::class.java,
            CameraSettings::class.java
        )
    ) {
    private lateinit var window: WindowSystem
    private lateinit var tagManager: TagManager
    private lateinit var transforms: ComponentMapper<Transform>
    private lateinit var settings: ComponentMapper<CameraSettings>
    private var mouseX: Double = 0.0
    private var mouseY: Double = 0.0

    override fun initialize() {
        if (!tagManager.isRegistered("local_player")) {
            val player = world?.createEntity()
            player?.edit()?.add(FirstPersonCamera())
            player?.edit()?.add(CameraSettings())
            player?.edit()?.add(Transform(Vector3f(0f, 25f, 0f)))
            tagManager.register("local_player", player)
        }
    }

    /**
     * Process a entity this system is interested in.
     * @param e
     * the entity to process
     */
    override fun process(e: Entity?) {
        if (Input.keyPressed(Input.KEY_R)) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        } else if (Input.keyPressed(Input.KEY_T)) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }

        val transform = transforms.get(e)

        val setting = settings.get(e)
        if (Input.keyPressed(Input.KEY_ENTER))
            Input.grabbed = !Input.grabbed

        if (Input.grabbed) {
            if (Input.keyPressed(Input.KEY_ESCAPE))
                Input.grabbed = false
            val startMousePos = window.mousePos
            val currentX = startMousePos.x
            val currentY = startMousePos.y
            val dx = currentX - mouseX
            val dy = currentY - mouseY
            this.mouseX = currentX
            this.mouseY = currentY

            val ox = Input.dy.toFloat()
            val oy = Input.dx.toFloat()
            transform.rotate(
                (((dy.toFloat() / setting.verticalSensitivity) * world.delta)),
                ((dx.toFloat() / setting.horizontalSensitivity) * world.delta),
                0f
            )
            val rads = (transform.rotation!!.y).degrees
            val rads90 = (transform.rotation!!.y + 90f).degrees
            if (Input.keyDown(Input.KEY_A)) {
                transform.translate(
                    x = (-cos(rads) * setting.walkSpeed) * world.delta,
                    z = (-sin(rads) * setting.walkSpeed) * world.delta
                )
            }
            if (Input.keyDown(Input.KEY_D)) {
                transform.translate(
                    x = (cos(rads) * setting.walkSpeed) * world.delta,
                    z = (sin(rads) * setting.walkSpeed) * world.delta
                )
            }
            if (Input.keyDown(Input.KEY_W)) {
                transform.translate(
                    x = (cos(rads90) * setting.walkSpeed) * world.delta,
                    z = (sin(rads90) * setting.walkSpeed) * world.delta
                )
            }
            if (Input.keyDown(Input.KEY_S)) {
                transform.translate(
                    x = (-cos(rads90) * setting.walkSpeed) * world.delta,
                    z = (-sin(rads90) * setting.walkSpeed) * world.delta
                )
            }
            if (Input.keyDown(Input.KEY_LEFT_CONTROL)) {
                transform.translate(
                    y = -setting.walkSpeed * world.delta
                )
            }
            if (Input.keyDown(Input.KEY_SPACE)) {
                transform.translate(
                    y = setting.walkSpeed * world.delta
                )
            }
        }
    }
}
