package com.example.ripetomatoes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val loginRootView = inflater.inflate(R.layout.fragment_login, container, false)
        val buttonView = loginRootView.findViewById<Button>(R.id.loginButton)
        buttonView.setOnClickListener { _ ->
            // When login button is clicked
            val username = loginRootView.findViewById<EditText>(R.id.usernameBox).text.toString()
            // Validate username input
            if (username.isBlank()) {
                (activity as MainActivity).showToast("Invalid username", true)
                return@setOnClickListener
            }
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, "http://10.0.0.43:8080/api/v1/login", JSONObject("{ 'username': '$username' }"),
                // On Volley success
                Response.Listener { response ->
                    val mainActivity = activity as MainActivity
                    mainActivity.jwtToken = response.getString("token")
                    mainActivity.showToast("Hello, $username.")
                },
                // On Volley failure
                Response.ErrorListener { error ->
                    (activity as MainActivity).showToast("Failed to login. ${error.message}", true)
                }
            )
            RequestManager.getInstance(this.requireContext()).addToRequestQueue(jsonObjectRequest)
        }
        return loginRootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}
