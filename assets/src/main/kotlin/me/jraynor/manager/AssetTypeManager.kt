package me.jraynor.manager


import com.google.common.base.Preconditions
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import me.jraynor.asset.Asset
import me.jraynor.asset.AssetType
import me.jraynor.data.AssetData
import me.jraynor.data.AssetFactory
import me.jraynor.files.AbstractAssetFileFormat
import net.mostlyoriginal.api.system.core.PassiveSystem
import java.util.*


/**
 * Maps a class to an give asset type
 */
class AssetTypeManager : PassiveSystem() {
    private val assetTypes: MutableMap<Class<out Asset<out AssetData>>, AssetType<*, *>> =
        Maps.newHashMap()
    private val assetFolders: MutableMap<Class<out Asset<out AssetData>>, MutableList<String>> =
        Maps.newHashMap()
    private val assetFormat: MutableMap<Class<out Asset<out AssetData>>, AbstractAssetFileFormat<out AssetData>> =
        Maps.newHashMap()
    private val resolvedTypes: MutableSet<Class<out Asset<out AssetData>>> = Sets.newConcurrentHashSet()

    /**
     * Registers an asset type. It will be available after the next time
     * read from modules from the provided subfolders. If there are no subfolders then assets will not be loaded from modules.
     *
     * @param type       The type of to register as a core type
     * @param factory    The factory to create assets of the desired type from asset data
     * @param subfolders The name of the subfolders which asset files related to this type will be read from within modules
     * @param <T>        The type of asset
     * @param <U>        The type of asset data
    </U></T> */
    @Synchronized
    fun <T : Asset<U>, U : AssetData> registerAssetType(
        type: Class<T>,
        factory: AssetFactory<T, U>,
        format: AbstractAssetFileFormat<U>,
        vararg subfolders: String
    ) {
        if (!resolvedTypes.contains(type)) {
            Preconditions.checkState(
                !assetTypes.containsKey(type),
                "Asset type '" + type.simpleName + "' already registered"
            )
            assetFormat[type] = format
            val assetType = AssetType(type, factory)
            assetTypes[type] = assetType
            if (!assetFolders.containsKey(type)) assetFolders[type] = ArrayList()
            for (folder in subfolders) assetFolders[type]!!.add(folder)
            resolvedTypes.add(type)
        }
    }

    /**
     * Gets the type of asset for the for the given class type
     *
     * @param type the type of asset to search for
     * @param <T>  The type of asset
     * @param <U>  The type of asset data
     * @return returns an asset type of null
    </U></T> */
    fun <T : Asset<U>, U : AssetData> getAssetTypeExact(type: Class<T>): Optional<AssetType<T, U>> {
        return Optional.ofNullable(assetTypes[type] as AssetType<T, U>?)
    }

    /**
     * Gets the type of asset for the for the given class type
     *
     * @param type the type of asset to search for
     * @param <T>  The type of asset
     * @param <U>  The type of asset data
     * @return returns an asset type of null
    </U></T> */
    fun getAssetType(type: Class<out Asset<out AssetData>>): Optional<AssetType<out Asset<out AssetData>, out AssetData>> {
        return Optional.ofNullable(assetTypes[type] as AssetType<out Asset<out AssetData>, out AssetData>?)
    }

    /**
     * Gets an asset's folder based on the given asset type
     *
     * @param type the type of asset to get folder for
     * @param <T>  the type of asset
     * @param <U>  the type of asset data
     * @return returns the asset folder
    </U></T> */
    fun <T : Asset<U>, U : AssetData?> getAssetFoldersExact(type: Class<T>): Optional<List<String>> {
        return Optional.ofNullable(assetFolders[type])
    }


    /**
     * Gets an asset's folder based on the given asset type
     *
     * @param type the type of asset to get folder for
     * @param <T>  the type of asset
     * @param <U>  the type of asset data
     * @return returns the asset folder
    </U></T> */
    fun getAssetFolders(type: Class<*>): Optional<List<String>> {
        return Optional.ofNullable(assetFolders[type])
    }

    /**
     * Gets the registered types for all of the assets
     *
     * @return returns collection of asset types
     */
    val registeredTypes: Collection<Class<out Any?>>
        get() = assetTypes.keys

    /**
     * Used so we don't try to load an asset type twice
     */
    fun finishType(type: Class<out Asset<out AssetData>>) {
        resolvedTypes.add(type)
    }

    /**
     * @return returns true if this type has been resolved
     */
    fun isResolved(type: Class<out Asset<out AssetData>>): Boolean {
        return resolvedTypes.contains(type)
    }

    /**
     * Gets the abstract asset format for using inside the asset source resolver
     *
     * @param type the type of asset
     * @param <T>  the generic type
     * @param <U>  the generic asset data type
     * @return returns the format
    </U></T> */
    fun getFormat(type: Class<out Asset<out AssetData>>): Optional<AbstractAssetFileFormat<out AssetData>> {
        return Optional.ofNullable<AbstractAssetFileFormat<out AssetData>>(assetFormat[type] as AbstractAssetFileFormat<out AssetData>)
    }

    /**
     * Gets the abstract asset format for using inside the asset source resolver
     *
     * @param type the type of asset
     * @param <T>  the generic type
     * @param <U>  the generic asset data type
     * @return returns the format
    </U></T> */
    fun <T : Asset<U>?, U : AssetData> getFormatExact(type: Class<T?>): Optional<AbstractAssetFileFormat<U>> {
        return Optional.ofNullable<AbstractAssetFileFormat<U>>(assetFormat[type] as AbstractAssetFileFormat<U>)
    }
}