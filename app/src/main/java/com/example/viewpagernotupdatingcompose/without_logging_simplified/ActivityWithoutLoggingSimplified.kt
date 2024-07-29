package com.example.viewpagernotupdatingcompose.without_logging_simplified

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.collectAsState
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

class ActivityWithoutLoggingSimplified : FragmentActivity() {
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

            if (dataValue.value) {
                Spacer(modifier = Modifier.background(ComposeColor.Yellow))
            } else {
                Spacer(modifier = Modifier.background(ComposeColor.Blue))
            }
        }
    }
}