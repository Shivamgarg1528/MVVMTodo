package com.codinginflow.mvvmtodo.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SORT
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.adapter.TaskAdapter
import com.codinginflow.mvvmtodo.databinding.FragmentTaskListBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.codinginflow.mvvmtodo.vm.TaskListViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = mAdapter.currentList[viewHolder.adapterPosition]
                    mTaskListViewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recyclerView)

            fabAddTask.setOnClickListener {
                mTaskListViewModel.addTaskButtonClicked()
            }
        }

        //1-
        mTaskListViewModel.mTasks.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
        }

        //2- attaching task-events
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mTaskListViewModel.mTaskEventsFlow.collectLatest { event ->
                when (event) {
                    is TaskListViewModel.TasksEvent.OnTaskDeletedEvent -> {
                        Snackbar
                            .make(requireView(), "Task Deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                mTaskListViewModel.onUndoDeleteClick(event.task)
                            }
                            .show()
                    }
                    is TaskListViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val editTaskAction =
                            TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment2(
                                event.task
                            )
                        findNavController().navigate(editTaskAction)
                    }
                    TaskListViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val addTaskAction =
                            TaskListFragmentDirections.actionTaskListFragmentToAddEditTaskFragment2()
                        findNavController().navigate(addTaskAction)
                    }
                    is TaskListViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar
                            .make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                            .show()
                    }
                }.exhaustive
            }
        }

        //3- fetch result
        setFragmentResultListener("add_edit_request") { _: String, bundle: Bundle ->
            val result = bundle.getInt("add_edit_result")
            mTaskListViewModel.onAddEditResult(result)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.task_list_menu, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.onQueryTextChanged {
            mTaskListViewModel.onQueryTextChanged(it)
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