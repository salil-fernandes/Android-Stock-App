package com.example.stockapp.Util

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.CompanyActivity
import com.example.stockapp.R
import com.example.stockapp.Structures.FavoritesItem
import com.example.stockapp.Structures.PortfolioItem
import java.util.Collections


class FavoritesAdapter(private val stocks: MutableList<FavoritesItem>, private val onReorderOperation: (MutableList<FavoritesItem>) -> Unit, private val onSwipeDeleteOperation: (MutableList<FavoritesItem>) -> Unit) :
    RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tickerText: TextView = view.findViewById(R.id.watchlistTicker)
        val companyText: TextView = view.findViewById(R.id.watchlistCompanyName)
        val priceText: TextView = view.findViewById(R.id.watchlistCurrentPrice)
        val changeText: TextView = view.findViewById(R.id.watchlistChangeText)
        val rightArrowButton: TextView = view.findViewById(R.id.rightArrow2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorites_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = stocks[position]
        holder.tickerText.text = item.ticker
        holder.companyText.text = item.companyName
        holder.priceText.text = "$" + item.currentPrice
        if(item.changePercentage > 0) {
            holder.changeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            holder.changeText.text = "${item.changeAmount} (${item.changePercentage})"
            val upwardTrend = ContextCompat.getDrawable(holder.itemView.context, R.drawable.trending_up)
            upwardTrend?.setBounds(0, 0, upwardTrend.intrinsicWidth, upwardTrend.intrinsicHeight)
            holder.changeText.setCompoundDrawables(upwardTrend, null, null, null) // Set to right
            holder.changeText.compoundDrawablePadding = 8
        } else if(item.changePercentage < 0) {
            holder.changeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            holder.changeText.text = "${item.changeAmount} (${item.changePercentage})"
            val downwardTrend = ContextCompat.getDrawable(holder.itemView.context, R.drawable.trending_down)
            downwardTrend?.setBounds(0, 0, downwardTrend.intrinsicWidth, downwardTrend.intrinsicHeight)
            holder.changeText.setCompoundDrawables(downwardTrend, null, null, null) // Set to right
            holder.changeText.compoundDrawablePadding = 8
        } else {
            holder.changeText.text = "${item.changeAmount} (${item.changePercentage})"
        }

        holder.rightArrowButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CompanyActivity::class.java).apply {
                putExtra("ticker", item.ticker)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return stocks.size
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(stocks, i, i + 1)
            }

        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(stocks, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun itemDrop(position: Int) {
        onReorderOperation(stocks)
    }

    fun updateData(newStocks: List<FavoritesItem>) {
        stocks.clear()
        stocks.addAll(newStocks)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        stocks.removeAt(position)
        notifyItemRemoved(position)
        onSwipeDeleteOperation(stocks)
    }
}