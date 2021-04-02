package me.jraynor.util.concurrent

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

/***
 * This allows us to execute tasks on a seperate thread.
 */
interface ITaskExecutor<Msg> : AutoCloseable {
    val name: String

    /***
     * This should push the given task onto the queue
     */
    fun enqueue(msg: Msg)

    /**
     * We don't want this todo anything by default
     */
    override fun close() {}

    /**
     * This will execute the given function
     */
    fun <Source> taskOf(executor: Function<ITaskExecutor<Source>, Msg>): CompletableFuture<Source> {
        val future = CompletableFuture<Source>()
        val msg = executor.apply(inline("ask future process handle", future::complete))
        this.enqueue(msg)
        return future
    }

    /**
     * This will create an inline executable task with the given message
     */

    companion object {
        fun <Msg> inline(name: String, consumer: Consumer<Msg>): ITaskExecutor<Msg> {
            return object : ITaskExecutor<Msg> {
                override val name: String
                    get() = name

                /**
                 * This will enqueu the given message
                 */
                override fun enqueue(msg: Msg) {
                    consumer.accept(msg)
                }

                override fun toString(): String {
                    return name
                }
            }
        }
    }

}