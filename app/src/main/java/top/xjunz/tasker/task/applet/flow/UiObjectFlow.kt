/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.task.applet.flow

import android.view.accessibility.AccessibilityNodeInfo
import top.xjunz.tasker.engine.applet.base.ScopedFlow
import top.xjunz.tasker.engine.runtime.TaskRuntime
import top.xjunz.tasker.service.uiAutomation

/**
 * @author xjunz 2022/08/25
 */
class UiObjectFlow : ScopedFlow<UiObjectContext>() {

    private val rootNodeKey = generateUniqueKey(1)

    override fun initializeTarget(runtime: TaskRuntime): UiObjectContext {
        return UiObjectContext()
    }

    override suspend fun doApply(runtime: TaskRuntime): Boolean {
        val ctx = runtime.target
        val node = runtime.getEnvironmentVariable(rootNodeKey) {
            uiAutomation.rootInActiveWindow
        }.findFirst {
            runtime.ensureActive()
            ctx.source = it
            super.doApply(runtime)
        }
        return node != null
    }

    private suspend fun AccessibilityNodeInfo.findFirst(condition: suspend (AccessibilityNodeInfo) -> Boolean)
            : AccessibilityNodeInfo? {
        for (i in 0 until childCount) {
            val child = getChild(i) ?: continue
            if (!child.isVisibleToUser) continue
            try {
                if (condition(child)) {
                    return child
                } else if (child.childCount > 0) {
                    val ret = child.findFirst(condition)
                    if (ret != null) return ret
                }
            } finally {
                @Suppress("DEPRECATION")
                child.recycle()
            }
        }
        return null
    }

    override fun deriveTargetByRefid(which: Int, target: UiObjectContext): Any? {
        if (which == 1) {
            return target.source.text?.toString()
        }
        return super.deriveTargetByRefid(which, target)
    }
}