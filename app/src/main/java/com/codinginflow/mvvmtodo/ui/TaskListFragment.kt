package com.codinginflow.mvvmtodo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SORT
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.adapter.TaskAdapter
import com.codinginflow.mvvmtodo.databinding.FragmentTaskListBinding
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.codinginflow.mvvmtodo.vm.TaskListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class TaskListFragment : Fragment(R.layout.fragment_task_list), TaskAdapter.OnItemClickListener {

    private var _binding: FragmentTaskListBinding? = null
    private val mBinding: FragmentTaskListBinding
        get() {
            check(_binding != null) {
                "Don't you know fragment view life-cycle"
            }
            return _binding!!
        }

    private val mAdapter: TaskAdapter by lazy { TaskAdapter(this) }
    private val mTaskListViewModel: TaskListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTaskListBinding.bind(view)

        with(mBinding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = mAdapter
        }

        //1-
        mTaskListViewModel.mTask.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.task_list_menu, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.onQueryTextChanged {
            mTaskListViewModel.mQueryAsFlow.value = it
        }

        lifecycleScope.launchWhenStarted {
            menu.findItem(R.id.hide_completed).isChecked =
                    mTaskListViewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_by_date_created -> {
                mTaskListViewModel.onSortOrderSelected(SORT.BY_DATE)
                return true
            }
            R.id.action_sort_by_name -> {
                mTaskListViewModel.onSortOrderSelected(SORT.BY_NAME)
                return true
            }
            R.id.hide_completed -> {
                item.isChecked = !item.isChecked
                mTaskListViewModel.onHideCompletedSelected(item.isChecked)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(task: Task) {
        mTaskListViewModel.onItemClick(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        mTaskListViewModel.onCheckBoxClick(task, isChecked)
    }
}