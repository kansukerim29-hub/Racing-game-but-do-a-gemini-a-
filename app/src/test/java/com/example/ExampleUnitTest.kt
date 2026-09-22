package com.example

import com.example.data.CarModel
import com.example.game.GameLoopState
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for game logic, models, and GameLoop state.
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun defaultCar_isInitialized() {
    val car = CarModel.DEFAULT_CAR
    assertNotNull(car)
    assertEquals("apex_gt", car.id)
    assertTrue(car.baseSpeed > 0)
  }

  @Test
  fun gameLoopState_transitionsCorrectly() {
    val state = GameLoopState()
    assertFalse(state.isRunning)
    assertFalse(state.isPaused)

    state.start()
    assertTrue(state.isRunning)
    assertFalse(state.isPaused)

    state.pause()
    assertTrue(state.isRunning)
    assertTrue(state.isPaused)

    state.resume()
    assertFalse(state.isPaused)

    state.stop()
    assertFalse(state.isRunning)
  }
}
