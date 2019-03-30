#pragma version(1)
#pragma rs java_package_name(com.ansorod.chromafilter)

void removeColor(const uchar4 *v_in, uchar4 *v_out) {

    double minValue;
    double maxValue;
    double deltaValue;
    float hue;
    int minColorRange;
    int maxColorRange;
    int colorTolerance;

    int baseColor = 100;
    int tolerance = 30;

    int r, g, b;
    r = v_in->r;
    g = v_in->g;
    b = v_in->b;

    // Based on the given baseColor and tolerance
    // we set the minColorRange and the maxColorRange.
    // Both are normalized in case of exceeding the colors limits
    colorTolerance = tolerance < 0 ? (tolerance * (-1)) :  tolerance;
    minColorRange = baseColor - colorTolerance;
    maxColorRange = baseColor + colorTolerance;

    minColorRange = minColorRange < 0 ? 0 : minColorRange;
    maxColorRange = maxColorRange > 360 ? 360 : maxColorRange;

    minValue = min(r, min(g,b));
    maxValue = max(r, max(g,b));

    deltaValue = maxValue - minValue;

    if(deltaValue < 0.00001 || maxValue == 0) {
        v_out->a = v_in->a;
        v_out->r = v_in->r;
        v_out->g = v_in->g;
        v_out->b = v_in->b;
    } else {
        if(r >= maxValue) {
            hue = (g - b) / deltaValue;
        } else if(g >= maxValue) {
            hue = 2.0 + (b - r) / deltaValue;
        } else {
            hue = 4.0 + (r - g) / deltaValue;
        }

        hue = hue * 60.0;
        hue = hue < 0.0 ? (hue + 360.0) : hue;


        if(hue >= minColorRange && hue <= maxColorRange) {
            v_out->a = 0;
        } else {
            v_out->a = v_in->a;
            v_out->r = v_in->r;
            v_out->g = v_in->g;
            v_out->b = v_in->b;
        }

    }
}