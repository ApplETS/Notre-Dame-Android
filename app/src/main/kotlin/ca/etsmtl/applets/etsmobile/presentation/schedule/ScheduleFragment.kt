package ca.etsmtl.applets.etsmobile.presentation.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.etsmtl.applets.etsmobile.R
import ca.etsmtl.applets.etsmobile.util.EventObserver
import ca.etsmtl.applets.etsmobile.util.show
import ca.etsmtl.applets.repository.data.model.Seance
import com.alamkanak.weekview.WeekViewEvent
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.empty_view_schedule.*
import kotlinx.android.synthetic.main.fragment_schedule.*
import java.util.Calendar
import javax.inject.Inject

/**
 * Created by Sonphil on 25-02-18.
 */
// TODO Fucc la librairie, switch à un recycler view
class ScheduleFragment : DaggerFragment() {
    private val scheduleViewModel: ScheduleViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ScheduleViewModel::class.java)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSwipeRefresh()
//        setUpRecyclerView()
        setUpWeekView()
        subscribeUI()
    }

    private fun setUpSwipeRefresh() {
//        swipeRefreshLayoutSchedule.setColorSchemeResources(R.color.colorPrimary)
//        swipeRefreshLayoutSchedule.setOnRefreshListener { scheduleViewModel.refresh() }
    }

    private fun setUpWeekView() {
        scheduleWeekView.setMonthChangeListener { newYear, newMonth ->
            val startTime = Calendar.getInstance()
            startTime.set(newYear, newMonth - 1, 1, 0, 0)

            val endTime = startTime.clone() as Calendar
            endTime.add(Calendar.MONTH, 1)
            val events = ArrayList<ScheduleEventAdapter>()
            scheduleViewModel.seances.value.orEmpty()
                /*.filter { s-> s.dateDebut.time in startTime.timeInMillis..endTime.timeInMillis}*/
                .forEach { s -> events.add(ScheduleEventAdapter(s)) }
            return@setMonthChangeListener events
        }
//        scheduleWeekView.monthChangeListener = MonthLoader.MonthChangeListener { newYear, newMonth ->
//            val startTime = Calendar.getInstance()
//            startTime.set(newYear, newMonth-1, 1, 0, 0)
//
//            val endTime = startTime.clone() as Calendar
//            endTime.add(Calendar.MONTH, 1)
//            return@MonthChangeListener scheduleViewModel.seances.value
//                ?.filter { s-> s.dateDebut.time in startTime.timeInMillis..endTime.timeInMillis}
//                .orEmpty().map { s->ScheduleEventAdapter(s) }.toMutableList()
//
//        }
        scheduleWeekView.setOnEventClickListener { event, _ -> Toast.makeText(context, "Clicked " + event.name, Toast.LENGTH_SHORT).show(); }
        scheduleWeekView.setEventLongPressListener { event, _ -> Toast.makeText(context, "Long pressed event: " + event.name, Toast.LENGTH_SHORT).show(); }
        scheduleWeekView.setEmptyViewLongPressListener { a -> Toast.makeText(context, "Shit ass tits", Toast.LENGTH_LONG).show() }
//        scheduleWeekView.dateTimeInterpreter = object :DateTimeInterpreter{
//            override fun interpretTime(hour: Int): String {
//                return hour.toString()
//            }
//
//            override fun interpretDate(date: Calendar?): String {
// //                return DateFormat.getTimeInstance().format(date)
//                return date.toString()
//            }
//        }
        scheduleWeekView.defaultEventColor = R.color.etsRougeClair
    }

    private fun subscribeUI() {
        scheduleViewModel.showEmptyView.observe(this, Observer {
//            scheduleWeekView.show(!it)
//            emptyViewSchedule.show(it)
        })

        scheduleViewModel.seances.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                scheduleWeekView.notifyDatasetChanged()
            }
        })

//        scheduleViewModel.loading.observe(this, Observer {
//            it?.let { swipeRefreshLayoutSchedule.isRefreshing = it }
//        })

        scheduleViewModel.errorMessage.observe(this, EventObserver {
            it?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        })

        this.lifecycle.addObserver(scheduleViewModel)
    }

    inner class ScheduleEventAdapter(seance: Seance) : WeekViewEvent(
        seance.dateDebut.time,
        seance.nomActivite,
        seance.dateDebut.year,
        seance.dateDebut.month,
        seance.dateDebut.day,
        seance.dateDebut.hours,
        seance.dateDebut.minutes,
        seance.dateFin.year,
        seance.dateFin.month,
        seance.dateFin.day,
        seance.dateFin.hours,
        seance.dateFin.minutes
    )

    companion object {
        private const val TAG = "ScheduleFragment"
        fun newInstance(): ScheduleFragment = ScheduleFragment()
    }
}
