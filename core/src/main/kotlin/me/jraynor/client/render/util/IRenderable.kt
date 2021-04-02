package me.jraynor.client.render.util

/**
 * This is a simple interface for anything that can be rendered. Normally there will be a system that extends this
 */
@FunctionalInterface
fun interface IRenderable {
    fun render()
}