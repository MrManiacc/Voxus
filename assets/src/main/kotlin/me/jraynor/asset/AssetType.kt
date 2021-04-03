package me.jraynor.asset

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableSet
import com.google.common.collect.MapMaker
import com.jgfx.assets.urn.ResourceUrn
import me.jraynor.data.AssetData
import me.jraynor.data.AssetFactory
import me.jraynor.util.AssetUtils
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.Semaphore


/**
 * AssetType manages all assets of a particular type/class.  It provides the ability to resolve and load assets by Urn, and caches assets so that there is only
 * a single instance of a given asset shared by all users.
 *
 *
 * AssetType is thread safe.
 *
 *
 * @param <T> The type of asset this AssetType manages
 * @param <U> The type of asset data required by the assets this AssetType manages
</U></T> */
class AssetType<T : Asset<U>, U : AssetData>(
    val assetClass: Class<T>,
    val factory: AssetFactory<T, U>
) {

    private var assetDataClass: Class<U>

    val loadedAssets: MutableMap<ResourceUrn, T> = MapMaker().concurrencyLevel(4).makeMap()

    // Per-asset locks to deal with situations where multiple threads attempt to obtain or create the same unloaded asset concurrently
    private val locks: MutableMap<ResourceUrn, ResourceLock> = MapMaker().concurrencyLevel(1).makeMap()

    /**
     * Obtains an asset by urn, loading it if necessary. If the urn is a instance urn, then a new asset will be created from the parent asset.
     *
     * @param urn The urn of the resource to get
     * @return The asset if available
     */
    fun getAsset(urn: ResourceUrn): Optional<T> {
        Preconditions.checkNotNull(urn)
        return Optional.ofNullable(loadedAssets[urn])
    }

    /**
     * Loads an asset with the given urn and data. If the asset already exists, it is reloaded with the data instead
     *
     * @param urn  The urn of the asset        Preconditions.checkNotNull(urn);
     * @param data The data to load the asset with
     * @return The loaded (or reloaded) asset
     */
    fun loadAsset(urn: ResourceUrn, data: AssetData?): T? {
        return if (urn.isInstance) {
            factory.build(urn, this, this.assetDataClass.cast(data))
        } else {
            var asset = loadedAssets[urn]
            if (asset != null) {
                asset.reload(this.assetDataClass.cast(data))
            } else {
                var lock: ResourceLock?
                synchronized(locks) {
                    lock = locks[urn]
                    if (lock == null) {
                        lock = ResourceLock(urn)
                        locks[urn] = lock!!
                    }
                }
                try {
                    lock!!.lock()
                    asset = loadedAssets[urn]
                    if (asset == null) {
                        asset = factory.build(urn, this, this.assetDataClass.cast(data))
                    } else {
                        asset.reload(this.assetDataClass.cast(data))
                    }
                    synchronized(locks) {
                        if (lock!!.unlock()) {
                            locks.remove(urn)
                        }
                    }
                } catch (e: InterruptedException) {
                    System.err.println("Failed to load asset - interrupted awaiting lock on resource: $urn")
                }
            }
            asset
        }
    }

    /**
     * @param urn The urn of the asset to check. Must not be an instance urn
     * @return Whether an asset is loaded with the given urn
     */
    fun isLoaded(urn: ResourceUrn): Boolean {
        Preconditions.checkArgument(!urn.isInstance, "Urn must not be an instance urn")
        return loadedAssets.containsKey(urn)
    }

    /**
     * Notifies the asset type when an asset is created
     *
     * @param baseAsset The asset that was created
     */
    @Synchronized
    fun registerAsset(baseAsset: Asset<U>) {
        loadedAssets[baseAsset.urn] = assetClass.cast(baseAsset)
    }

    /**
     * @return A set of the urns of all the loaded assets.
     */
    val loadedAssetUrns: Set<ResourceUrn>
        get() = ImmutableSet.copyOf(loadedAssets.keys)

    /**
     * @return A list of all the loaded assets.
     */
    fun getLoadedAssets(): Set<T> {
        return ImmutableSet.copyOf(loadedAssets.values)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AssetType<*, *>) {
            return assetClass == obj.assetClass
        }
        return false
    }

    override fun hashCode(): Int {
        return assetClass.hashCode()
    }

    override fun toString(): String {
        return assetClass.simpleName
    }

    private class ResourceLock(private val urn: ResourceUrn) {
        private val semaphore = Semaphore(1)

        @Throws(InterruptedException::class)
        fun lock() {
            semaphore.acquire()
        }

        fun unlock(): Boolean {
            val lockFinished = !semaphore.hasQueuedThreads()
            semaphore.release()
            return lockFinished
        }

        override fun toString(): String {
            return "lock($urn)"
        }
    }

    /**
     * Constructs an AssetType for managing assets of the provided Asset class. The Asset class must have its AssetData generic parameter bound via inheritance
     * (e.g. MyType extends Asset&lt;MyDataType&gt;)
     *
     * @param assetClass The class of asset this AssetType will manage.
     * @param factory    The factory used to convert AssetData to Assets for this type
     */
    init {
        Preconditions.checkNotNull(assetClass)
        Preconditions.checkNotNull(factory)
        val assetDataType: Optional<Type> = AssetUtils.getTypeParameterBindingForInheritedClass(
            assetClass,
            Asset::class.java, 0
        )
        assetDataClass = if (assetDataType.isPresent)
            AssetUtils.getClassOfType(assetDataType.get()) as Class<U>
        else {
            throw IllegalArgumentException("Asset class must have a bound AssetData parameter - $assetClass")
        }
    }
}