package com.example.viewpagernotupdatingcompose.with_logging

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.viewpagernotupdatingcompose.R
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.atomic.AtomicInteger
import androidx.compose.ui.graphics.Color as ComposeColor
import android.graphics.Color as ViewsColor

class ActivityWithLogging : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val switchToFirstPageButton: Button = findViewById(R.id.switch_to_first_page)
        val switchToLastPageButton: Button = findViewById(R.id.switch_to_last_page)

        viewPager.adapter = ViewPagerAdapter(this)

        switchToFirstPageButton.setOnClickListener { viewPager.setCurrentItem(0, true) }
        switchToLastPageButton.setOnClickListener { viewPager.setCurrentItem(5, true) }
    }
}

private class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BlankFragment(ViewsColor.RED)
            1 -> BlankFragment(ViewsColor.GREEN)
            2 -> BlankFragment(ViewsColor.BLACK)
            3 -> BlankFragment(ViewsColor.CYAN)
            4 -> BlankFragment(ViewsColor.GRAY)
            5 -> ComposeFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}

class BlankFragment(private val color: Int) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FrameLayout(container?.context ?: requireContext()).apply {
            setBackgroundColor(color)
        }
    }
}

class ComposeFragment : Fragment() {
    private val instanceId = fragmentInstanceId.getAndIncrement()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        println("Test: $instanceId: creating view")

        return MyComposeView(container?.context ?: requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool) // same as ViewCompositionStrategy.Default
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed) // Fix 1
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle))
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
        }
    }

    override fun onResume() {
        println("Test: $instanceId: resuming")
        super.onResume()
        println("Test: $instanceId: resumed")
    }

    override fun onPause() {
        println("Test: $instanceId: pausing")
        super.onPause()
        println("Test: $instanceId: paused")
    }

    override fun onStart() {
        println("Test: $instanceId: starting")
        super.onStart()
        println("Test: $instanceId: started")
    }

    override fun onStop() {
        println("Test: $instanceId: stopping")
        super.onStop()
        println("Test: $instanceId: stopped")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        println("Test: $instanceId: creating")
        super.onCreate(savedInstanceState)
        println("Test: $instanceId: created")
    }

    override fun onDestroy() {
        println("Test: $instanceId: destroying")
        super.onDestroy()
        println("Test: $instanceId: destroyed")
    }

    private class RememberOververLogger(
        val currentFragmentId: Int,
        val currentViewId: Int
    ) : RememberObserver {
        override fun onAbandoned() {
            println("Test: $currentFragmentId: $currentViewId: RememberLogger: abandoned")
        }

        override fun onForgotten() {
            println("Test: $currentFragmentId: $currentViewId: RememberLogger: forgotten")
        }

        override fun onRemembered() {
            println("Test: $currentFragmentId: $currentViewId: RememberLogger: remembered")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.id = View.generateViewId()
        val currentViewId = view.id

        val data = flowOf(true)

        view as MyComposeView
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                println("Test: $instanceId: $currentViewId: view attached to window")
            }

            override fun onViewDetachedFromWindow(v: View) {
                println("Test: $instanceId: $currentViewId: view detached from window")
            }
        })
        view.setContent {
            remember { RememberOververLogger(instanceId, currentViewId) }

            val dataValue = data.collectAsState(initial = false)

            println("Test: $instanceId: $currentViewId: composing with dataValue = ${dataValue.value}")

            @Composable
            fun TrueComposable() {
                Spacer(
                    modifier = Modifier
                        .background(ComposeColor.Yellow)
                        .logLayout(currentViewId, "True Spacer")
                        .logDraw(currentViewId, "True Spacer")
                )

                println("Test: $instanceId: $currentViewId: composed True Spacer")
            }

            @Composable
            fun FalseComposable() {
                Spacer(
                    modifier = Modifier
                        .background(ComposeColor.Blue)
                        .logLayout(currentViewId, "False Spacer")
                        .logDraw(currentViewId, "False Spacer")
                )

                println("Test: $instanceId: $currentViewId: composed False Spacer")
            }

            val example = Example.DIFFERENT_COMPOSABLES_VISIBLE_ISSUE

            when (example) {
                Example.SAME_COMPOSABLE_NO_ISSUE -> {
                    Spacer(
                        modifier = Modifier
                            .background(if (dataValue.value) ComposeColor.Yellow else ComposeColor.Blue)
                            .logLayout(currentViewId, "Single Spacer")
                            .logDraw(currentViewId, "Single Spacer")
                    )

                    println("Test: $instanceId: $currentViewId: composed Single Spacer")
                }
                Example.DIFFERENT_COMPOSABLES_VISIBLE_ISSUE -> {
                    if (dataValue.value) {
                        TrueComposable()
                    } else {
                        FalseComposable()
                    }
                }
                Example.SAME_COMPOSABLE_INSIDE_MOVING_CONTENT_NO_ISSUE -> {
                    val trueContent = remember { movableContentOf { TrueComposable() } }
                    @Suppress("UNUSED_VARIABLE")
                    val falseContent = remember { movableContentOf { FalseComposable() } }

                    if (dataValue.value) {
                        trueContent.invoke()
                    } else {
                        trueContent.invoke()
                    }
                }
                Example.DIFFERENT_COMPOSABLES_INSIDE_MOVING_CONTENT_VISIBLE_ISSUE -> {
                    val trueContent = remember { movableContentOf { TrueComposable() } }
                    val falseContent = remember { movableContentOf { FalseComposable() } }

                    if (dataValue.value) {
                        trueContent.invoke()
                    } else {
                        falseContent.invoke()
                    }
                }
            }

            println("Test: $instanceId: $currentViewId: composed")
            SideEffect {
                println("Test: $instanceId: $currentViewId: side effect")
            }
        }
    }

    private fun Modifier.logLayout(currentViewId: Int, type: String): Modifier {
        return this.layout { measurable, constraints ->
            println("Test: $instanceId: $currentViewId: $type: measuring")
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                println("Test: $instanceId: $currentViewId: $type: placing")
                placeable.place(0, 0)
            }
        }
    }

    private fun Modifier.logDraw(currentViewId: Int, type: String): Modifier {
        return this.drawBehind {
            println("Test: $instanceId: $currentViewId: $type: drawing")
        }
    }

    override fun onDestroyView() {
        val currentViewId = requireView().id
        println("Test: $instanceId: $currentViewId: destroying view")
        super.onDestroyView()
        println("Test: $instanceId: $currentViewId: view destroyed")
    }

    companion object {
        val fragmentInstanceId = AtomicInteger()
    }
}

private enum class Example {
    SAME_COMPOSABLE_NO_ISSUE,
    DIFFERENT_COMPOSABLES_VISIBLE_ISSUE,
    SAME_COMPOSABLE_INSIDE_MOVING_CONTENT_NO_ISSUE,
    DIFFERENT_COMPOSABLES_INSIDE_MOVING_CONTENT_VISIBLE_ISSUE
}

private class MyComposeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private val content = mutableStateOf<(@Composable () -> Unit)?>(null)

    @Suppress("RedundantVisibilityModifier")
    protected override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    @Composable
    override fun Content() {
        content.value?.invoke()
    }

    override fun getAccessibilityClassName(): CharSequence {
        return javaClass.name
    }

    /**
     * Set the Jetpack Compose UI content for this view.
     * Initial composition will occur when the view becomes attached to a window or when
     * [createComposition] is called, whichever comes first.
     */
    fun setContent(content: @Composable () -> Unit) {
        shouldCreateCompositionOnAttachedToWindow = true
        this.content.value = content
        if (isAttachedToWindow) {
            createComposition()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Fix 2:
        // requestLayout()
    }
}