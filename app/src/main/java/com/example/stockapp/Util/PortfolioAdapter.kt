package com.example.stockapp.Util

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.CompanyActivity
import com.example.stockapp.MainActivity
import com.example.stockapp.R
import com.example.stockapp.Structures.PortfolioItem
import java.util.Collections


class PortfolioAdapter(private val stocks: MutableList<PortfolioItem>, private val onReorderOperation: (MutableList<PortfolioItem>) -> Unit) :
    RecyclerView.Adapter<PortfolioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tickerText: TextView = view.findViewById(R.id.portfolioTicker)
        val sharesText: TextView = view.findViewById(R.id.portfolioNumShares)
        val marketValueText: TextView = view.findViewById(R.id.portfolioMarketValue)
        val changeText: TextView = view.findViewById(R.id.portfolioChangeText)
        val rightArrowButton: TextView = view.findViewById(R.id.rightArrow1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.portfolio_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = stocks[position]
        holder.tickerText.text = item.ticker
        holder.sharesText.text = if (item.shareCount == 1) "1 share" else "${item.shareCount} shares"
        holder.marketValueText.text = "$" + String.format("%.2f", item.marketValue)
        if(item.changePercentage > 0) {
            holder.changeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            holder.changeText.text = "$${String.format("%.2f",item.changeAmount)} ( ${String.format("%.2f", item.changePercentage)}% )"
            val upwardTrend = ContextCompat.getDrawable(holder.itemView.context, R.drawable.trending_up)
            upwardTrend?.setBounds(0, 0, upwardTrend.intrinsicWidth, upwardTrend.intrinsicHeight)
            holder.changeText.setCompoundDrawables(upwardTrend, null, null, null)
            holder.changeText.compoundDrawablePadding = 8
        } else if(item.changePercentage < 0) {
            holder.changeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            holder.changeText.text = "$${String.format("%.2f", item.changeAmount)} ( ${String.format("%.2f", item.changePercentage)}% )"
            val downwardTrend = ContextCompat.getDrawable(holder.itemView.context, R.drawable.trending_down)
            downwardTrend?.setBounds(0, 0, downwardTrend.intrinsicWidth, downwardTrend.intrinsicHeight)
            holder.changeText.setCompoundDrawables(downwardTrend, null, null, null)
            holder.changeText.compoundDrawablePadding = 8
        } else {
            holder.changeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.changeText.text = "$${String.format("%.2f", item.changeAmount)} ( ${String.format("%.2f", item.changePercentage)}% )"
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

    fun updateData(newStocks: List<PortfolioItem>) {
        stocks.clear()
        stocks.addAll(newStocks)
        notifyDataSetChanged()
    }
}
