/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.engine.applet.base

import androidx.annotation.CallSuper
import androidx.annotation.CheckResult
import kotlinx.coroutines.CancellationException
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * A flow is a set of [applets][Applet] but is also a special applet.
 *
 * @author xjunz 2022/08/04
 */
open class Flow(private val elements: MutableList<Applet> = ArrayList()) : Applet(),
    MutableList<Applet> by elements {

    open val minSize = 1

    open val maxSize = MAX_FLOW_CHILD_COUNT

    inline val requiredSize get() = if (minSize == maxSize) minSize else -1

    override val valueType: Int = VAL_TYPE_IRRELEVANT

    protected open val registerResultsForChildren: Boolean = true

    @CallSuper
    @CheckResult
    protected open suspend fun applyFlow(runtime: TaskRuntime): AppletResult {
        forEachIndexed `continue`@{ index, applet ->
            if (!applet.isEnabled) {
                return@`continue`
            }
            runtime.ensureActive()
            // Always execute the first applet in a flow and skip an applet if its relation to
            // previous peer applet does not meet the previous execution result.
            if (index != 0 && applet.isAnd != runtime.isSuccessful) {
                runtime.observer?.onAppletSkipped(applet, runtime)
                return@`continue`
            }
            applet.parent = this
            applet.index = index
            runtime.currentApplet = applet
            runtime.tracker.moveTo(index)
            runtime.observer?.onAppletStarted(applet, runtime)
            val result = try {
                applet.apply(runtime)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                AppletResult.error(t)
            }
            if (registerResultsForChildren) {
                runtime.registerResult(applet, result)
            }
            runtime.isSuccessful = result.isSuccessful
            runtime.observer?.onAppletTerminated(applet, runtime)
        }
        return if (runtime.isSuccessful) AppletResult.SUCCESS else AppletResult.FAILURE
    }

    open fun performStaticCheck(): StaticError? {
        val errorCode = staticCheckMyself()
        if (errorCode != StaticError.ERR_NONE) {
            return StaticError(this, errorCode)
        }
        forEach {
            if (it is Flow) {
                val error = it.performStaticCheck()
                if (error != null)
                    return error
            }
        }
        return null
    }

    @CallSuper
    protected open fun staticCheckMyself(): Int {
        // Code layer checks: find bugs
        check(size <= maxSize)
        if (requiredSize != -1) {
            check(size == requiredSize)
            check(isEnabled)
        }
        // User layer checks: find improper operations
        if (requiredSize != 0 && isEmpty()) {
            return StaticError.ERR_FLOW_NO_ELEMENT
        }
        return StaticError.ERR_NONE
    }

    final override suspend fun apply(runtime: TaskRuntime): AppletResult {
        onPreApply(runtime)
        runtime.currentFlow = this
        runtime.tracker.jumpIn()
        onPrepare(runtime)
        // Backup the target, because sub-flows may change the target, we don't want the changed
        // target to fall through.
        val backup = runtime.getRawTarget()
        val succeeded = applyFlow(runtime)
        onPostApply(runtime)
        // Restore the target
        runtime.setTarget(backup)
        runtime.tracker.jumpOut()
        return succeeded
    }

    /**
     * Just before the flow executing its elements.
     */
    protected open fun onPrepare(runtime: TaskRuntime) {
        /* no-op */
    }

    /**
     * Do something before the flow is started. At this time, [TaskRuntime.currentFlow] is
     * not yet assigned to this flow.
     */
    protected open fun onPreApply(runtime: TaskRuntime) {}

    /**
     * Do something after all [elements] are completed.
     */
    protected open fun onPostApply(runtime: TaskRuntime) {
        /* no-op */
    }
}