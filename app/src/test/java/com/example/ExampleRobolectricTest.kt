package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.IntakeStatus
import com.example.data.model.MealTiming
import com.example.data.model.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ঔষধ রিমাইন্ডার", appName)
  }

  @Test
  fun `verify three time slots existence and titles`() {
    assertEquals(3, TimeSlot.entries.size)
    assertEquals("সকাল", TimeSlot.MORNING.titleBn)
    assertEquals("দুপুর", TimeSlot.NOON.titleBn)
    assertEquals("সন্ধ্যা", TimeSlot.EVENING.titleBn)
  }

  @Test
  fun `verify intake statuses`() {
    assertEquals("খেয়েছি", IntakeStatus.TAKEN.labelBn)
    assertEquals("খাইনি", IntakeStatus.MISSED.labelBn)
    assertEquals("বাকি আছে", IntakeStatus.PENDING.labelBn)
  }

  @Test
  fun `verify meal timing labels`() {
    assertEquals("খাওয়ার আগে", MealTiming.BEFORE_MEAL.labelBn)
    assertEquals("খাওয়ার পরে", MealTiming.AFTER_MEAL.labelBn)
  }
}
