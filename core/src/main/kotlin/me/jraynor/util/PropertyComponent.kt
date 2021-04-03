package me.jraynor.util

import com.artemis.Component
import me.jraynor.client.render.util.EditorComponent
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

/**
 * This allows a component to store various property types
 */
abstract class PropertyComponent : EditorComponent() {
    val properties: MutableMap<KClass<*>, MutableMap<String, Any>> = HashMap()

    /**
     * This will get the property with the given name and type or null
     */
    inline operator fun <reified T : Any> get(name: String): T? {
        val group = properties[T::class] ?: return null
        if (!group.containsKey(name)) return null
        return group[name] as T
    }

    /**
     * This allows us to add nameless properties, thatGG can simply be iterated over
     */
    fun add(property: Any) {
        set(UUID.randomUUID().toString(), property)
    }

    /**
     * This will set the given property with the given name
     */
    operator fun set(name: String, property: Any) {
        val type = property::class
        if (!properties.containsKey(type))
            properties[type] = HashMap()
        properties[type]?.set(name, property)
    }

    /**
     * This will remove the specified property, or all of the given type if [name] is null
     */
    inline fun <reified T : Any> remove(name: String? = null): Boolean {
        if (!has<T>(name)) return false
        if (name == null)
            return this.properties.remove(T::class) != null
        return this.properties[T::class]?.remove(name) != null
    }

    /**
     * If the [name] is null we will check to see if any instance of the given type [T] is present, if it's not null
     * then we will check to make sure a property with the given type and name exists.
     */
    inline fun <reified T : Any> has(name: String? = null): Boolean {
        if (!properties.containsKey(T::class)) return false
        if (name == null) return true
        return properties[T::class]!!.containsKey(name)
    }

    /**
     * This will call the [caller] if it's present
     */
    inline fun <reified T : Any> ifPresent(name: String, caller: (T) -> Unit) {
        if (has<T>(name)) {
            this.get<T>(name)?.let(caller)
        }
    }

    /**
     * This will iterate each of the properties with the given reified [T] type
     */
    inline fun <reified T : Any> forEach(noinline iterator: (name: String, property: T) -> Unit) {
        properties[T::class]?.forEach { iterator(it.key, it.value as T) }
    }

    /**
     * This will iterate each of the properties with the given reified [T] type
     */
    inline fun <reified T : Any> forEach(noinline iterator: (Pair<String, T>) -> Unit) {
        properties[T::class]?.forEach { iterator(Pair(it.key, it.value as T)) }
    }


    /**
     * This allows for an arbitrary component that can store some kind of data
     */
    override fun render() {
        //TODO: render properties here and allow them to be edited
    }

    override fun toString(): String {
        return this.properties.toString()
    }
}