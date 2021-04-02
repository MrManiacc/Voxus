package me.jraynor.util.side

/**
 * This allows us to keep track of the current side we're on.
 */
enum class Side {
    Client, Server, Both;

    val isClient: Boolean
        get() = this == Client || this == Both

    val isServer: Boolean
        get() = this == Server || this == Both

}