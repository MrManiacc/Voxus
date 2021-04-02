package me.jraynor.util.concurrent

import java.lang.Exception
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executor
import java.util.concurrent.locks.LockSupport
import java.util.function.BooleanSupplier

/**
 * This abstract class allows for easy threaded executionl
 */
abstract class AbstractThreadedTaskExecutor<R : Runnable>(override val name: String = "un-named abstract task") :
    ITaskExecutor<R>, Executor {
    /**Used to track the current execution point**/
    private var drivers = 0

    /**Keeps track of the currently queued up runnables**/
    private val queue: Queue<R> = ConcurrentLinkedDeque()

    /**This is used to make sure we're on the correct thread**/
    protected val onExecutionThread: Boolean
        get() = Thread.currentThread() == this.getExecutionThread()

    /**We deffer the tasks if we're on the execution thread**/
    protected val shouldDefferTasks: Boolean
        get() = !onExecutionThread

    /**
     * Wraps a given task ask the message type
     */
    protected abstract fun wrapTask(runnable: Runnable): R

    /***
     * This will get the current execution thread
     */
    protected abstract fun getExecutionThread(): Thread

    /***
     * Checks to see if the given runnable can run on this task executor
     */
    protected abstract fun canRun(runnable: R): Boolean

    /**
     * This will run the given task on the correct thread
     */
    fun deferTask(taskIn: Runnable): CompletableFuture<Void> {
        return CompletableFuture.supplyAsync(
            {
                taskIn.run()
                null
            }, this
        )
    }

    /**
     * This will run the task either on thsi thread it's its current or enqueu it to the correct thread
     */
    open fun runAsync(taskIn: Runnable): CompletableFuture<Void> {
        return if (this.shouldDefferTasks) {
            deferTask(taskIn)
        } else {
            taskIn.run()
            CompletableFuture.completedFuture(null as Void?)
        }
    }

    /**
     * This will run the task right now, joining the thread if it has to.
     */
    fun runImmediately(taskIn: Runnable) {
        if (shouldDefferTasks)
            deferTask(taskIn).join()
        else
            taskIn.run()
    }

    /**
     * This enqueues the given task
     */
    override fun enqueue(msg: R) {
        this.queue.add(msg)
        LockSupport.unpark(this.getExecutionThread())
    }

    /**
     * Executes in the correct place.
     */
    override fun execute(command: Runnable) {
        if (shouldDefferTasks)
            this.enqueue(this.wrapTask(command))
        else command.run()
    }

    /**
     * This will attempt to execute the next one
     */
    protected fun drainTasks() {
        while (driveOne()) {
        }
    }

    /**
     * This attempts to the drive the next possiblel
     */
    protected fun driveOne(): Boolean {
        val r = queue.peek()
        return if (r == null) {
            false
        } else if (this.drivers == 0 && !canRun(r)) {
            false
        } else {
            this.run(queue.remove())
            true
        }
    }

    /**
     * This will attempt to drive until the given boolean supplier is true.
     */
    fun driveUntil(isDone: BooleanSupplier) {
        ++drivers
        try {
            while (!isDone.asBoolean) {
                if (!driveOne()) {
                    this.threadYieldPark()
                }
            }
        } finally {
            --drivers
        }
    }

    /**
     * This will try to run the given task wrapped in an try catch
     */
    fun run(taskIn: R) {
        try {
            taskIn.run()
        } catch (exception: Exception) {
            error(exception.localizedMessage + " for ${this.name}")
        }
    }

    protected open fun threadYieldPark() {
        Thread.yield()
        LockSupport.parkNanos("waiting for tasks", 100000L)
    }

}