package me.jraynor.client.parse

import assimp.*
import glm_.toInt
import me.jraynor.client.opengl.model.material.Material
import me.jraynor.client.opengl.model.material.MaterialData
import me.jraynor.client.opengl.model.texture.Texture
import org.joml.Vector3f
import resourcePath
import java.io.File
import java.nio.file.Paths

/**
 * This will parse all of the material data from the scene and store it inside the
 */
object MaterialParser {

    /**
     * This will parse the material data
     */
    fun parse(scene: AiScene, materialData: MaterialData) {
        scene.materials.forEach {
            materialData.add(parseMaterial(it))
        }
    }

    /**
     * This will read all of the materials
     */
    private fun parseMaterial(materialIn: AiMaterial): Material {
        val material = Material()
        materialIn.reflectivity?.let { material["reflect"] = it }
        materialIn.shininess?.let { material["shine"] = it }
        materialIn.shininessStrength?.let { material["shine_strength"] = it }
        materialIn.blendFunc?.let { material["blend"] = it }
        materialIn.name?.let { material["name"] = it }
        materialIn.color?.let { parseColor(it, material) }
        materialIn.textures.forEach { parseTexture(it, material) }
        return material
    }

    /**
     * This will parse all of the textures for the material
     */
    private fun parseTexture(textureIn: AiMaterial.Texture, material: Material) {
        val name = textureIn.file ?: return
        val file = File("$resourcePath/models/$name")
        val key = (textureIn.type ?: return).name
        val texture = Texture()
        material[key] = texture
        texture["type"] = textureIn.type!!
        texture["path"] = file
        Material::class.java.getResource("/models/$name")?.toURI()?.let {
            val filePath = Paths.get(it);
            texture.readTexture(filePath)

            texture["uri"] = it
        }

    }

    /**
     * This will parse all of the colors to the material
     */
    private fun parseColor(color: AiMaterial.Color, material: Material) {
        addColor(material, AiTexture.Type.diffuse, color.diffuse)
        addColor(material, AiTexture.Type.ambient, color.ambient)
        addColor(material, AiTexture.Type.emissive, color.emissive)
        addColor(material, AiTexture.Type.reflection, color.reflective)
        addColor(material, AiTexture.Type.specular, color.specular)
        addColor(material, AiTexture.Type.opacity, color.transparent)
    }

    /**
     * This adds a color of the given type
     */
    private fun addColor(material: Material, type: AiTexture.Type, color: AiColor3D?) {
        color?.let {
            material[type.name] = Vector3f(it.x, it.y, it.z)
        }
    }
}