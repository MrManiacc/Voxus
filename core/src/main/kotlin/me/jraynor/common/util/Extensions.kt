@file:Suppress("UNCHECKED_CAST", "EXTENSION_SHADOWED_BY_MEMBER")

package me.jraynor.common.util

import com.artemis.*
import com.artemis.systems.EntityProcessingSystem
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f
import org.joml.Vector3i
import java.text.DecimalFormat
import java.util.function.Consumer
import kotlin.Pair
import kotlin.reflect.KClass



/**
 * Checks to see if the entity has all of the given components
 */
fun Entity.hasComponents(vararg components: Class<out Component>): Boolean {
    var valid = true
    components.forEach {
        if (this.getComponent(it) == null)
            valid = false
    }
    return valid
}

/**
 * This will compute a new component if it doesn't exist inside the entity
 */
inline fun <reified T : Any> Entity.getOrNew(): T {
    if (!Component::class.java.isAssignableFrom(T::class.java)) {
        println("Passed class ${T::class.java.simpleName} does not extend component!")
    }
    val cmpCls = T::class.java as Class<Component>
    val localCmp = this.getComponent(cmpCls)
    return if (localCmp == null) {
        val cmp = cmpCls.getConstructor().newInstance() as T
        this.edit().add(cmp as Component)
        cmp
    } else {
        localCmp as T
    }
}


/**
 * Checks to see if the entity has all of the given components
 */
fun World.hasComponents(entityId: Int, vararg components: Class<out Component>): Boolean {
    return this.getEntity(entityId).hasComponents(*components)
}

/**
 * Buffers a [Vector2i] with some arrays
 */
data class Vec2iBuf(
    val widthBuffer: IntArray = IntArray(1),
    val heightBuffer: IntArray = IntArray(1),
    private val value: Vector2i = Vector2i()
) {
    fun get(): Vector2i {
        value.x = widthBuffer[0]
        value.y = heightBuffer[0]
        return value
    }
}

/**
 * Buffers a [Vector2f] with some arrays
 */
data class Vec2fBuf(
    val widthBuffer: FloatArray = FloatArray(1),
    val heightBuffer: FloatArray = FloatArray(1),
    private val value: Vector2f = Vector2f()
) {
    fun get(): Vector2f {
        value.x = widthBuffer[0]
        value.y = heightBuffer[0]
        return value
    }
}


fun Vector2i.formatted(): String {
    return this.toString(DecimalFormat.getInstance())
}

fun Vector2f.formatted(): String {
    return this.toString(DecimalFormat.getInstance())
}

fun Vector3f.formatted(): String {
    return this.toString(DecimalFormat.getInstance())
}


val Vector3i.formatted: String
    get() = this.toString(DecimalFormat.getInstance())

val Vector2i.formatted: String
    get() = this.toString(DecimalFormat.getInstance())


/**
 * This will convert a list of strings to a single string
 */
fun List<String>.asString(): String {
    val br = StringBuilder()
    this.forEach {
        br.append(it).append("\n")
    }
    return br.toString()
}


/**
 * This will convert a list of strings to a single string
 */
fun String.asList(): ArrayList<String> {
    val split = this.split("\n")
    val list = ArrayList<String>()
    split.forEach {
        list.add(it)
    }
    return list
}

/**
 * This will convert a float to radians
 */
val Float.degrees: Float
    get() = Math.toDegrees(this.toDouble()).toFloat()

val Float.radians: Float
    get() = Math.toRadians(this.toDouble()).toFloat()

fun Float.clamp(min: Float, max: Float): Float {
    return this.coerceAtLeast(min).coerceAtMost(max)
}

/**
 * This will get a new connection of core systems
 */
val World.coreSystems: ArrayList<CoreSystem>?
    get() {
        val systems = ArrayList<CoreSystem>()
        this.systems.forEach(Consumer {
            if (it is CoreSystem)
                systems.add(it)
        })
        return systems
    }

/**
 * This will get a new connection of core systems
 */
val World.coreEntitySystems: ArrayList<CoreEntitySystem>?
    get() {
        val systems = ArrayList<CoreEntitySystem>()
        this.systems.forEach(Consumer {
            if (it is CoreEntitySystem)
                systems.add(it)
        })
        return systems
    }

/**
 * This will get a new connection of core systems
 */
val World.coreEntityProcessingSystems: ArrayList<CoreEntityProcessingSystem>?
    get() {
        val systems = ArrayList<CoreEntityProcessingSystem>()
        this.systems.forEach(Consumer {
            if (it is CoreEntityProcessingSystem)
                systems.add(it)
        })
        return systems
    }

open class CoreSystem(val initOnly: Boolean = false, val tick: Boolean = false) : BaseSystem() {


    protected fun <T> system(system: KClass<*>): T {
        val systemCls = system::class.java as Class<BaseSystem>
        return world!!.getSystem(systemCls) as T
    }


    /**
     * The main post init method
     */
    open fun postInitialization() {
    }

    /**
     * Process the system.
     */
    override fun processSystem() {
    }


    /**
     * This will be called 20 times per second
     */
    open fun tick() {
        processSystem()
    }
}

open class CoreEntitySystem(aspect: Aspect.Builder? = null, val initOnly: Boolean = false) : EntitySystem(aspect) {

    protected fun <T : Any> system(system: KClass<T>): T {
        val systemCls = system::class.java as Class<BaseSystem>
        return world!!.getSystem(systemCls) as T

    }

    /**
     * Process the system.
     */
    override fun processSystem() {
    }

    /**
     * The main post init method
     */
    fun postInitialization() {
    }

}

open class CoreEntityProcessingSystem(
    aspect: Aspect.Builder? = null,
    val initOnly: Boolean = false,
    val tick: Boolean = false
) : EntityProcessingSystem(aspect) {


    protected fun <T : Any> sys(system: KClass<T>): T {
        val systemCls = system::class.java as Class<BaseSystem>
        return world!!.getSystem(systemCls) as T
    }

    /**
     * Process a entity this system is interested in.
     * @param e
     * the entity to process
     */
    override fun process(e: Entity?) {

    }

    /**
     * Does the system desire processing.
     *
     * Useful when the system is enabled, but only occasionally
     * needs to process.
     *
     * This only affects processing, and does not affect events
     * or subscription lists.
     *
     * @return true if the system should be processed, false if not.
     * @see .isEnabled
     */
    override fun checkProcessing(): Boolean {
        return super.checkProcessing()
    }

    /**
     * The main post init method
     */
    fun postInitialization() {
    }

    /**
     * This will be called 20 times per second
     */
    open fun tick() {
    }

}

/**
 * Returns an empty array of the specified type [T].
 */
inline fun <reified T> emptyArrayOfSize(size: Int): Array<T> =
    @Suppress("UNCHECKED_CAST")
    (arrayOfNulls<T>(size) as Array<T>)



data class MyPair<A, B>(
    var first: A? = null,
    var second: B? = null
) {

    /**
     * Returns string representation of the [Pair] including its [first] and [second] values.
     */
    override fun toString(): String = "($first, $second)"
}

inline fun <T> withAll(vararg receivers: T, block: T.() -> Unit) {
    for (receiver in receivers) receiver.block()
}

/**
 * This will do ors of the given values.
 */
fun Int.orEquals(vararg ints: Int): Int {
    var out = this
    for (element in ints)
        out = out or element
    return out
}