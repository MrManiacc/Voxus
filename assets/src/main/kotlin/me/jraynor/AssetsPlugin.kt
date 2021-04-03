package me.jraynor

import com.artemis.ArtemisPlugin
import com.artemis.WorldConfigurationBuilder
import me.jraynor.manager.AssetTypeManager

/**
 * This will setup the asset resolvers and field resolvers for assets.
 */
class AssetsPlugin : ArtemisPlugin {
    /**
     * This will register our asset type manager as a manager that can be injected
     */
    override fun setup(b: WorldConfigurationBuilder) {
        b.with(AssetTypeManager())
    }
}