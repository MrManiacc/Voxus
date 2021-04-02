package me.jraynor

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import me.jraynor.client.ClientPlugin
import me.jraynor.common.CommonPlugin
import me.jraynor.server.ServerPlugin
import me.jraynor.util.side.Side
import net.mostlyoriginal.api.event.common.EventSystem
import kotlin.system.exitProcess

/**
 * This will keep track of the entire engine. It keeps the current state of the engine, keeps track of the worlds, and much more.
 */
class Voxus(private val side: Side) {
    /**When this is false, we should stop running**/
    private var running: Boolean = true

    /**This will be used to add the plugins to the builder**/
    private val builder: WorldConfigurationBuilder = WorldConfigurationBuilder()

    /**This is the current world, by default it's null.**/
    private var world: World? = null

    /**This will start the actual engine**/
    fun start() {
        builder.with(CommonPlugin())
        if (side.isServer)
            builder.with(ServerPlugin())
        if (side.isClient)
            builder.with(ClientPlugin(this))
        world = World(builder.build())
        var start = System.currentTimeMillis()
        while (running) {
            val now = System.currentTimeMillis()
            val dt = now - start
            start = now
            process(dt / 1000f)
        }
        world?.dispose()
    }

    /**This should process the engine with the given delta time**/
    private fun process(dt: Float) {
        world?.delta = dt
        world?.process()
    }

    /**This should stop the engine**/
    fun stop(now: Boolean = false) {
        if (!now)
            running = false
        else
            exitProcess(-1)
    }
}
