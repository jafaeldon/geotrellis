package geotrellis.raster

import geotrellis._

import spire.syntax.cfor._

/**
 * LazyConvert represents a lazily-applied conversion to any type.
 *
 * @note     If you care converting to a RasterType with less bits
 *           than the type of the underlying data, you are responsible
 *           for managing overflow. This convert does not do any casting;
 *           therefore converting from a TypeInt to TypeByte could still
 *           return values greater than 127 from apply().
 */
final case class LazyConvert(data: RasterData, typ: RasterType)
  extends RasterData {

  final def getType = typ
  final def alloc(cols: Int, rows: Int) = RasterData.allocByType(typ,cols,rows)
  final def length = data.length

  def cols = data.cols
  def rows = data.rows

  def apply(i: Int) = data.apply(i)
  def applyDouble(i: Int) = data.applyDouble(i)
  def copy = force
  override def toArray = data.toArray
  override def toArrayDouble = data.toArrayDouble

  def mutable():MutableRasterData = {
    val rasterType = getType
    val forcedData = RasterData.allocByType(rasterType,cols,rows)
    if(rasterType.isDouble) {
      cfor(0)(_ < cols, _ + 1) { col =>
        cfor(0)(_ < rows, _ + 1) { row =>
          forcedData.setDouble(col,row,data.getDouble(col,row))
        }
      }
    } else {
      cfor(0)(_ < cols, _ + 1) { col =>
        cfor(0)(_ < rows, _ + 1) { row =>
          forcedData.set(col,row,data.get(col,row))
        }
      }
    }
    forcedData
  }
  def force():RasterData = mutable
  
  def toArrayByte: Array[Byte] = force.toArrayByte

  def warp(current:RasterExtent,target:RasterExtent):RasterData =
    LazyConvert(data.warp(current,target),typ)
}
