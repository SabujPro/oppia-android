package org.oppia.app.player.state.itemviewmodel

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean
) : StateItemViewModel(ViewType.CONTENT)
