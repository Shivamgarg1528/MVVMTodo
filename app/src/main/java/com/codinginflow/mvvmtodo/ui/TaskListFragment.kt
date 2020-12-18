package com.codinginflow.mvvmtodo.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.adapter.TaskAdapter
import com.codinginflow.mvvmtodo.databinding.FragmentTaskListBinding
import com.codinginflow.mvvmtodo.vm.TaskListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null
    private val mBinding: FragmentTaskListBinding
        get() {
            check(_binding != null) {
                "Don't you know fragment view life-cycle"
            }
            return _binding!!
        }

    private val mAdapter: TaskAdapter by lazy { TaskAdapter() }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}