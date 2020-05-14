package nl.testchamber.mailordercoffeeshop

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.core.AllOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@LooperMode(LooperMode.Mode.PAUSED)
class RobolectricTest {

    private lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        SharedPreferencesUtil.setIsFirstLaunchToFalse(app.applicationContext)

        activityScenario = ActivityScenario.launch(MainActivity::class.java)

        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
        activityScenario.close()
    }

    // For the 'Plus button' I used a hardcoded string. But if a developer has added the text
    // to the Android resources, then it's also available using R.string.*
    // but it's still possible to use "Review order" instead of R.string.review_order_button
    @Test
    fun orderOverViewShouldDisplayIngredients() {
        Espresso.onView(ViewMatchers.withText("+")).perform(ViewActions.click(), ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.chocolate)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.review_order_button)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.beverage_detail_ingredients)).check(ViewAssertions.matches(ViewMatchers.withText("Ingredients:\n2 shots of espresso\nChocolate")))
    }

    @Test
    fun shouldBeAbleToSelectAnItemInTheRecyclerView() {
        Espresso.onView(ViewMatchers.withId(R.id.use_menu)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.beverage_recycler_view))
                .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(ViewMatchers.withText("CON PANNA")), ViewActions.click()))
        Espresso.onView(ViewMatchers.withId(R.id.beverage_detail_title)).check(ViewAssertions.matches(ViewMatchers.withText("Con Panna")))
    }

    @Test
    fun shouldSendAnIntentContainingTheRightOrderName() {
        onView(withId(R.id.espresso_shot_counter)).check(matches(withText("0")))
        Espresso.onView(ViewMatchers.withText("+")).perform(ViewActions.click(), ViewActions.click())
        onView(withId(R.id.espresso_shot_counter)).check(matches(withText("2")))
        Espresso.onView(ViewMatchers.withId(R.id.chocolate)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.review_order_button)).perform(scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.name_text_box)).perform(ViewActions.scrollTo(), ViewActions.typeText("My name"))
        Espresso.onView(ViewMatchers.withId(R.id.custom_order_name_box)).perform(ViewActions.scrollTo(), ViewActions.typeText("Custom Order Name"))
        Espresso.onView(ViewMatchers.withId(R.id.mail_order_button)).perform(ViewActions.scrollTo(), ViewActions.click())
        Intents.intended(AllOf.allOf(
                IntentMatchers.hasAction(CoreMatchers.equalTo(Intent.ACTION_SENDTO)),
                IntentMatchers.hasExtra(Intent.EXTRA_SUBJECT, "Order: My name - Custom Order Name")))
    }
}