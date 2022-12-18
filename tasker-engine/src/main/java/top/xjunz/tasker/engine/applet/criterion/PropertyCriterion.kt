package top.xjunz.tasker.engine.applet.criterion

import top.xjunz.tasker.engine.applet.dto.AppletValues

/**
 * @author xjunz 2022/09/22
 */
class PropertyCriterion<T : Any>(private inline val matcher: (target: T) -> Boolean) :
    Criterion<T, Boolean>() {

    override val valueType: Int = AppletValues.VAL_TYPE_IRRELEVANT

    override var defaultValue: Boolean = true

    override fun matchTarget(target: T, value: Boolean): Boolean {
        return matcher(target) == value
    }
}