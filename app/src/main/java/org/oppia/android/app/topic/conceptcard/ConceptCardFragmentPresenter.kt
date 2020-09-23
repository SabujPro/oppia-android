package org.oppia.android.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.R
import org.oppia.android.app.databinding.ConceptCardFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ConceptCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>
) {
  private lateinit var skillId: String

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, id: String): View? {
    val binding = ConceptCardFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val view = binding.conceptCardExplanationText
    val viewModel = getConceptCardViewModel()

    skillId = id
    viewModel.setSkillIdAndBinding(skillId, view)
    logConceptCardEvent(skillId)

    binding.conceptCardToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.conceptCardToolbar.setNavigationContentDescription(
      R.string.concept_card_close_icon_description
    )
    binding.conceptCardToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? ConceptCardListener)?.dismissConceptCard()
    }

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun getConceptCardViewModel(): ConceptCardViewModel {
    return viewModelProvider.getForFragment(fragment, ConceptCardViewModel::class.java)
  }

  private fun logConceptCardEvent(skillId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_CONCEPT_CARD,
      oppiaLogger.createConceptCardContext(skillId)
    )
  }
}
