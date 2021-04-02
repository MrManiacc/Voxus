package me.jraynor.client.render.group

import com.artemis.World
import com.artemis.injection.FieldResolver
import com.artemis.utils.reflect.ClassReflection
import com.artemis.utils.reflect.Field
import me.jraynor.client.render.RenderMaster

/**
 * This will resolve all of the possible group builders and inject them.
 */

class RenderGroupFieldResolver : FieldResolver {
    private var master: RenderMaster? = null

    override fun initialize(world: World) {
        master = world.getSystem(RenderMaster::class.java)
    }

    /**
     * This will attempt to resolve the
     */
    override fun resolve(target: Any, fieldType: Class<*>, field: Field): Any? {

        return null
    }
}