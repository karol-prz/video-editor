package com.kpchuck.videoeditor.controllers

import android.content.Context
import android.text.InputType
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.kpchuck.videoeditor.R
import me.relex.circleindicator.CircleIndicator2
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout


class AttributeViewController(attributeView: LinearLayout, context: Context, val attributes: Array<String>, val useKeyframes: Boolean = false) {

    private val recyclerView = attributeView.findViewById<RecyclerView>(R.id.attributeRecyclerView)
    private val circleIndicator = attributeView.findViewById<CircleIndicator2>(R.id.attributeIndicator)
    private val layoutManager: LinearLayoutManager

    init {
        val adapter = SimplePagerAdapter(attributes)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(recyclerView)

        circleIndicator.attachToRecyclerView(recyclerView, pagerSnapHelper)
        // optional
        adapter.registerAdapterDataObserver(circleIndicator.adapterDataObserver)
    }

    fun getAttributesAt(position: Int): ArrayMap<String, Int> {
        val array = ArrayMap<String, Int>()
        val view = layoutManager.findViewByPosition(position)
        for (attr in attributes){
            val editText = view?.findViewWithTag<TextInputEditText>(attr)
            array[attr] = editText?.text.toString().toInt()
        }
        return array
    }


    inner class SimplePagerAdapter(private val attributes: Array<String>)  : RecyclerView.Adapter<SimplePagerAdapter.MyViewHolder>() {

        private val dataSet = arrayOf("Start", "End")


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        inner class MyViewHolder(view: LinearLayout, val rootView: LinearLayout, val titleView: TextView) :
            RecyclerView.ViewHolder(view)


        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            // create a new view
            val view = LayoutInflater.from(parent.context).inflate(R.layout.attribute_selector_layout, parent, false) as LinearLayout
            var container = LinearLayout(parent.context)
            for ((i, attr) in attributes.withIndex()){
                if (i % 3 == 0) {
                    container = newContainer(parent.context)
                    view.addView(container)
                }
                container.addView(newEditText(parent.context, attr))
            }
            return MyViewHolder(view, view, view.findViewById(R.id.attributeTitle))
        }

        private fun newEditText(context: Context, hint: String): TextInputLayout {
            val textInputLayout = TextInputLayout(context)
            textInputLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val textInputEditText = TextInputEditText(context)
            textInputEditText.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textInputEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            textInputEditText.tag = hint
            textInputEditText.hint = hint
            textInputLayout.addView(textInputEditText)
            return textInputLayout
        }

        private fun newContainer(context: Context): LinearLayout {
            val container = LinearLayout(context)
            container.orientation = LinearLayout.HORIZONTAL
            container.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            return container
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.titleView.text = "${dataSet[position]} (Pixels)"
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }
}