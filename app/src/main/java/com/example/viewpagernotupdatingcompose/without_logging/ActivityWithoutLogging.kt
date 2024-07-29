package com.example.viewpagernotupdatingcompose.without_logging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.viewpagernotupdatingcompose.R
import kotlinx.coroutines.flow.flowOf
import android.graphics.Color as ViewsColor
import androidx.compose.ui.graphics.Color as ComposeColor

class ActivityWithoutLogging : FragmentActivity() {
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(container?.context ?: requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = flowOf(true)

        view as ComposeView
        view.setContent {
            val dataValue = data.collectAsState(initial = false)

            @Composable
            fun TrueComposable() {
                Spacer(modifier = Modifier.background(ComposeColor.Yellow))
            }

            @Composable
            fun FalseComposable() {
                Spacer(modifier = Modifier.background(ComposeColor.Blue))
            }

            val example = Example.DIFFERENT_COMPOSABLES_VISIBLE_ISSUE

            when (example) {
                Example.SAME_COMPOSABLE_NO_ISSUE -> {
                    Spacer(
                        modifier = Modifier
                            .background(if (dataValue.value) ComposeColor.Yellow else ComposeColor.Blue)
                    )
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
        }
    }
}

private enum class Example {
    SAME_COMPOSABLE_NO_ISSUE,
    DIFFERENT_COMPOSABLES_VISIBLE_ISSUE,
    SAME_COMPOSABLE_INSIDE_MOVING_CONTENT_NO_ISSUE,
    DIFFERENT_COMPOSABLES_INSIDE_MOVING_CONTENT_VISIBLE_ISSUE
}