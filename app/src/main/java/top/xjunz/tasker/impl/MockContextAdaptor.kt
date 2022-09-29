package top.xjunz.tasker.impl

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.os.PowerManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.test.uiautomator.mock.MockContext
import androidx.test.uiautomator.mock.MockDisplay


/**
 * @author xjunz 2022/07/23
 */
@Suppress("DEPRECATION")
class MockContextAdaptor(private val ctx: Context) : MockContext, MockDisplay {

    private val powerManager by lazy {
        ctx.getSystemService(PowerManager::class.java)
    }

    private val defDisplay by lazy {
        ctx.getSystemService(WindowManager::class.java).defaultDisplay
    }

    override fun getRealSize(p: Point) {
        defDisplay.getRealSize(p)
    }

    override fun getRealMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }

    override fun getSize(p: Point) {
        defDisplay.getSize(p)
    }

    override fun getRotation(): Int {
        return defDisplay.rotation
    }

    override fun isInteractive(): Boolean {
        return powerManager.isInteractive
    }

    override fun getLauncherPackageName(): String? {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val pm: PackageManager = ctx.packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName
    }

    override fun getDefaultDisplay(): MockDisplay {
        return this
    }

    override fun getDisplayMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }
}