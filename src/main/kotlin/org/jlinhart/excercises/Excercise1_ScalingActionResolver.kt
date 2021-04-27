package org.jlinhart.excercises

import java.time.Duration
import kotlin.math.abs

/**
 * This component is supposed to identify proper scaling action of "Worker" component based on given data.
 * We attempt to have enough workers to satisfy all requests within 1 min.
 *
 * Input data include:
 * 1) Workload - current amount of work to do
 * 2) Workers - current state of running Workers
 */
class ScalingActionResolver {

    companion object {
        val targetDuration = Duration.ofMinutes(1)
    }

    fun resolveAction(workload: Workload, workers: Workers): ScalingAction {
        val requiredWorkersCount = calculateRequiredWorkersCount(workload)
        val activeWorkers = countActiveWorkers(workers)
        val diff = abs(requiredWorkersCount - activeWorkers)
        return when {
            requiredWorkersCount < activeWorkers -> resolveScaleDown(diff, workers)
            activeWorkers < requiredWorkersCount -> ScalingAction.ScaleUp(diff)
            else -> ScalingAction.NoAction
        }
    }

    private fun resolveScaleDown(maxShutdownCount: Int, workers: Workers): ScalingAction {
        val toShutdown = workers.instances
            .asSequence()
            .filter { (_, state) -> state == WorkerState.IDLING }
            .take(maxShutdownCount)
            .map { (id, _) -> id }
            .toList()
        return if (toShutdown.isNotEmpty()) {
            ScalingAction.ScaleDown(toShutdown)
        } else {
            ScalingAction.NoAction
        }
    }

    private val activeWorkerStates = setOf(WorkerState.IDLING, WorkerState.INITIALIZING, WorkerState.RUNNING)
    private fun countActiveWorkers(workers: Workers): Int {
        return workers.instances.count { (_, state) -> activeWorkerStates.contains(state) }
    }

    private fun calculateRequiredWorkersCount(workload: Workload): Int {
        val totalTimeRequired = workload.averageJobDuration.multipliedBy(workload.waitingRequests.toLong())
        val fullyLoadedWorkers = totalTimeRequired.dividedBy(targetDuration)
        val extra = if (workload.averageJobDuration.multipliedBy(fullyLoadedWorkers) < totalTimeRequired) 1 else 0
        return fullyLoadedWorkers.toInt() + extra
    }
}

data class Workload(
    val waitingRequests: Int,
    val averageJobDuration: Duration,
)

data class WorkerId(val value: Long)
enum class WorkerState { INITIALIZING, RUNNING, IDLING, STOPPING, FAILED }
data class Workers(
    val instances: Map<WorkerId, WorkerState>
)

sealed class ScalingAction {
    object NoAction : ScalingAction()
    class ScaleUp(val workersToAdd: Int) : ScalingAction()
    class ScaleDown(val workersToStop: List<WorkerId>) : ScalingAction()
}
