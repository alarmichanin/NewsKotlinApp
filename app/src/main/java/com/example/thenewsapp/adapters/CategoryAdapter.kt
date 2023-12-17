package com.example.thenewsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thenewsapp.R
import com.example.thenewsapp.models.Category

class CategoryAdapter(private val categoriesList: ArrayList<Category>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private lateinit var categoryClickListener: CategoryClickListener

    class CategoryViewHolder(view: View, private val categoryClickListener: CategoryClickListener): RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.categoryId)
        val categoryIcon: ImageView = view.findViewById(R.id.categoryIcon)

        init {
            view.setOnClickListener {
                categoryClickListener.onCategoryClick(categoryName.text.toString())
            }
        }
    }

    fun setCategoryClickListener(listener: CategoryClickListener) {
        this.categoryClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return CategoryViewHolder(view, categoryClickListener)
    }

    override fun getItemCount(): Int = categoriesList.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.categoryName.text = categoriesList[position].name
        holder.categoryIcon.setImageResource(categoriesList[position].iconPath)
    }
}