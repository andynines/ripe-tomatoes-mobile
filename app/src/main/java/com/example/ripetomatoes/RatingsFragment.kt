package com.example.ripetomatoes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

class RatingsAdapter(private val titles: ArrayList<String>, private val ratings: ArrayList<String>,
                     private val context: Context) : RecyclerView.Adapter<RatingsAdapter.ViewHolder>() {

    private var statusCode: Int? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleView: TextView = view.findViewById(R.id.titleView) as TextView
        var ratingView: TextView = view.findViewById(R.id.ratingView) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )
    }

    private fun calcRatingColor(rating: String): Int {
        val numericRatingMap = rating.toInt() * 21
        return Color.rgb(255 - numericRatingMap, numericRatingMap, 0)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = titles[position]
        holder.ratingView.text = ratings[position]
        holder.ratingView.setBackgroundColor(calcRatingColor(ratings[position]))
        // Click behavior for ratings
        holder.ratingView.setOnClickListener { _ ->
            val currentRating = (holder.ratingView.text as String).toInt()
            val incRating = (if (currentRating == 10) 1 else currentRating + 1).toString()
            val mainActivity = context as MainActivity
            val jsonObjectRequest = object: JsonObjectRequest(
                Method.PUT, "http://10.0.0.43:8080/api/v1/update",
                JSONObject("{ 'name': '${titles[position]}', 'updatedRating': '$incRating' }"),
                // On Volley success
                Response.Listener { response ->
                    if (statusCode == 200) {
                        mainActivity.showToast("Rating updated")
                    } else {
                        mainActivity.showToast("Status $statusCode: $response", true)
                    }
                },
                // On Volley failure
                Response.ErrorListener { error ->
                    if (error.networkResponse != null) {
                        mainActivity.showToast(
                            "Status ${error.networkResponse.statusCode}: $error",
                            true
                        )
                    } else {
                        mainActivity.showToast("$error", true)
                    }
                }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json"
                    headers["Authorization"] = "Bearer " + mainActivity.jwtToken
                    return headers
                }
                override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject>? {
                    statusCode = response?.statusCode
                    return super.parseNetworkResponse(response)
                }
            }
            RequestManager.getInstance(mainActivity).addToRequestQueue(jsonObjectRequest)
            holder.ratingView.text = incRating
            holder.ratingView.setBackgroundColor(calcRatingColor(incRating))
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    fun updateData(newTitles: ArrayList<String>, newRatings: ArrayList<String>) {
        titles.clear()
        ratings.clear()
        notifyDataSetChanged()
        titles.addAll(newTitles)
        ratings.addAll(newRatings)
        notifyDataSetChanged()
    }
}

/**
 * A simple [Fragment] subclass.
 * Use the [RatingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RatingsFragment : Fragment() {
    private var statusCode: Int? = null

    private var titles = ArrayList<String>()
    private var ratings = ArrayList<String>()

    private lateinit var ratingsRootView: View

    private fun isValidRating(rating: String): Boolean {
        val numericRating = rating.toIntOrNull() ?: return false
        return (numericRating.toString() == rating) && (1 <= numericRating) && (numericRating <= 10)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ratingsRootView = inflater.inflate(R.layout.fragment_ratings, container, false)
        // Set up seek bar
        val ratingView = ratingsRootView.findViewById<SeekBar>(R.id.ratingView)
        ratingView.setOnSeekBarChangeListener(
            object: SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    ratingsRootView.findViewById<TextView>(R.id.ratingQualifierView).text =
                        "$progress out of 10"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        ratingView.progress = 5
        // When submit button clicked
        val submitButtonView = ratingsRootView.findViewById<Button>(R.id.submitButtonView)
        submitButtonView.setOnClickListener { _ ->
            // Validate title and rating
            val titleView = ratingsRootView.findViewById<EditText>(R.id.titleView)
            val title = titleView.text.toString()
            val rating = ratingView.progress.toString()
            titleView.setText("")
            ratingView.progress = 5
            val mainActivity = activity as MainActivity
            if (title.isBlank()) {
                mainActivity.showToast("No title provided", true)
                return@setOnClickListener
            } else if (rating.isBlank()) {
                mainActivity.showToast("No rating provided", true)
                return@setOnClickListener
            } else if (!isValidRating(rating)) {
                mainActivity.showToast("Invalid rating", true)
                return@setOnClickListener
            }
            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, "http://10.0.0.43:8080/api/v1/films",
                JSONObject("{ 'name': '$title', 'rating': '$rating' }"),
                // On Volley success
                Response.Listener { response ->
                    if (statusCode == 200) {
                        mainActivity.showToast("Title submitted")
                    } else {
                        mainActivity.showToast("Status $statusCode: $response", true)
                    }
                },
                // On Volley failure
                Response.ErrorListener { error ->
                    mainActivity.showToast("Status ${error.networkResponse.statusCode}: $error", true)
                }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json"
                    headers["Authorization"] = "Bearer " + (activity as MainActivity).jwtToken
                    return headers
                }
                override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject>? {
                    statusCode = response?.statusCode
                    return super.parseNetworkResponse(response)
                }
            }
            RequestManager.getInstance(this.requireContext()).addToRequestQueue(jsonObjectRequest)
        }
        return ratingsRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up ratings list
        val ratingsAdapter = RatingsAdapter(titles, ratings, (activity as MainActivity))
        val ratingsListView = ratingsRootView.findViewById<RecyclerView>(R.id.ratingsList)
        ratingsListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ratingsAdapter
        }
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val displayMetrics = DisplayMetrics()
        (activity as MainActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        swipeRefresh.setDistanceToTriggerSync(displayMetrics.heightPixels / 3)
        // When pulled
        swipeRefresh.setOnRefreshListener {
            val jsonArrayRequest = object: JsonArrayRequest(
                Method.GET, "http://10.0.0.43:8080/api/v1/films", null,
                // On Volley success
                Response.Listener { response ->
                    if ((statusCode == 200) || (statusCode == 304)) {
                        val newTitles = ArrayList<String>()
                        val newRatings = ArrayList<String>()
                        for (i in 0 until response.length()) {
                            val currentJSONObject = response.getJSONObject(i)
                            newTitles.add(currentJSONObject.getString("name"))
                            newRatings.add(currentJSONObject.getString("rating"))
                        }
                        ratingsAdapter.updateData(newTitles, newRatings)
                    } else {
                        (activity as MainActivity).showToast("Status $statusCode: $response", true)
                    }
                },
                // On Volley failure
                Response.ErrorListener { error ->
                    val statusCode = if (error.networkResponse != null) error.networkResponse.statusCode else 503
                    (activity as MainActivity).showToast("Status ${statusCode}: $error", true)
                }
            )
            {
                override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONArray>? {
                    statusCode = response?.statusCode
                    return super.parseNetworkResponse(response)
                }
            }
            RequestManager.getInstance(this.requireContext()).addToRequestQueue(jsonArrayRequest)
            swipeRefresh.isRefreshing = false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment RatingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = RatingsFragment()
    }
}
