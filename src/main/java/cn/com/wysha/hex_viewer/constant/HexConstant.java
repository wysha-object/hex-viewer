package cn.com.wysha.hex_viewer.constant;

import javafx.scene.paint.Color;

public class HexConstant {
    public static final int TABLE_CACHE_SIZE = 16384;

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public static final int FIRST_ROW_HEIGHT = 48;
    public static final int ROW_HEIGHT = 32;
    public static final int HEX_FIRST_COLUMN_WIDTH = 140;
    public static final int HEX_COL_WIDTH = 24;
    public static final int ASCII_COL_WIDTH = 10;

    public static final int SCROLL_SLEEP = 10;

    public static final Color FIRST_COL_BACKGROUND = Color.rgb(0xD7, 0xD7, 0xD7);
    public static final Color FIRST_ROW_BACKGROUND = Color.rgb(0xDF, 0xDF, 0xDF);
    public static final Color ROW_BACKGROUND = Color.rgb(0xFF, 0xFF, 0xFF);

    public static final String FILE_NOT_SELECTED = "请选择文件以查看";
    public static final String FILE_NOT_READABLE = "文件不可读";
}
