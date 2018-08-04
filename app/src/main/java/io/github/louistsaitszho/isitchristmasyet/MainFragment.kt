package io.github.louistsaitszho.isitchristmasyet

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main.*
import org.threeten.bp.LocalDate
import org.koin.android.viewmodel.ext.android.viewModel as koinViewModel

/**
 * The main (and only) fragment. This is where all the (UI) magic happens
 */
class MainFragment : Fragment() {
    //Inject ViewModel using KOIN
    val viewModel: MainFragmentViewModel by koinViewModel()

    //TODO I don't remember where I read but I should observe LiveData in onActivityCreate
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*
        This uses the viewLifecycleOwner, cause:
        https://medium.com/@BladeCoder/architecture-components-pitfalls-part-1-9300dd969808
         */
        viewModel.answer.observe(viewLifecycleOwner, Observer { updateAnswer(it) })
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, true)
    }

    //onStart (usually) means the view becomes visible, this is when the UI needs to update
    override fun onStart() {
        super.onStart()
        viewModel.startMonitoring()
    }

    //onStop (usually) means the view is no longer visible, stop all UI related stuff
    override fun onStop() {
        super.onStop()
        viewModel.stopMonitoring()
    }

    /**
     * Update the answer text view with [newAnswer]. Set it to "Yes" or "No" in the corresponding
     * locale
     */
    private fun updateAnswer(newAnswer: Boolean?) {
        newAnswer?.run {
            val stringRes = if (this) R.string.yes else R.string.no
            text_view_answer.setText(stringRes)
        }
    }
}

/**
 * ViewModel class that
 * - House all "Business" logic
 * - Stuff that should not depend on lifecycle of the view
 */
class MainFragmentViewModel(private val holiday: Holiday) : ViewModel() {

    val answer: MutableLiveData<Boolean> = MutableLiveData()
    private var clockThread: Thread? = null

    fun startMonitoring() {
        if (clockThread == null) clockThread = Thread(Runnable { runClock() })

        clockThread!!.start()
    }

    fun stopMonitoring() {
        clockThread?.interrupt()
        clockThread = null
    }

    private fun runClock() {
        while (Thread.currentThread().isInterrupted.not()) {
            try {
                checkIfChristmas()
                sleepTillNextSecond()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun checkIfChristmas() {
        answer.postValue(isChristmas(LocalDate.now()))
    }

    private fun isChristmas(now: LocalDate): Boolean =
            now.monthValue == holiday.month && now.dayOfMonth == holiday.dayInMonth

    /**
     * Try to [Thread.sleep] the right amount of time to get back to xxxxxxxxxxxx000 ms
     */
    private fun sleepTillNextSecond() {
        val sleepTime = 1000 - (System.currentTimeMillis() % 1000)
        Thread.sleep(sleepTime)
    }
}