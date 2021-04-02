package me.jraynor.client.render.util

import com.artemis.Component
import imgui.ImGui
import imgui.type.ImFloat
import org.joml.Vector3f
import org.joml.Vector4d
import org.joml.Vector4f
import java.lang.reflect.Field

/**
 * This will allow us to make certain things edible using a cache an reflection.
 */
open class EditorComponent : Component() {
    /**keeps track of whether or not we've cached yet**/
    private var cached = false

    /**This will allow us to cache our fields and ultimately use this for rendering/editing**/
    private val cachedFloats = HashMap<String, Pair<FloatArray, Field>>()
    private val cachedVec3fs = HashMap<String, Pair<FloatArray, Field>>()
    private val cachedVec4fs = HashMap<String, Pair<FloatArray, Field>>()


    /**
     * This will be rendered when a component is selected. There will be editable elements to the component
     */
    fun render() {
        if (!cached)
            cache()

        cachedFloats.forEach {
            val imFloat = it.value.first
            val field = it.value.second
            if (ImGui.dragFloat(it.key, imFloat)) {
                field.set(this, imFloat[0])
                println("updated float field value for ${it.key} for class ${this::class.java.simpleName}")
            }
        }
        cachedVec3fs.forEach {
            val imArray = it.value.first
            val field = it.value.second
            if (ImGui.dragFloat3(it.key, imArray)) {
                val vec3f = Vector3f(imArray)
                field.set(this, vec3f)
                println("updated vec3 field value for ${it.key} for class ${this::class.java.simpleName}")
            }
        }


        cachedVec4fs.forEach {
            val imArray = it.value.first
            val field = it.value.second
            if (ImGui.dragFloat4(it.key, imArray)) {
                val vec4fOut = field.get(this) as Vector4f
                vec4fOut.set(imArray)
                println("updated vec3 field value for ${it.key} for class ${this::class.java.simpleName}")
            }
        }
    }

    /**
     * This will attempt to cache all of the fields that are editable.
     */
    private fun cache() {
        val cls = this::class.java

        println("here")
        cls.declaredFields.forEach {
            val name = it.name
            if (Vector3f::class.java.isAssignableFrom(it.type)) {
                if (!cachedVec3fs.containsKey(name)) {
                    it.isAccessible = true
                    val valueIn = it.get(this) as Vector3f? ?: Vector3f()
                    cachedVec3fs[name] = Pair(floatArrayOf(valueIn.x, valueIn.y, valueIn.z), it)
                    println("Cached float field $name for component ${cls.simpleName}")
                }
            } else if (Vector4f::class.java.isAssignableFrom(it.type)) {
                if (!cachedVec4fs.containsKey(name)) {
                    it.isAccessible = true
                    val valueIn = it.get(this) as Vector4f? ?: Vector4f()
                    cachedVec4fs[name] = Pair(floatArrayOf(valueIn.x, valueIn.y, valueIn.z, valueIn.w), it)
                    println("Cached float field $name for component ${cls.simpleName}")
                }
            } else if (Float::class.java.isAssignableFrom(it.type)) {
                if (!cachedFloats.containsKey(name)) {
                    it.isAccessible = true
                    val value = it.get(this) as Float? ?: 0f
                    cachedFloats[name] = Pair(floatArrayOf(value), it)
                    println("Cached float field $name for component ${cls.simpleName}")
                }
            }
        }
        cached = true
    }


}