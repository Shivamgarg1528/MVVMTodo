package com.codinginflow.mvvmtodo.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.AddEditTaskFragmentBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.vm.AddEditViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.add_edit_task_fragment) {

    private val mAddEditTaskVM: AddEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AddEditTaskFragmentBinding.bind(view)
        binding.apply {
            // task name
            editTextTaskName.setText(mAddEditTaskVM.taskName)
            editTextTaskName.addTextChangedListener {
                mAddEditTaskVM.taskName = it.toString()
            }

            // task-importance
            checkBoxImportant.isChecked = mAddEditTaskVM.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState()
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                mAddEditTaskVM.taskImportance = isChecked
            }

            // task date
            textViewDateCreated.text = "Created: ${mAddEditTaskVM.task?.createdDateFormatted}"
            textViewDateCreated.isVisible = mAddEditTaskVM.task != null

            // save button
            fabSaveTask.setOnClickListener {
                mAddEditTaskVM.onSaveClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mAddEditTaskVM.mAddTaskEventFlow.collectLatest {
                when (it) {
                    AddEditViewModel.AddEditTaskEvents.TaskNameEmpty -> {
                        Snackbar.make(
                            requireView(),
                            "Task name can't be empty",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is AddEditViewModel.AddEditTaskEvents.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to it.resultCode)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }
    }
}