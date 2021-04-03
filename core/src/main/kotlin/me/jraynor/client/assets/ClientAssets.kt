package me.jraynor.client.assets

import me.jraynor.client.opengl.shader.Shader
import me.jraynor.client.opengl.shader.ShaderFormat
import me.jraynor.manager.AssetTypeManager
import net.mostlyoriginal.api.system.core.PassiveSystem

/**
 * This will register all of our assets for the client.
 */
class ClientAssets : PassiveSystem() {
    private lateinit var typeManager: AssetTypeManager

    /**
     * This will register all of our asset types for the clent.
     */
    override fun initialize() {
        typeManager.registerAssetType(
            Shader::class.java,
            { urn, type, data -> Shader(urn, type, data) },
            ShaderFormat(),
            "shaders"
        )

    }
}