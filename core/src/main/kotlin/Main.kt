import me.jraynor.Voxus
import me.jraynor.util.side.Side

var isDevEnvironment: Boolean = true

/**
 * This is used to get the resource path
 */
val resourcePath: String
    get() {
        return if (isDevEnvironment)
            "src/main/resources"
        else
            "" //TODO: figure out a good place to manage resources in release enviorment.
    }

fun main(vararg args: String) {
    val path = Voxus::class.java.getResource("")
    if (path.toString().startsWith("jar:"))
        isDevEnvironment = false
    val voxus = if (args.isNotEmpty()) {
        when {
            args[0] == "server" -> {
                Voxus(Side.Server)
            }
            args[0] == "client" -> {
                Voxus(Side.Client)
            }
            else -> Voxus(Side.Both)
        }
    } else Voxus(Side.Both)
    /**This starts the voxus engine for the correct side based upon command line**/
    voxus.start()
}
