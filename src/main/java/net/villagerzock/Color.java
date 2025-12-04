package net.villagerzock;

import org.joml.Vector4f;

public class Color extends Vector4f {
    public static final Color white     = new Color(255, 255, 255);

    /**
     * The color white.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color WHITE = white;

    /**
     * The color light gray.  In the default sRGB space.
     */
    public static final Color lightGray = new Color(192, 192, 192);

    /**
     * The color light gray.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color LIGHT_GRAY = lightGray;

    /**
     * The color gray.  In the default sRGB space.
     */
    public static final Color gray      = new Color(128, 128, 128);

    /**
     * The color gray.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color GRAY = gray;

    /**
     * The color dark gray.  In the default sRGB space.
     */
    public static final Color darkGray  = new Color(64, 64, 64);

    /**
     * The color dark gray.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color DARK_GRAY = darkGray;

    /**
     * The color black.  In the default sRGB space.
     */
    public static final Color black     = new Color(0, 0, 0);

    /**
     * The color black.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color BLACK = black;

    /**
     * The color red.  In the default sRGB space.
     */
    public static final Color red       = new Color(255, 0, 0);

    /**
     * The color red.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color RED = red;

    /**
     * The color pink.  In the default sRGB space.
     */
    public static final Color pink      = new Color(255, 175, 175);

    /**
     * The color pink.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color PINK = pink;

    /**
     * The color orange.  In the default sRGB space.
     */
    public static final Color orange    = new Color(255, 200, 0);

    /**
     * The color orange.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color ORANGE = orange;

    /**
     * The color yellow.  In the default sRGB space.
     */
    public static final Color yellow    = new Color(255, 255, 0);

    /**
     * The color yellow.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color YELLOW = yellow;

    /**
     * The color green.  In the default sRGB space.
     */
    public static final Color green     = new Color(0, 255, 0);

    /**
     * The color green.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color GREEN = green;

    /**
     * The color magenta.  In the default sRGB space.
     */
    public static final Color magenta   = new Color(255, 0, 255);

    /**
     * The color magenta.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color MAGENTA = magenta;

    /**
     * The color cyan.  In the default sRGB space.
     */
    public static final Color cyan      = new Color(0, 255, 255);

    /**
     * The color cyan.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color CYAN = cyan;

    /**
     * The color blue.  In the default sRGB space.
     */
    public static final Color blue      = new Color(0, 0, 255);

    /**
     * The color blue.  In the default sRGB space.
     * @since 1.4
     */
    public static final Color BLUE = blue;
    
    
    public Color(int hexColor) {
        this((hexColor >> 24) & 0xFF,(hexColor >> 16) & 0xFF,(hexColor >> 8) & 0xFF,hexColor & 0xFF);
    }
    public Color(Vector4f color) {
        super(color);
    }
    public Color(float r, float g, float b, float a) {
        super(r,g,b,a);
    }
    public Color(int r, int g, int b, int a) {
        super(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
    }
    public Color(int r, int g, int b){
        this(r,g,b,255);
    }
}
