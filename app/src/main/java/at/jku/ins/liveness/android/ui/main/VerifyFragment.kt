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
import at.jku.ins.liveness.android.databinding.FragmentVerifyBinding

//import at.jku.ins.liveness.protocol.ChallengeMessage

import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.client.WebTarget
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response


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

        val verifyBtn = binding.buttonVerify as Button
        verifyBtn.setOnClickListener {
            startVerify(root)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun startVerify(view: View) {
        pageViewModel.setText("Starting request ...")

        val serverUrl = "https://192.168.64.22:8080/liveness"
        val client = ClientBuilder.newClient();
        val livenessTarget = client.target(serverUrl);
        val challengeTarget = livenessTarget.path("challenge")
        val challengeBuilder = challengeTarget.request(MediaType.APPLICATION_JSON)
        /*val res = challengeBuilder.get(Response::class.java)
        val cookies = res.cookies
        //val challenge: ChallengeMessage = res.readEntity(ChallengeMessage::class.java)*/

        pageViewModel.addLine(challengeBuilder.toString())
    }
}

// code from https://developer.android.com/kotlin/coroutines
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
