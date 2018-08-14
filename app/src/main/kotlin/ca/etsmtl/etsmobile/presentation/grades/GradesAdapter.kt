package ca.etsmtl.etsmobile.presentation.grades

import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ca.etsmtl.etsmobile.R
import ca.etsmtl.etsmobile.presentation.grades.GradesAdapter.CourseGradeViewHolder.GradeViewHolder
import ca.etsmtl.etsmobile.presentation.grades.GradesAdapter.CourseGradeViewHolder.HeaderViewHolder
import ca.etsmtl.repository.data.model.Cours
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.header_grade_course.tvSessionGrades
import kotlinx.android.synthetic.main.item_grade_course.tvCourseGrade
import kotlinx.android.synthetic.main.item_grade_course.tvCourseSigle

/**
 * Created by Sonphil on 12-08-18.
 */

class GradesAdapter : RecyclerView.Adapter<GradesAdapter.CourseGradeViewHolder>() {

    val differ = AsyncListDiffer<Any>(this, object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is String && newItem is String ->
                    oldItem == newItem
                oldItem is Cours && newItem is Cours ->
                    oldItem.sigle == newItem.sigle && oldItem.groupe == newItem.groupe && oldItem.session == newItem.session
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
    })

    var items: Map<String, List<Cours>> = emptyMap()
        set(value) {
            field = value
            differ.submitList(mutableListOf<Any>().apply {
                value.forEach {
                    this.add(it.key)
                    it.value.forEach { cours ->
                        this.add(cours)
                    }
                }
            })
        }

    init {
        differ.submitList(emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseGradeViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.item_grade_course -> GradeViewHolder(
                    inflater.inflate(viewType, parent, false)
            )
            R.layout.header_grade_course -> HeaderViewHolder(
                    inflater.inflate(viewType, parent, false)
            )
            else -> {
                throw IllegalStateException("Unknown viewType $viewType")
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is String -> R.layout.header_grade_course
            is Cours -> R.layout.item_grade_course
            else -> throw IllegalStateException("Unknown view type at position $position")
        }
    }

    override fun onBindViewHolder(holder: CourseGradeViewHolder, position: Int) {
        when (holder) {
            is GradeViewHolder -> {
                with(differ.currentList[position] as Cours) {
                    holder.gradeTextView.text = when {
                        !this.cote.isNullOrEmpty() -> this.cote
                        !this.noteSur100.isNullOrEmpty() -> this.noteSur100 + " %"
                        else -> "--"
                    }

                    holder.sigleTextView.text = this.sigle
                }
            }
            is HeaderViewHolder -> {
                holder.sessionGradesTextView.text = differ.currentList[position] as String
            }
        }
    }

    sealed class CourseGradeViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        class GradeViewHolder(override val containerView: View) : CourseGradeViewHolder(containerView) {
            val gradeTextView: TextView = tvCourseGrade
            val sigleTextView: TextView = tvCourseSigle
        }

        class HeaderViewHolder(override val containerView: View) : CourseGradeViewHolder(containerView) {
            val sessionGradesTextView: TextView = tvSessionGrades
        }
    }
}