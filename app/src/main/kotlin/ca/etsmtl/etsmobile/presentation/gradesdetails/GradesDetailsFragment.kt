package ca.etsmtl.etsmobile.presentation.gradesdetails

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import ca.etsmtl.etsmobile.R
import ca.etsmtl.etsmobile.util.EventObserver
import ca.etsmtl.repository.data.model.Cours
import com.moos.library.CircleProgressView
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_grades_details.progressViewAverage
import kotlinx.android.synthetic.main.fragment_grades_details.progressViewGrade
import kotlinx.android.synthetic.main.fragment_grades_details.recyclerViewEvaluation
import kotlinx.android.synthetic.main.fragment_grades_details.swipeRefreshLayoutGradesDetails
import kotlinx.android.synthetic.main.fragment_grades_details.tvAverage
import kotlinx.android.synthetic.main.fragment_grades_details.tvGrade
import kotlinx.android.synthetic.main.fragment_grades_details.tvRating
import javax.inject.Inject

/**
 * Created by Sonphil on 15-08-18.
 */

class GradesDetailsFragment : DaggerFragment() {

    private val gradesDetailsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(GradesDetailsViewModel::class.java)
    }
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val groupAdapter by lazy { GroupAdapter<ViewHolder>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_grades_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSwipeRefresh()

        setUpRecyclerView()

        subscribeUI()

        arguments?.getParcelable<Cours>(COURS_KEY)?.let { course ->
            gradesDetailsViewModel.cours.value = course
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(COURS_KEY, gradesDetailsViewModel.cours.value)

        super.onSaveInstanceState(outState)
    }

    private fun setUpSwipeRefresh() {
        swipeRefreshLayoutGradesDetails.setColorSchemeResources(R.color.colorPrimary)
        swipeRefreshLayoutGradesDetails.setOnRefreshListener { gradesDetailsViewModel.refresh() }
    }

    private fun setUpRecyclerView() {
        recyclerViewEvaluation.adapter = groupAdapter
        recyclerViewEvaluation.layoutManager = LinearLayoutManager(context)
    }

    private fun subscribeUI() {
        gradesDetailsViewModel.getLoading().observe(this, Observer {
            swipeRefreshLayoutGradesDetails.isRefreshing = it == true
        })

        gradesDetailsViewModel.errorMessage.observe(this, EventObserver {
            it?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        })

        gradesDetailsViewModel.getGradePercentage().observe(this, Observer {
            tvRating.text = gradesDetailsViewModel.cours.value?.cote

            it?.takeIf { it.isNotBlank() }?.let {
                setCircleProgressViewProgress(progressViewGrade, it.replace(",", ".").toFloat())
                tvGrade.text = String.format(getString(R.string.text_grade_in_percentage), it)
            }
        })

        gradesDetailsViewModel.getAveragePercentage().observe(this, Observer {
            it?.takeIf { it.isNotBlank() }?.let {
                setCircleProgressViewProgress(progressViewAverage, it.replace(",", ".").toFloat())
                tvAverage.text = String.format(getString(R.string.text_grade_in_percentage), it)
            }
        })

        gradesDetailsViewModel.getEvaluations().observe(this, Observer {
            it?.forEach {
                ExpandableGroup(EvaluationHeaderItem(it)).apply {
                    val grade = String.format(
                            getString(R.string.text_grade_with_percentage),
                            it.note,
                            it.corrigeSur,
                            it.notePourcentage
                    )

                    val averageStr = String.format(
                            getString(R.string.text_grade_with_percentage),
                            it.moyenne,
                            it.corrigeSur,
                            it.moyennePourcentage
                    )

                    add(Section(
                            listOf(
                                    EvaluationDetailItem(
                                            getString(R.string.label_grade),
                                            grade
                                    ),
                                    EvaluationDetailItem(
                                            getString(R.string.label_average),
                                            averageStr
                                    ),
                                    EvaluationDetailItem(
                                            getString(R.string.label_median),
                                            it.mediane ?: ""
                                    ),
                                    EvaluationDetailItem(
                                            getString(R.string.label_standard_deviation),
                                            it.ecartType ?: ""
                                    ),
                                    EvaluationDetailItem(
                                            getString(R.string.label_percentile_rank),
                                            it.rangCentile ?: ""
                                    ),
                                    EvaluationDetailItem(
                                            getString(R.string.label_target_date),
                                            it.dateCible ?: ""
                                    )
                            )
                    ))

                    groupAdapter.add(this)
                }
            }
        })

        lifecycle.addObserver(gradesDetailsViewModel)
    }

    private fun setCircleProgressViewProgress(circleProgressView: CircleProgressView, progress: Float?) {
        progress?.let {
            with(circleProgressView) {
                setEndProgress(it)
                startProgressAnimation()
            }
        }
    }

    companion object {
        const val TAG = "GradesDetailsFragment"
        const val COURS_KEY = "CoursKey"

        fun newInstance(cours: Cours) = GradesDetailsFragment().apply {
            arguments = Bundle().apply { putParcelable(COURS_KEY, cours) }
        }
    }
}