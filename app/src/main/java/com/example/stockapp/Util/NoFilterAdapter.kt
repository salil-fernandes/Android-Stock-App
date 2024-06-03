package com.example.stockapp.Util

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterAdapter<T>(context: Context, layout: Int, list: List<T>) : ArrayAdapter<T>(context, layout, list) {
    private var items: List<T> = list

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): T? = items[position]

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = items
                results.count = items.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}