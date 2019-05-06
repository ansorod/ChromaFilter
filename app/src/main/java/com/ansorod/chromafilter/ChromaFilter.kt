package com.ansorod.chromafilter

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.Type
import android.view.Surface
import android.view.TextureView

class ChromaFilter(context: Context) {

    private val render: RenderScript = RenderScript.create(context)

    public var textureView: TextureView? = null
    public var baseColor: Int
    public var tolerance: Int
    public var saturation: Double
    public var brightness: Int

    init {
        baseColor = DEFAULT_BASE_COLOR
        tolerance = DEFAULT_TOLERANCE
        saturation = DEFAULT_SATURATION
        brightness = DEFAULT_BRIGHTNESS
    }

    /**
     * Applies the chroma filter to the given bitmap
     *
     * @param bitmap source image
     * @return {Bitmap} a nullable Bitmap. Chosen color is replaced by transparent pixels
     */
    fun applyToBitmap(bitmap: Bitmap): Bitmap? {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val script = ScriptC_color_extractor(render)

        val allocationInput = Allocation.createFromBitmap(render, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT)
        val allocationOutput = Allocation.createTyped(render, allocationInput.type)

        script._baseColor = baseColor
        script._tolerance = tolerance
        script._saturation = saturation
        script._brightness = brightness

        script.forEach_removeColor(allocationInput, allocationOutput)
        allocationOutput.copyTo(result)

        allocationInput.destroy()
        allocationOutput.destroy()

        return result
    }

    /**
     * Applies the chroma filter based on given yuvData and
     * the result is post to textureView if provided
     *
     * @param yuvData image as ByteArray
     */
    private fun applyToSurface(yuvData: ByteArray, width: Int, height: Int) {
        textureView?.let {
            val converter = ScriptC_chromakey(render)
            val rgbType = Type.Builder(render, Element.RGBA_8888(render)).setX(width).setY(height)

            val inputAllocation = Allocation.createTyped(render, rgbType.create())
            val outputAllocation = Allocation.createTyped(render, rgbType.create(), Allocation.USAGE_IO_OUTPUT or Allocation.USAGE_SCRIPT)

            outputAllocation.syncAll(Allocation.USAGE_SCRIPT)
            inputAllocation.copyFrom(yuvData)

            outputAllocation.surface =  Surface(it.surfaceTexture)

            converter._baseColor = baseColor
            converter._tolerance = tolerance
            converter._saturation = saturation
            converter._brightness = brightness

            converter.forEach_chromaKey(inputAllocation, outputAllocation)
            outputAllocation.ioSend()

            inputAllocation.destroy()
            outputAllocation.destroy()
        }
    }

    companion object {
        private val DEFAULT_BASE_COLOR = 100
        private val DEFAULT_TOLERANCE = 40
        private val DEFAULT_SATURATION = 0.07
        private val DEFAULT_BRIGHTNESS = 80
    }
}