package com.example.stockapp.Util

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.R
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsAdapter(private val newsList: JSONArray) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LARGE = 0
        private const val VIEW_TYPE_NORMAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_LARGE else VIEW_TYPE_NORMAL
    }

    class LargeNewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.newsImage)
        val headline: TextView = view.findViewById(R.id.newsHeadline)
        val source: TextView = view.findViewById(R.id.newsSource)
        val time: TextView = view.findViewById(R.id.newsTime)
    }

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.newsImage)
        val headline: TextView = view.findViewById(R.id.newsHeadline)
        val source: TextView = view.findViewById(R.id.newsSource)
        val time: TextView = view.findViewById(R.id.newsTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            VIEW_TYPE_LARGE -> {
                val largeView = inflater.inflate(R.layout.news_item_large, parent, false)
                LargeNewsViewHolder(largeView)
            }
            else -> {
                val normalView = inflater.inflate(R.layout.news_item_normal, parent, false)
                NewsViewHolder(normalView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = newsList.getJSONObject(position)
        val context = holder.itemView.context

        when (holder) {
            is LargeNewsViewHolder -> {
                holder.headline.text = newsItem.getString("headline")
                holder.source.text = "${newsItem.getString("source")}"
                holder.time.text = "${getRelativeTimePast(newsItem.getLong("datetime"))}"
                Picasso.get().load(newsItem.getString("image"))
                    .resize(1024, 768)
                    .centerInside()
                    .into(holder.imageView)
            }
            is NewsViewHolder -> {
                holder.headline.text = newsItem.getString("headline")
                holder.source.text = "${newsItem.getString("source")}"
                holder.time.text = "${getRelativeTimePast(newsItem.getLong("datetime"))}"
                Picasso.get().load(newsItem.getString("image"))
                    .resize(1024, 768)
                    .centerInside()
                    .into(holder.imageView)
            }
        }

        holder.itemView.setOnClickListener {
            showNewsDialog(holder.itemView.context, newsItem)
        }
    }

    override fun getItemCount(): Int {
        return newsList.length()
    }

    private fun getRelativeTimePast(timestamp: Long): String {
        val timeDifference = System.currentTimeMillis() - (timestamp * 1000)
        val seconds = timeDifference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes minutes ago"
            hours < 1 -> "1 hour ago"
            hours < 24 -> "$hours hours ago"
            hours < 48 -> "1 day ago"
            days > 1 -> "$days days ago"
            else -> SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(timestamp * 1000))
        }
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }

    private fun showNewsDialog(context: Context, newsItem: JSONObject) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.news_dialog)

        val source: TextView = dialog.findViewById(R.id.newsDialogSource)
        val date: TextView = dialog.findViewById(R.id.newsDialogDate)
        val headline: TextView = dialog.findViewById(R.id.newsDialogHeadline)
        val summary: TextView = dialog.findViewById(R.id.newsDialogSummary)
        val chromeIcon: ImageView = dialog.findViewById(R.id.newsDialogChromeIcon)
        val XIcon: ImageView = dialog.findViewById(R.id.newsDialogXIcon)
        val facebookIcon: ImageView = dialog.findViewById(R.id.newsDialogFacebookIcon)

        source.text = newsItem.getString("source")
        date.text = formatDate(newsItem.getLong("datetime"))
        headline.text = newsItem.getString("headline")
        summary.text = newsItem.getString("summary")
        chromeIcon.setOnClickListener {
            val url = newsItem.getString("url")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
        XIcon.setOnClickListener {
            val tweetText = URLEncoder.encode(headline.text.toString(), "UTF-8")
            val tweetUrl = URLEncoder.encode(newsItem.getString("url"), "UTF-8")
            val tweetIntentUrl = "https://twitter.com/intent/tweet?text=$tweetText&url=$tweetUrl"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetIntentUrl))
            context.startActivity(intent)
        }
        facebookIcon.setOnClickListener {
            val shareUrl = URLEncoder.encode(newsItem.getString("url"), "UTF-8")
            val facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=$shareUrl&src=sdkpreparse"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
            context.startActivity(intent)
        }

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(1300, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }

}
