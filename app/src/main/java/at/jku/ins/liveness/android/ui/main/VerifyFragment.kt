package at.jku.ins.liveness.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import at.jku.ins.liveness.android.data.SendProtocolRun
import at.jku.ins.liveness.android.data.VerifyProtocolRun
import at.jku.ins.liveness.android.databinding.FragmentVerifyBinding

/**
 * A placeholder fragment containing a simple view.
 */
class VerifyFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentVerifyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java) /*.apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentVerifyBinding.inflate(inflater, container, false)
        val root = binding.root
        val textView: TextView = binding.verifyLogView
        pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        /*val verifyBtn = binding.buttonVerify as Button
        verifyBtn.setOnClickListener {
            startVerify(root)
        }*/

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*fun startVerify(view: View) {
        pageViewModel.setText("Starting request ...")
        pageViewModel.runNetworkRequest(VerifyProtocolRun())
    }*/

    companion object {
        private var singleton: VerifyFragment = VerifyFragment()

        @JvmStatic
        fun getInstance(): VerifyFragment {
            /*if (singleton == null)
                singleton = VerifyFragment()*/

            return singleton as VerifyFragment
        }

        fun getModel(): PageViewModel {
            return singleton.pageViewModel
        }

        fun startAction() {
            getModel().setText("Starting request ...")
            getModel().runNetworkRequest(VerifyProtocolRun())
        }
    }
}
