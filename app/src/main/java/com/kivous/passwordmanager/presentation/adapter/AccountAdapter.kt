package com.kivous.passwordmanager.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kivous.passwordmanager.databinding.ListAccountBinding
import com.kivous.passwordmanager.domain.model.Account

class AccountAdapter(
    private val viewController: (ViewHolder, account: Account) -> Unit,
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    class ViewHolder(val binding: ListAccountBinding) : RecyclerView.ViewHolder(binding.root)

    val differ = AsyncListDiffer(this, Comparator)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = differ.currentList[position]
        viewController(holder, account)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

object Comparator : DiffUtil.ItemCallback<Account>() {
    override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean =
        oldItem == newItem
}