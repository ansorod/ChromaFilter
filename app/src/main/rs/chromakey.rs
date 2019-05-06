#pragma version(1)
#pragma rs java_package_name(com.ansorod.chromafilter)

int baseColor;
int tolerance;
double saturation;
int brightness;

void chromaKey(const uchar4 *allocationIn, uchar4 *v_out, uint32_t x, uint32_t y) {

    int ALPHA = 255;
    int RED = allocationIn->r;
    int GREEN = allocationIn->g;
    int BLUE = allocationIn->b;

    double minValue, maxValue, deltaValue, saturationValue = 0;
    float hue;
    uint32_t minColorRange, maxColorRange, colorTolerance;

    // Based on the given baseColor and tolerance
    // we set the minColorRange and the maxColorRange.
    // Both are normalized in case of exceeding the colors limits
    colorTolerance = tolerance < 0 ? (tolerance * (-1)) :  tolerance;
    minColorRange = baseColor - colorTolerance;
    maxColorRange = baseColor + colorTolerance;

    // minColorRange can be higher than maxColorRange since HSV is arranged in a radial slice
    minColorRange = minColorRange < 0 ? (360 + minColorRange) : minColorRange;
    maxColorRange = maxColorRange > 360 ? (360 - maxColorRange) : maxColorRange;

    minValue = min(RED, min(GREEN,BLUE));
    maxValue = max(RED, max(GREEN,BLUE));

    deltaValue = maxValue - minValue;

    if(deltaValue < 0.0001 || maxValue == 0) {
        v_out->a = ALPHA;
        v_out->r = RED;
        v_out->g = GREEN;
        v_out->b = BLUE;
    } else {
        if(RED >= maxValue) {
            hue = (GREEN - BLUE) / deltaValue;
        } else if(GREEN >= maxValue) {
            hue = 2.0 + (BLUE - RED) / deltaValue;
        } else {
            hue = 4.0 + (RED - GREEN) / deltaValue;
        }

        hue = hue * 60.0;
        hue = hue < 0.0 ? (hue + 360.0) : hue;

        if(maxValue > 0) {
            saturationValue = deltaValue / maxValue;
        }

        bool inBounds = (minColorRange <= maxColorRange) && (hue >= minColorRange && hue <= maxColorRange);
        bool outOfBounds = (minColorRange >= maxColorRange) && (hue >= minColorRange || hue <= maxColorRange);

        if((inBounds || outOfBounds) && saturationValue >= saturation && maxValue > brightness) {
            v_out->a = 0;
        } else {
            v_out->a = ALPHA;
            v_out->r = RED;
            v_out->g = GREEN;
            v_out->b = BLUE;
        }
    }

}