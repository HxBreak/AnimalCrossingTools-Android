package com.hxbreak.animalcrossingtools.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.hxbreak.animalcrossingtools.R
import com.hxbreak.animalcrossingtools.RunnerType
import com.hxbreak.animalcrossingtools.combinedLiveData
import com.hxbreak.animalcrossingtools.data.prefs.PreferenceStorage
import com.hxbreak.animalcrossingtools.extensions.ControlledRunner
import com.hxbreak.animalcrossingtools.i18n.ResourceLanguageSettingDialogFragment
import com.hxbreak.animalcrossingtools.theme.ThemeSettingDialogFragment
import com.hxbreak.animalcrossingtools.ui.settings.prefs.ui.HemisphereChooseDialogFragment
import com.hxbreak.animalcrossingtools.ui.settings.prefs.ui.TimeZeroChooseDialogFragment
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalField
import java.time.temporal.TemporalUnit
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val navigator by lazy {
        findNavController()
    }

    @Inject
    lateinit var preference: PreferenceStorage
    @Inject
    lateinit var formatter: DateTimeFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward

        reenterTransition = backward

        exitTransition = forward
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        settings_choose_theme.setOnClickListener {
            ThemeSettingDialogFragment.newInstance()
                .show(childFragmentManager, null)
        }

        settings_choose_resource_language.setOnClickListener {
            ResourceLanguageSettingDialogFragment.newInstance()
                .show(childFragmentManager, null)
        }

        settings_choose_island_hemisphere.setOnClickListener {
            HemisphereChooseDialogFragment.newInstance().show(childFragmentManager, null)
        }

        settings_choose_timezone.setOnClickListener {
            TimeZeroChooseDialogFragment.newInstance().show(childFragmentManager, null)
        }

        settings_about_software.setOnClickListener {
            navigator.navigate(SettingsFragmentDirections.actionGlobalFlutterFragment("/about", cachedEngineId = "only"))
        }
        preference.observableLocale.observe(viewLifecycleOwner){
            locale_value.text = it.getDisplayName(it)
        }
        preference.observableHemisphere.observe(viewLifecycleOwner){
            hemisphere.text = it.toString()
        }
        val stream : LiveData<Unit> = combinedLiveData(viewLifecycleOwner.lifecycleScope.coroutineContext,
            x = preference.dateTimeFormatter,
            y = preference.observableTimeZone,
            runnerType = RunnerType.CANCEL_PRE_AND_RUN){ x, y ->
            if ( x != null && y != null){
                while (true){
                    if (!isActive) return@combinedLiveData
                    val timeClock = Clock.system(y)
                    clock.text = LocalDateTime.now(timeClock).format(x)
                    val instant = timeClock.instant()
                    /**
                     * always update time after a second left
                     */
                    delay((1000L - (instant.toEpochMilli() - (instant.epochSecond * 1000L))))
                }
            }
        }
        Snackbar.make(requireView(),
            resources.getIdentifier("internal_version", "string", requireContext().packageName),
            Snackbar.LENGTH_SHORT
        ).show()
        /**
         * observe two #LiveData stream
         */
        stream.observe(viewLifecycleOwner){}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).setSupportActionBar(null)
    }
}