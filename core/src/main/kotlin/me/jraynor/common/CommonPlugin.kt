package me.jraynor.common

import com.artemis.ArtemisPlugin
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.TagManager
import me.jraynor.AssetsPlugin

/**
 * Here we will setup all systems that will run on both the client and server.
 */
class CommonPlugin : ArtemisPlugin {
    override fun setup(b: WorldConfigurationBuilder) {
        b.with(TagManager())
        b.with(AssetsPlugin())
    }

}