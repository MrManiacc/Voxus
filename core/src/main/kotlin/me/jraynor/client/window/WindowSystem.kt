package me.jraynor.client.window

import com.artemis.BaseSystem
import me.jraynor.Voxus
import me.jraynor.client.input.Input
import me.jraynor.client.render.util.Gui
import me.jraynor.common.util.Vec2DfBuf
import me.jraynor.common.util.Vec2fBuf
import me.jraynor.common.util.Vec2iBuf
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector2i
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.Platform

/**
 * This will create and handle the window it will handle the initialization of the imgui related stuff as well
 */
class WindowSystem(
    var width: Int = 1920,
    var height: Int = 1080,
    val title: String = "window",
    val vsync: Boolean = false,
    val resizable: Boolean = true,
    private val voxus: Voxus
) : BaseSystem() {
    var handle: Long? = null
    private val windowSize = Vec2iBuf()
    private val framebufferSize = Vec2iBuf()
    private val framebufferScale = Vec2fBuf()
    private val mousePosBuffer = Vec2DfBuf()
    private val windowPos = Vec2iBuf()
    private var grabbed = false
    val gui: Gui = Gui(this)

    /**
     * Here we will create our window
     */
    override fun initialize() {
        setupHints()
        setupWindow()
        setupCallbacks()
        gui.init()
    }
    /**
     * This will get the mouse position
     */
    val mousePos: Vector2d
        get() {
            handle?.let { glfwGetCursorPos(it, mousePosBuffer.x, mousePosBuffer.y) }
            return mousePosBuffer.get()
        }
    /**
     * This allows us access to the gui
     */
    fun renderWith(unit: (Gui) -> Unit) {
        unit(gui)
    }

    /**
     * Here we check to see if we're running, based upon the glfwwindow state. If we're not we want to gracefully
     * shutdown the client main.
     */
    override fun processSystem() {
        if (isCloseRequested())
            voxus.stop(false)
    }

    /**
     * This will setup all of the default hints
     */
    private fun setupHints() {
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation

        GLFW.glfwWindowHint(
            GLFW.GLFW_RESIZABLE,
            if (resizable) GLFW.GLFW_TRUE else GLFW.GLFW_FALSE
        ) // the window will be resizable

        if (Platform.get() === Platform.MACOSX) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        }
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4)
        handle = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        if (handle == 0L) throw RuntimeException("Failed to create the GLFW window")
    }

    /**
     * This will setup the callbacks
     */
    private fun setupCallbacks() {
        GLFW.glfwSetFramebufferSizeCallback(handle!!, object : GLFWFramebufferSizeCallback() {
            /**
             * Here we want to call gl view port to make sure the viewport,
             * is matching the new frame size
             */
            override fun invoke(window: Long, width: Int, height: Int) {
                GL11.glViewport(0, 0, width, height)
                framebufferSize.widthBuffer[0] = width
                framebufferSize.heightBuffer[0] = height
            }
        })

        GLFW.glfwSetCursorPosCallback(handle!!, object : GLFWCursorPosCallback() {
            /**
             * Here we just want to update our [Input] object
             */
            override fun invoke(window: Long, xpos: Double, ypos: Double) {
                Input.setMousePosition(xpos, ypos)
            }
        })
        GLFW.glfwSetMouseButtonCallback(handle!!, object : GLFWMouseButtonCallback() {
            /**
             * Here we just want to update our [Input] object
             */
            override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
                Input.setMouseState(button, action)
            }
        })

        GLFW.glfwSetKeyCallback(handle!!, object : GLFWKeyCallback() {
            /**
             * Here we just want to update our [Input] object
             */
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                Input.setKeyState(key, action)
            }
        })
    }



    /**
     * This will finish the window setup
     */
    private fun setupWindow() {
        GLFW.glfwMakeContextCurrent(handle!!)
        GLFW.glfwSwapInterval(if (vsync) 1 else 0)
        GLFW.glfwShowWindow(handle!!)
        GL.createCapabilities()
    }

    /**
     * returns true if the window has requested close
     */
    fun isCloseRequested(): Boolean {
        return GLFW.glfwWindowShouldClose(handle!!)
    }

    /**
     * This will update the input for the window
     */
    fun poll() {
        processInput()
        GLFW.glfwSwapBuffers(handle!!);
        GLFW.glfwPollEvents();
    }

    /**
     * This will manage the input class
     */
    private fun processInput() {
        if (Input.grabbed) {
            if (!grabbed)
                setGrabbed(true)
        } else {
            if (grabbed) {
//                val size = framebufferSize.get()
                setGrabbed(false)
//                GLFW.glfwSetCursorPos(handle!!, size.x / 2.0, size.y / 2.0)
            }
        }
        Input.reset()
    }

    /**
     * This will dispose of the window
     */
    override fun dispose() {
        GLFW.glfwSetWindowShouldClose(handle!!, true);
        Callbacks.glfwFreeCallbacks(handle!!);
        GLFW.glfwDestroyWindow(handle!!);
        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        gui.destroy()
    }

    /**
     * @return returns true if the mouse is grabbed
     */
    fun isGrabbed(): Boolean {
        return GLFW.glfwGetInputMode(handle!!, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED
    }

    /**
     * @param grabbed the grab state of the mouse
     */
    fun setGrabbed(grabbed: Boolean) {
        GLFW.glfwSetInputMode(
            handle!!,
            GLFW.GLFW_CURSOR,
            if (grabbed) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL
        )
        this.grabbed = grabbed
    }

    /**
     * @return returns true if the glfw window is focused
     */
    fun isFocused(): Boolean {
        return GLFW.glfwGetInputMode(handle!!, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED
    }

    /**
     * @param focused the new focused value
     */
    fun setFocused(focused: Boolean) {
        GLFW.glfwSetInputMode(
            handle!!,
            GLFW.GLFW_CURSOR,
            if (focused) GLFW.GLFW_CURSOR_DISABLED else GLFW.GLFW_CURSOR_NORMAL
        )
    }

    /**
     * @returns the size of the window
     */
    fun getFramebufferSize(): Vector2i {
        glfwGetWindowSize(handle!!, framebufferSize.widthBuffer, framebufferSize.heightBuffer)
        return framebufferSize.get()
    }

    /**
     * @returns the size of the frame buffer
     */
    fun getWindowSize(): Vector2i {
        glfwGetFramebufferSize(handle!!, windowSize.widthBuffer, windowSize.heightBuffer)
        return windowSize.get()
    }

    /**
     * Gets the window positions
     */
    fun getWindowPos(): Vector2i {
        GLFW.glfwGetWindowPos(handle!!, windowPos.widthBuffer, windowPos.heightBuffer)
        return windowPos.get()
    }

    /**
     * @returns the size of the frame buffer
     */
    fun getWindowScale(): Vector2f {
        glfwGetWindowContentScale(handle!!, framebufferScale.widthBuffer, framebufferScale.heightBuffer)
        return framebufferScale.get()
    }

}