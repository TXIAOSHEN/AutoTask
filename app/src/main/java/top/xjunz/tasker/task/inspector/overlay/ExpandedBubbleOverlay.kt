package top.xjunz.tasker.task.inspector.overlay

import android.annotation.SuppressLint
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import top.xjunz.tasker.R
import top.xjunz.tasker.databinding.OverlayBubbleExpandedBinding
import top.xjunz.tasker.ktx.*
import top.xjunz.tasker.task.inspector.FloatingInspector

/**
 * @author xjunz 2022/10/17
 */
class ExpandedBubbleOverlay(inspector: FloatingInspector) :
    FloatingInspectorOverlay<OverlayBubbleExpandedBinding>(inspector) {

    override fun modifyLayoutParams(base: WindowManager.LayoutParams) {
        super.modifyLayoutParams(base)
        base.x = vm.bubbleX
        base.y = vm.bubbleY
    }

    override fun onOverlayInflated() {
        super.onOverlayInflated()
        binding.apply {
            ibBottom.asPointerController(KeyEvent.KEYCODE_DPAD_DOWN)
            ibLeft.asPointerController(KeyEvent.KEYCODE_DPAD_LEFT)
            ibRight.asPointerController(KeyEvent.KEYCODE_DPAD_RIGHT)
            ibTop.asPointerController(KeyEvent.KEYCODE_DPAD_UP)
            draggableRoot.onDragListener = { _, offsetX, offsetY ->
                offsetViewInWindow(offsetX.toInt(), offsetY.toInt())
                vm.bubbleX = layoutParams.x
                vm.bubbleY = layoutParams.y
            }
            ibConfirm.setOnClickListener {
                if (vm.emphaticNode.isNull()) {
                    vm.makeToast(R.string.no_node_selected)
                    return@setOnClickListener
                }
                vm.showNodeInfo.value = true
            }
            ibLayers.setOnClickListener {
                vm.showNodeTree.value = true
            }
            ibCenter.setOnClickListener {
                vm.showGamePad.value = false
            }
            ibCollapse.setOnClickListener {
                vm.isCollapsed.toggle()
            }
            ibGamePad.setOnClickListener {
                vm.showGamePad.value = true
                vm.makeToast(R.string.pointer_controller_enabled)
            }
            ibPinScreenshot.setOnClickListener {
                vm.pinScreenShot.toggle()
                if (vm.pinScreenShot.isTrue) {
                    vm.makeToast(R.string.pin_screenshot)
                } else {
                    vm.makeToast(R.string.cancel_pin_screenshot)
                }
            }
            ibShowGrid.setOnClickListener {
                vm.showGrids.toggle()
                if (vm.showGrids.isTrue) {
                    vm.makeToast(R.string.show_node_bounds)
                } else {
                    vm.makeToast(R.string.cancel_show_node_bounds)
                }
            }
            inspector.observe(vm.isCollapsed) {
                if (!it) {
                    layoutParams.x = vm.bubbleX
                    layoutParams.y = vm.bubbleY
                    if (root.isAttachedToWindow) windowManager.updateViewLayout(root, layoutParams)
                }
                root.isVisible = !it
            }
            inspector.observe(vm.showGamePad) {
                gamePad.isVisible = it
                ibGamePad.isEnabled = !it
            }
            inspector.observe(vm.pinScreenShot) {
                if (it) {
                    ibPinScreenshot.setImageResource(R.drawable.ic_outline_image_24)
                } else {
                    ibPinScreenshot.setImageResource(R.drawable.ic_outline_hide_image_24)
                }
            }
            inspector.observe(vm.showGrids) {
                if (it) {
                    ibShowGrid.setImageResource(R.drawable.ic_baseline_grid_on_24)
                } else {
                    ibShowGrid.setImageResource(R.drawable.ic_baseline_grid_off_24)
                }
            }
            inspector.observe(vm.currentMode) {
                when (it) {
                    FloatingInspector.MODE_UI_OBJECT -> {
                        ibPinScreenshot.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                        ibLayers.isVisible = true
                        ibGamePad.isVisible = true
                        ibShowGrid.isVisible = true
                    }
                    FloatingInspector.MODE_COMPONENT -> {
                        ibPinScreenshot.isVisible = false
                        ibGamePad.isVisible = false
                        vm.showGamePad.value = false
                        ibLayers.isVisible = false
                        ibShowGrid.isVisible = false
                    }
                    FloatingInspector.MODE_COORDINATE -> {
                        ibPinScreenshot.isVisible = false
                        ibGamePad.isVisible = true
                        ibLayers.isVisible = false
                        ibShowGrid.isVisible = false
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.asPointerController(keyCode: Int) {
        setOnLongClickListener l@{
            vm.onKeyLongPressed.value = keyCode
            return@l keyCode >= KeyEvent.KEYCODE_DPAD_UP && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT
        }
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                vm.onKeyUpOrCancelled.value = keyCode
            }
            return@setOnTouchListener false
        }
    }
}