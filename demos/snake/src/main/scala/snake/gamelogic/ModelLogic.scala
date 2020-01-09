package snake.gamelogic

import indigo._
import indigoexts.grids._
import indigoexts.scenemanager._
import snake.init.GameAssets
import snake.model._
import snake.model.arena.MapElement.Apple
import snake.model.arena.{Arena, GameMap, MapElement}
import snake.model.snakemodel.{CollisionCheckOutcome, Snake}
import snake.scenes.GameOverScene

object ModelLogic {

  val ScoreIncrement: Int = 100

  def initialModel(gridSize: GridSize, controlScheme: ControlScheme): GameModel =
    GameModel(
      gridSize = gridSize,
      snake = Snake(
        gridSize.centre.x,
        gridSize.centre.y - (gridSize.centre.y / 2)
      ).grow.grow,
      gameState = GameState.Running.start,
      gameMap = Arena.genLevel(gridSize),
      score = 0,
      tickDelay = Millis(100),
      controlScheme = controlScheme,
      lastUpdated = Millis.zero
    )

  def update(gameTime: GameTime, state: GameModel): GlobalEvent => Outcome[GameModel] = {
    case FrameTick if gameTime.running < state.lastUpdated + state.tickDelay =>
      Outcome(state)

    case FrameTick =>
      state.gameState match {
        case s @ GameState.Running(_, _) =>
          updateRunning(
            gameTime,
            state.resetLastUpdated(gameTime.running),
            s
          )(FrameTick)

        case s @ GameState.Crashed(_, _, _, _) =>
          updateCrashed(
            gameTime,
            state.resetLastUpdated(gameTime.running),
            s
          )(FrameTick)
      }

    case gameEvent =>
      state.gameState match {
        case s @ GameState.Running(_, _) =>
          updateRunning(gameTime, state, s)(gameEvent)

        case s @ GameState.Crashed(_, _, _, _) =>
          updateCrashed(gameTime, state, s)(gameEvent)
      }
  }

  def updateRunning(
      gameTime: GameTime,
      state: GameModel,
      runningDetails: GameState.Running
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick =>
      updateBasedOnCollision(gameTime)(
        normalUpdate(gameTime, state)
      )

    case e: KeyboardEvent =>
      Outcome(
        state.copy(
          snake = state.controlScheme.instructSnake(e, state.snake, runningDetails.lastSnakeDirection)
        )
      )

    case _ =>
      Outcome(state)
  }

  def normalUpdate(
      gameTime: GameTime,
      state: GameModel
  ): (GameModel, CollisionCheckOutcome) =
    state.update(
      gameTime,
      state.gameMap.gridSize,
      hitTest(
        state.gameMap,
        state.snake.givePath
      )
    )

  def hitTest(
      gameMap: GameMap,
      body: List[GridPoint]
  ): GridPoint => CollisionCheckOutcome = pt => {
    if (body.contains(pt)) CollisionCheckOutcome.Crashed(pt)
    else
      gameMap.fetchElementAt(pt.x, pt.y) match {
        case Some(MapElement.Apple(_)) =>
          CollisionCheckOutcome.PickUp(pt)

        case Some(MapElement.Wall(_)) =>
          CollisionCheckOutcome.Crashed(pt)

        case None =>
          CollisionCheckOutcome.NoCollision(pt)
      }
  }

  def updateBasedOnCollision(gameTime: GameTime): ((GameModel, CollisionCheckOutcome)) => Outcome[GameModel] = {
    case (gameModel, CollisionCheckOutcome.Crashed(_)) =>
      Outcome(
        gameModel.copy(
          gameState = gameModel.gameState match {
            case c @ GameState.Crashed(_, _, _, _) =>
              c

            case r @ GameState.Running(_, _) =>
              r.crash(gameTime.running, gameModel.snake.length)
          }
        )
      ).addGlobalEvents(PlaySound(GameAssets.soundLose, Volume.Max))

    case (gameModel, CollisionCheckOutcome.PickUp(pt)) =>
      Outcome(
        gameModel.copy(
          snake = gameModel.snake.grow,
          gameMap = gameModel.gameMap
            .removeElement(pt)
            .insertElement(
              Apple(
                gameModel.gameMap
                  .findEmptySpace(pt :: gameModel.snake.givePath)
              )
            ),
          score = gameModel.score + ScoreIncrement
        )
      ).addGlobalEvents(
        PlaySound(GameAssets.soundPoint, Volume.Max),
        Score.spawnEvent(ViewLogic.gridPointToPoint(pt, gameModel.gameMap.gridSize))
      )

    case (gameModel, CollisionCheckOutcome.NoCollision(_)) =>
      Outcome(gameModel)
  }

  def updateCrashed(
      gameTime: GameTime,
      state: GameModel,
      crashDetails: GameState.Crashed
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick if gameTime.running <= crashDetails.crashedAt + Millis(750) =>
      //Pause briefly on collision
      Outcome(state)

    case FrameTick if state.snake.length > 1 =>
      Outcome(
        state.copy(
          snake = state.snake.shrink,
          gameState = state.gameState.updateNow(gameTime.running, state.gameState.lastSnakeDirection)
        )
      )

    case FrameTick if state.snake.length == 1 =>
      Outcome(state)
        .addGlobalEvents(SceneEvent.JumpTo(GameOverScene.name))

    case _ =>
      Outcome(state)
  }

}