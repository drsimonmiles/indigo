package indigo.gameengine

import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{GlobalEvent, InputState}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.BoundaryLocator

trait FrameProcessor[StartUpData, Model, ViewModel] {
  def run(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)]

  def runSkipView(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel)]
}
