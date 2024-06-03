package com.example.stockapp.Util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.stockapp.R

class MovementAdapter(adapter: RecyclerView.Adapter<*>, private val context: Context, dragDirs: Int, swipeDirs: Int) :
    ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    private val myAdapter = adapter

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        if(myAdapter is PortfolioAdapter) {
            myAdapter.moveItem(fromPosition, toPosition)
        } else if(myAdapter is FavoritesAdapter) {
            myAdapter.moveItem(fromPosition, toPosition)
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            if (myAdapter is FavoritesAdapter) {
                myAdapter.deleteItem(position)
            }
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true // Enable drag on long press
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if(myAdapter is PortfolioAdapter) {
            myAdapter.itemDrop(viewHolder.adapterPosition)
        } else if(myAdapter is FavoritesAdapter) {
            myAdapter.itemDrop(viewHolder.adapterPosition)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val paint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.red)
            }
            val icon = ContextCompat.getDrawable(context, R.drawable.delete)
            val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
            val iconBottom = iconTop + icon.intrinsicHeight

            val iconLeft: Int
            val iconRight: Int

            if (dX < 0) {
                val backgroundLeft = itemView.right.toFloat() + dX
                val backgroundRight = itemView.right.toFloat()

                c.drawRect(backgroundLeft, itemView.top.toFloat(), backgroundRight, itemView.bottom.toFloat(), paint)

                iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                iconRight = itemView.right - iconMargin

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}