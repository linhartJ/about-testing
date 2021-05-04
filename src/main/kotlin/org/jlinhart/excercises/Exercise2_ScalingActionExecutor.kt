package org.jlinhart.excercises

import java.io.IOException

/**
 * This component performs desired ScalingAction by starting/stopping Workers.
 * Every start/stop is synchronous and may fail - this component will not try again but returns information about
 * actually completed operations.
 * Start & Stop operations are delegated to WorkerOperator component
 */
class ScalingActionExecutor(
    private val workerOperator: WorkerOperator,
    private val actionProvider: ActionProvider
) {

    fun run(): ScalingActionResult {
        val scalingAction = actionProvider.get()
        return when (scalingAction) {
            ScalingAction.NoAction -> ScalingActionResult(emptyList(), emptyList())
            is ScalingAction.ScaleUp -> ScalingActionResult(startWorkers(scalingAction.workersToAdd), emptyList())
            is ScalingAction.ScaleDown -> ScalingActionResult(emptyList(), stopWorkers(scalingAction.workersToStop))
        }
    }

    private fun startWorkers(count: Int): List<WorkerId> {
        return (1..count).mapNotNull { startOrNull() }
    }

    private fun startOrNull(): WorkerId? {
        return try {
            println("Starting new worker")
            workerOperator.start()
        } catch (e: IOException) {
            println("Failed to start new worker")
            null
        }
    }

    private fun stopWorkers(workersToStop: List<WorkerId>): List<WorkerId> {
        return workersToStop.mapNotNull { stopOrNull(it) }
    }

    private fun stopOrNull(id: WorkerId): WorkerId? {
        return try {
            println("Stopping worker $id")
            workerOperator.stop(id)
            id
        } catch (e: NoSuchElementException) {
            println("Worker $id already does not exist. Stop considered successful.")
            id
        } catch (e: IOException) {
            println("Failed to stop worker $id")
            null
        }
    }
}

data class ScalingActionResult(
    val workersStarted: List<WorkerId>,
    val workersStopped: List<WorkerId>
)

interface WorkerOperator {
    @Throws(IOException::class)
    fun start(): WorkerId

    @Throws(NoSuchElementException::class, IOException::class)
    fun stop(id: WorkerId)
}

interface ActionProvider {
    fun get(): ScalingAction
}
