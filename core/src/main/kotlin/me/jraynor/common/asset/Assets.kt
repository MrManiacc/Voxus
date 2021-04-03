package me.jraynor.common.asset

import com.artemis.Component
import resourcePath
import java.io.InputStream
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * This is a simple static object to create new instances of assets
 */
object Assets {
    val assets = ConcurrentHashMap<KClass<*>, HashMap<String, Any>>();

    /**
     * This will create a new asset with the given type
     */
    inline fun <reified U : AssetData, reified T : Asset<U>> new(
        name: String,
        reload: Boolean = false,
        data: U? = null
    ): T? {
        val assetCls = T::class.java
        if (has<T>(name))
            return get(name)
        val ctr = try {
            assetCls.getConstructor(String::class.java)
        } catch (e: Exception) {
            println("Failed to find constructor with only a name string for asset class: ${assetCls.simpleName}, for name: $name")
            null
        } ?: return null
        val instance = put(ctr.newInstance(name))
        if (reload) {
            instance.load(force = true, dataIn = data)
        }
        return instance
    }

    /**
     * This will attempt to put the [asset] into the assets map,
     * it will return false if an asset with the given name exists
     */
    inline fun <reified T : Asset<*>> put(asset: T): T {
        val name = asset.name
        val cls = T::class
        if (!assets.containsKey(cls))
            assets[cls] = HashMap()
        if (assets[cls]?.containsKey(name)!!)
            return assets[cls]?.get(name) as T
        assets[cls]?.put(name, asset)
        return asset
    }

    /**
     * This will attempt to find the asset instance
     */
    inline fun <reified T : Asset<*>> get(name: String): T? {
        if (!has<T>(name))
            return null
        return assets[T::class]?.get(name) as T
    }

    /**
     * This will return true if the asset by the given type and name is present
     */
    inline fun <reified T : Asset<*>> has(name: String? = null): Boolean {
        val cls = T::class
        val typePresent = assets.containsKey(cls)
        if (name == null)
            return typePresent
        if (!typePresent) return false
        return assets[cls]!!.containsKey(name)
    }

    /**
     * This will reload all of the currently loaded assets, if [force] is true
     */
    fun reloadAll(force: Boolean = false) {
        if (force)
            disposeAll(force)
        assets.forEach { group ->
            group.value.forEach {
                val asset = it.value as Asset<*>
                asset.load(force)
            }
        }
    }

    /**
     * This will reload all of the currently loaded assets, if [force] is true
     */
    inline fun <reified T : Asset<*>> reload(force: Boolean = false) {
        if (force)
            dispose<T>(force)
        if (has<T>())
            assets[T::class]?.forEach {
                val asset = it.value as Asset<*>
                asset.load(force)
            }
    }

    /**
     * This will reload all of the currently loaded assets, if [force] is true
     */
    inline fun <reified T : Asset<*>> dispose(force: Boolean = false) {

        if (has<T>())
            assets[T::class]?.forEach {
                val asset = it.value as Asset<*>
                asset.unload(force)
            }
    }

    /**
     * This will dispose of all of the currently active [assets]
     */
    fun disposeAll(force: Boolean = false) {
        assets.forEach { group ->
            group.value.forEach {
                val asset = it.value as Asset<*>
                asset.unload(force)
            }
        }
    }
}

/**
 * This class will be abstract, it will load an asset using the input stream
 */
@Suppress("DEPRECATION")
abstract class Asset<T : AssetData>(
    val name: String,
    private val dataClass: Class<T>,
    private val extension: String,
    private val container: String
) : Component() {
    private var data: T? = null
    private var loaded = false

    init {
        this.data = createData()
    }

    /**
     * This will return the full path of the asset, relative to the jar
     */
    val path: String
        get() {
            if (!name.startsWith(container))
                return "/$container/$name.$extension"
            return "/$name.$extension"
        }

    /**
     * This will use the name to get the input stream
     */
    private val inputStream: InputStream?
        get() {
            return this::class.java.getResourceAsStream(path)
        }

    /**
     * This will generate the [AssetData] class instance
     */
    private fun createData(): T? {
        return try {
            val ctr = dataClass.getConstructor()
            val instance = ctr.newInstance()
            instance.path = path
            instance
        } catch (e: Exception) {
            println("Failed to instantiate asset with type ${dataClass.simpleName}")
            return null
        }
    }

    /**
     * This will reload the asset data
     */
    abstract fun reload(data: T)

    /**
     * This will reload the asset data
     */
    fun load(force: Boolean = false, stream: InputStream? = null, dataIn: T? = null) {
        if ((!loaded || force) && dataIn == null) {
            var dataStream: InputStream? = stream
            if (dataStream == null)
                dataStream = this.inputStream
            var valid = data!!.load("$resourcePath${data!!.path!!}")
            if (!valid)
                valid = data!!.load(dataStream!!)
            if (valid && data != null) {
                println("loading asset '${toString()}'")
                reload(data!!)
                loaded = true
            }
        } else if ((!loaded || force) && dataIn != null) {
            reload(dataIn as T)
        }
    }

    /**
     * This will call the dispose method, only if we're currently active
     */
    fun unload(force: Boolean = false) {
        if (loaded || force) {
            println("disposing asset '${toString()}'")
            dispose()
            loaded = false
        }
    }

    /**
     * This will dispose of the asset data
     */
    protected abstract fun dispose()

    override fun toString(): String {
        return "name: '$name', asset type: '${this::class.java.simpleName}' asset data type: '${dataClass.simpleName}'"
    }
}

/**
 * This represents some form of asset data, it will be the intermediary between
 * the asset, and the raw data
 */
open class AssetData(var path: String? = null) {
    open fun load(stream: InputStream): Boolean {
        return false
    }

    open fun load(path: String): Boolean {
        return false
    }
}
