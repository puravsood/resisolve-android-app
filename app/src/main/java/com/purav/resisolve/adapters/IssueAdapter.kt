package com.purav.resisolve.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.purav.resisolve.databinding.ItemIssueBinding
import com.purav.resisolve.models.Issue
import java.text.SimpleDateFormat
import java.util.*

class IssueAdapter(
    private val issues: MutableList<Issue>,
    private val showReporterInfo: Boolean,
    private val onIssueClick: (Issue) -> Unit
) : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {

    fun updateIssues(newIssues: List<Issue>) {
        issues.clear()
        issues.addAll(newIssues)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val binding = ItemIssueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IssueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(issues[position])
    }

    override fun getItemCount(): Int = issues.size

    inner class IssueViewHolder(
        private val binding: ItemIssueBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(issue: Issue) {
            binding.apply {
                tvIssueTitle.text = issue.title
                tvCategory.text = issue.category
                tvDate.text = formatDate(issue.createdAt)

                // Status chip
                chipStatus.text = issue.status
                chipStatus.setChipBackgroundColorResource(getStatusColor(issue.status))

                // Priority chip
                chipPriority.text = issue.priority
                chipPriority.setChipBackgroundColorResource(getPriorityColor(issue.priority))

                // Reporter info (for admin)
                if (showReporterInfo) {
                    layoutReporterInfo.visibility = View.VISIBLE
                    tvReporterInfo.text = "${issue.reportedByName} • ${issue.reportedByFlat}"
                } else {
                    layoutReporterInfo.visibility = View.GONE
                }

                root.setOnClickListener {
                    onIssueClick(issue)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun getStatusColor(status: String): Int {
            return when (status) {
                "Received" -> android.R.color.holo_blue_light
                "In Progress" -> android.R.color.holo_orange_light
                "Resolved" -> android.R.color.holo_green_light
                "Closed" -> android.R.color.darker_gray
                else -> android.R.color.darker_gray
            }
        }

        private fun getPriorityColor(priority: String): Int {
            return when (priority) {
                "High" -> android.R.color.holo_red_light
                "Medium" -> android.R.color.holo_orange_light
                "Low" -> android.R.color.holo_green_light
                else -> android.R.color.darker_gray
            }
        }
    }
}