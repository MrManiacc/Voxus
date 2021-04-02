package me.jraynor.client.render.editor

import com.artemis.*
import com.artemis.managers.TagManager
import com.artemis.utils.Bag
import imgui.ImGui
import imgui.flag.ImGuiStyleVar
import me.jraynor.client.render.RenderMaster
import me.jraynor.client.render.game.GameLayer
import me.jraynor.client.window.WindowSystem
import me.jraynor.client.render.group.RenderGroupBuilder
import me.jraynor.client.render.systems.AbstractRenderer
import me.jraynor.client.render.util.EditorComponent
import me.jraynor.client.render.util.Gui
import net.mostlyoriginal.api.system.core.PassiveSystem
import org.lwjgl.opengl.GL11

/**
 * This will render all of the layered viewport
 */
class EditorLayer() : EntitySystem(Aspect.one()) {
    private val fillBag = Bag<Component>()
    private var activeComponent: Component? = null
    private var activeEntity: Entity? = null
    private lateinit var window: WindowSystem
    private lateinit var master: RenderMaster
    private lateinit var gameLayer: GameLayer
    private lateinit var tagManager: TagManager

    /**Quick accessor **/
    val gui: Gui
        get() = window.gui

    override fun initialize() {
        master["imgui", RenderGroupBuilder.Priority.LOW].pre(this::begin)
        master["imgui"].post(this::end)
        master["imgui"].main(this::renderEntities)
        master["imgui"].main(this::renderComponent)
        master["imgui"].main(this::blitViewport)
    }

    /**
     * This is just to keep it clean, this could go inside the process system.
     */
    override fun begin() {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glClearColor(0.2423f, 0.4223f, 0.7534f, 1.0f)
        val size = window.getFramebufferSize()
        GL11.glViewport(0, 0, size.x, size.y)
        gui.startFrame()
        gui.dockspace("editor")
    }

    /**
     * This will finish the rendering of the
     */
    override fun end() {
        gui.endFrame()
    }

    /**
     * This attempts to blit the viewport render fbo layer in toto the imgui viewport
     */
    private fun blitViewport() {
        with(window.gui) {
            viewport {
                val size = ImGui.getWindowSize()
                val pos = ImGui.getWindowPos()
                val fbo = gameLayer.fbo
                val id = fbo.textureId ?: return@viewport
                ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
                fbo.resize((size.x - 20).toInt(), (size.y - 35).toInt())
                ImGui.image(id, fbo.width.toFloat(), fbo.height.toFloat(), 0f, 1f, 1f, 0f)
                ImGui.popStyleVar()
            }
        }
    }

    /**
     * This should render the entity
     */
    private fun renderEntities() {
        with(window.gui) {
            entities {
                entities.forEach { entity ->
                    val tag = tagManager.getTag(entity)
                    val text = if (tag != null) "[${entity.id}]$tag->" else "[${entity.id}]entity->"
                    if (ImGui.treeNode(text)) {
                        ImGui.separator()
                        fillBag.clear()
                        val components = entity.getComponents(fillBag)
                        var i = 0
                        components.forEach lit@{
                            ImGui.text(it::class.java.simpleName.toString())
                            ImGui.sameLine()
                            if (ImGui.button("edit##${entity.id + i++}")) {
                                activeComponent = it
                                activeEntity = entity
                                println("selected ${activeComponent!!::class.java.simpleName} for entity ${entity.id}")
                            }
                        }
                        ImGui.treePop()
                    }
                }
            }
        }
    }

    /**
     * This will render a component with the owner if selected
     */
    private fun renderComponent() {
        val owner = this.activeEntity ?: return
        val component = this.activeComponent ?: return
        with(window.gui) {
            components {
                ImGui.text("Owner id: ${owner.id}")
                ImGui.text("Component: ${component::class.java.simpleName}")
                ImGui.separator()
                if (component is EditorComponent) {
                    component.render()
                }
            }
        }
    }

    override fun processSystem() {
    }

    override fun checkProcessing(): Boolean {
        return false
    }
}