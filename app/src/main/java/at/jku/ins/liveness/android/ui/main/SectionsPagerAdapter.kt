package at.jku.ins.liveness.android.ui.main

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import at.jku.ins.liveness.android.R
import at.jku.ins.liveness.android.data.ProtocolRunDataRepository
import at.jku.ins.liveness.android.data.SendProtocolRun
import at.jku.ins.liveness.android.data.VerifyProtocolRun

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    // keep the view fragments as singletons
    private val fragments = arrayOf<ViewFragment>(
        VerifyFragment(VerifyProtocolRun()),
        // TODO: this passing the repository to SendProtocolRun is a bit of a hack, maybe solve better?
        SendFragment(SendProtocolRun(ProtocolRunDataRepository.getInstance(context))))

    override fun getItem(position: Int): ViewFragment {
        return when (position) {
            0 -> fragments[0]
            else -> fragments[1]
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}