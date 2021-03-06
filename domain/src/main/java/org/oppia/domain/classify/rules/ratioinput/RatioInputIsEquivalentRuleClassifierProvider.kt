package org.oppia.domain.classify.rules.ratioinput

import org.oppia.app.model.InteractionObject
import org.oppia.app.model.RatioExpression
import org.oppia.domain.classify.RuleClassifier
import org.oppia.domain.classify.rules.GenericRuleClassifier
import org.oppia.domain.classify.rules.RuleClassifierProvider
import org.oppia.domain.util.toSimplestForm
import javax.inject.Inject

/**
 * Provider for a classifier that determines whether two object are equal by converting them into
 * their lowest form as per the ratio input interaction.
 */
internal class RatioInputIsEquivalentRuleClassifierProvider @Inject constructor(
  private val classifierFactory: GenericRuleClassifier.Factory
) : RuleClassifierProvider, GenericRuleClassifier.SingleInputMatcher<RatioExpression> {

  override fun createRuleClassifier(): RuleClassifier {
    return classifierFactory.createSingleInputClassifier(
      InteractionObject.ObjectTypeCase.RATIO_EXPRESSION,
      "x",
      this
    )
  }

  override fun matches(answer: RatioExpression, input: RatioExpression): Boolean {
    return answer.toSimplestForm() == input.toSimplestForm()
  }
}
