package org.oppia.app.options

import android.app.Activity
import android.app.Application
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import com.google.firebase.FirebaseApp
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OptionsFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = OptionsFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class OptionsFragmentTest {

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @get:Rule
  var optionActivityTestRule: ActivityTestRule<OptionsActivity> = ActivityTestRule(
    OptionsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  private fun createOptionActivityIntent(
    internalProfileId: Int,
    isFromNavigationDrawer: Boolean
  ): Intent {
    return OptionsActivity.createOptionsActivity(
      ApplicationProvider.getApplicationContext(),
      internalProfileId,
      isFromNavigationDrawer
    )
  }

  @Test
  fun testOptionsFragment_parentIsExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(createOptionActivityIntent(0, false)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(matches(isCompletelyDisplayed()))
    }
  }

  @Test
  fun testOptionsFragment_parentIsNotExploration_checkBackArrowNotVisible() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.abc_action_bar_up_description))
        .check(doesNotExist())
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionFragment_clickNavigationDrawerHamburger_navigationDrawerIsOpenedSuccessfully() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.options_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.options_activity_drawer_layout)).check(matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_readingTextSize_testOnActivityResult() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      val resultDataIntent = Intent()
      resultDataIntent.putExtra(KEY_MESSAGE_READING_TEXT_SIZE, "Large")
      val activityResult = ActivityResult(Activity.RESULT_OK, resultDataIntent)

      val activityMonitor = getInstrumentation().addMonitor(
        ReadingTextSizeActivity::class.java.name,
        activityResult,
        true
      )

      it.onActivity { activity ->
        activity.startActivityForResult(
          createReadingTextSizeActivityIntent("Small"),
          REQUEST_CODE_TEXT_SIZE
        )
      }

      getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3)

      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          0,
          R.id.reading_text_size_text_view
        )
      ).check(
        matches(withText("Large"))
      )
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_audioLanguage_testOnActivityResult() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      val resultDataIntent = Intent()
      resultDataIntent.putExtra(KEY_MESSAGE_AUDIO_LANGUAGE, "French")
      val activityResult = ActivityResult(Activity.RESULT_OK, resultDataIntent)

      val activityMonitor = getInstrumentation().addMonitor(
        DefaultAudioActivity::class.java.name,
        activityResult,
        true
      )

      it.onActivity { activity ->
        activity.startActivityForResult(
          createDefaultAudioActivityIntent("Hindi"),
          REQUEST_CODE_AUDIO_LANGUAGE
        )
      }

      getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3)

      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          2,
          R.id.audio_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  @Test
  // TODO(#973): Fix OptionsFragmentTest
  @Ignore
  fun testOptionsFragment_appLanguage_testOnActivityResult() {
    launch<OptionsActivity>(createOptionActivityIntent(0, true)).use {
      val resultDataIntent = Intent()
      resultDataIntent.putExtra(KEY_MESSAGE_APP_LANGUAGE, "French")
      val activityResult = ActivityResult(Activity.RESULT_OK, resultDataIntent)

      val activityMonitor = getInstrumentation().addMonitor(
        AppLanguageActivity::class.java.name,
        activityResult,
        true
      )

      it.onActivity { activity ->
        activity.startActivityForResult(
          createAppLanguageActivityIntent("English"),
          REQUEST_CODE_APP_LANGUAGE
        )
      }

      getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 3)

      onView(
        atPositionOnView(
          R.id.options_recyclerview,
          1, R.id.app_language_text_view
        )
      ).check(
        matches(withText("French"))
      )
    }
  }

  private fun createReadingTextSizeActivityIntent(summaryValue: String): Intent {
    return ReadingTextSizeActivity.createReadingTextSizeActivityIntent(
      ApplicationProvider.getApplicationContext(),
      READING_TEXT_SIZE,
      summaryValue
    )
  }

  private fun createAppLanguageActivityIntent(summaryValue: String): Intent {
    return AppLanguageActivity.createAppLanguageActivityIntent(
      ApplicationProvider.getApplicationContext(),
      APP_LANGUAGE,
      summaryValue
    )
  }

  private fun createDefaultAudioActivityIntent(summaryValue: String): Intent {
    return DefaultAudioActivity.createDefaultAudioActivityIntent(
      ApplicationProvider.getApplicationContext(),
      AUDIO_LANGUAGE,
      summaryValue
    )
  }

  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(optionsFragmentTest: OptionsFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOptionsFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(optionsFragmentTest: OptionsFragmentTest) {
      component.inject(optionsFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
