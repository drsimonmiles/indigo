package indigo.platform.renderer.shared

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.display.DisplayEffects
import indigo.shared.datatypes.Matrix4

import scalajs.js.JSConverters._
import scala.collection.mutable
import indigo.shared.display.DisplayEntity

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      rotation = 0,
      scaleX = 1,
      scaleY = 1,
      atlasName = "",
      frame = SpriteSheetFrame.defaultOffset,
      albedoAmount = 1.0f,
      emissiveOffset = Vector2.zero,
      emissiveAmount = 0.0f,
      normalOffset = Vector2.zero,
      normalAmount = 0.0f,
      specularOffset = Vector2.zero,
      specularAmount = 0.0f,
      isLit = 0.0f,
      refX = 0,
      refY = 0,
      effects = DisplayEffects.default
    )

  val verticesAndTextureCoords: scalajs.js.Array[Float] = {
    val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
    val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
    val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
    val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

    vert0 ++ vert1 ++ vert2 ++ vert3
  }

  val vertexCount: Int = verticesAndTextureCoords.length / 3

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] =
    mat4d.mat.toJSArray

  val sortByDepth: mutable.ListBuffer[DisplayEntity] => mutable.ListBuffer[DisplayEntity] =
    _.sortWith((d1, d2) => d1.z > d2.z)

}
