package app.tuuure.v2uid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class AppListAdapter(var context: Context) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
    var filter: CharSequence = ""

    var data: LinkedList<AppInfo> = LinkedList()
        get() = LinkedList(field.filter {
            (filter == "") or
                    it.appName.contains(filter, ignoreCase = true) or
                    it.packageName.contains(filter, ignoreCase = true) or
                    it.uid.toString().contains(filter, ignoreCase = true)
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.iconImage.setImageDrawable(item.icon)
        holder.appNameText.text = item.appName
        holder.pkgText.text = item.packageName
        holder.uidText.text = item.uid.toString()
        holder.checkBox.isChecked = item.isChecked
        holder.itemView.setOnClickListener(holder)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val iconImage: ImageView = itemView.findViewById(R.id.image_icon)
        val appNameText: TextView = itemView.findViewById(R.id.text_app_name)
        val pkgText: TextView = itemView.findViewById(R.id.text_pkg_name)
        val uidText: TextView = itemView.findViewById(R.id.text_uid)
        val checkBox: CheckBox = itemView.findViewById(R.id.state_check)

        override fun onClick(view: View?) {
            val wrapperPosition = adapterPosition
            val state = !checkBox.isChecked

            val uid = data[wrapperPosition].uid
            var rangeSmall = wrapperPosition
            var rangeLarge = wrapperPosition
            do {
                data[rangeSmall].isChecked = state

                if (rangeSmall <= 0) {
                    break
                }
                rangeSmall -= 1
            } while (data[rangeSmall].uid == uid)

            do {
                data[rangeLarge].isChecked = state

                if (rangeLarge >= itemCount - 1) {
                    break
                }
                rangeLarge += 1
            } while (data[rangeLarge].uid == uid)

            notifyItemRangeChanged(rangeSmall, rangeLarge - rangeSmall + 1)
        }
    }
}