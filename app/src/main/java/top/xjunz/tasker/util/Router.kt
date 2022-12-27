/*
 * Copyright (c) 2022 xjunz. All rights reserved.
 */

package top.xjunz.tasker.util

import android.content.Context
import top.xjunz.tasker.ktx.viewUrlSafely
import java.net.URLEncoder

/**
 * Helper class for routing app to a specific host.
 *
 * @author xjunz 2022/10/21
 */
object Router {

    private const val SCHEME = "xtsk"
    const val HOST_ACCEPT_OPTIONS_FROM_INSPECTOR = "options-from-inspector"
    const val HOST_NONE = "no-op"
    const val HOST_ACTION = "action"

    fun Context.launchRoute(host: String) {
        viewUrlSafely("$SCHEME://$host")
    }

    fun Context.launchAction(actionName: String, value: Any) {
        query(HOST_ACTION, actionName to value)
    }

    fun Context.query(host: String, vararg queries: Pair<String, Any>) {
        check(queries.isNotEmpty()) {
            "No query provided!"
        }
        val query = StringBuilder()
        queries.forEach {
            query.append(it.first).append("=").append(
                URLEncoder.encode(it.second.toString(), "utf-8")
            ).append("&")
        }
        query.deleteCharAt(query.lastIndex)
        viewUrlSafely("$SCHEME://$host/?$query")
    }
}