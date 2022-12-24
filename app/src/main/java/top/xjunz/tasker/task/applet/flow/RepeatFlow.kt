package top.xjunz.tasker.task.applet.flow

import top.xjunz.shared.ktx.casted
import top.xjunz.tasker.engine.applet.base.Flow
import top.xjunz.tasker.engine.applet.dto.AppletValues
import top.xjunz.tasker.engine.runtime.TaskRuntime

/**
 * @author xjunz 2022/12/04
 */
class RepeatFlow : Flow() {

    override val valueType: Int = AppletValues.VAL_TYPE_INT

    private var count: Int = 0

    override fun staticCheckMyself(): Int {
        check(value != null && value!!.casted<Int>() > 0) {
            "Repeat count must be specified!"
        }
        return super.staticCheckMyself()
    }

    override suspend fun doApply(runtime: TaskRuntime) {
        for (i in 0 until count) {
            super.doApply(runtime)
        }
    }

}