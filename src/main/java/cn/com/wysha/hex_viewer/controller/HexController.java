package cn.com.wysha.hex_viewer.controller;

import cn.com.wysha.hex_viewer.cache.LRUMapCache;
import cn.com.wysha.hex_viewer.constant.HexConstant;
import cn.com.wysha.hex_viewer.service.FileService;
import cn.com.wysha.hex_viewer.utils.BeanUtils;
import cn.com.wysha.hex_viewer.utils.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;

@Slf4j
public class HexController implements Initializable {
    @FXML
    public HBox hBox;
    @FXML
    private ToggleButton toggleButton;
    @FXML
    private TextField jmpAddrInput;
    @FXML
    private TextField fromInput;
    @FXML
    private TextField toInput;
    @FXML
    private ScrollBar bar;
    @FXML
    private Canvas hexCanvas;
    @FXML
    private Canvas asciiCanvas;
    @FXML
    private VBox pane;

    @FXML
    private void onScroll(ScrollEvent event) {
        double y = - (event.getDeltaY() / HexConstant.ROW_HEIGHT) * 1.3;
        setBarValue(bar.getValue() + y);
    }

    @FXML
    private void toggleViewMode() {
        if (toggleButton.isSelected()) {
            viewMode = ViewMode.COL64;
        } else {
            viewMode = ViewMode.COL32;
        }
        reinitialize();
    }

    @FXML
    private void jmp() {
        long index = Long.parseLong(jmpAddrInput.getText(), 16);
        long indexOfRow = index / viewMode.numOfCol;
        setBarValue(indexOfRow);
    }

    @FXML
    private void writeFile() {
        FileChooser chooser = new FileChooser();
        File original = chooser.showOpenDialog(stage);

        if (original == null) {
            return;
        }

        long from = Long.parseLong(fromInput.getText(), 16);
        long to = Long.parseLong(toInput.getText(), 16);

        try {
            int length = (int) (to - from);
            byte[] tmp = Files.readAllBytes(original.toPath());
            byte[] bytes = Arrays.copyOf(tmp, length);

            Stage noticeStage = new Stage();
            noticeStage.initOwner(stage);
            noticeStage.initModality(Modality.WINDOW_MODAL);
            noticeStage.setAlwaysOnTop(true);
            NoticeController notice = NoticeController.show(noticeStage, String.format("确认将%s写入%s的0x%X至0x%X吗?",
                    original.getAbsolutePath(),
                    fileService.getFile().getAbsolutePath(),
                    from,
                    to
            ));
            notice.setOnEnsure(isEnsure -> {
                if (isEnsure) {
                    fileService.saveBytes(from, bytes);
                    reinitialize();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Stage stage;

    private volatile ViewMode viewMode = ViewMode.COL32;

    private final FileService fileService;

    public HexController(Stage stage, File file) {
        Objects.requireNonNull(stage);
        Objects.requireNonNull(file);
        this.stage = stage;
        this.fileService = BeanUtils.getBean(FileService.class);
        fileService.setFile(file);
    }

    public static void show(Stage stage, File file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(_ -> new HexController(stage, file));
        fxmlLoader.setLocation(IndexController.class.getResource("hex.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        FxUtils.show(scene, stage, HexConstant.DEFAULT_WIDTH, HexConstant.DEFAULT_HEIGHT, file.getAbsolutePath());
    }

    private void setBarValue(double v) {
        if (v < bar.getMin()) {
            v = bar.getMin();
        } else if (v > bar.getMax()) {
            v = bar.getMax();
        }
        bar.setValue(v);
    }

    private void checkBarValue(double v) {
        if (v < bar.getMin()) {
            v = bar.getMin();
        } else if (v > bar.getMax()) {
            v = bar.getMax();
        } else {
            return;
        }
        bar.setValue(v);
    }

    private long getTotalNumOfRow() {
        return (fileService.getFileLength() + viewMode.numOfCol - 1) / viewMode.numOfCol;
    }

    private void drawText(GraphicsContext graphicsContext, String data, int x, int y, int w, int h, int topBoard, int rightBoard, int bottomBoard, int leftBoard, Color background, Color board) {
        int endX = x + w -1;
        int endY = y + h -1;

        graphicsContext.setFill(background);
        graphicsContext.fillRect(x, y, w, h);

        int centerX = x + w / 2;
        int centerY = y + h / 2;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFont(Font.font("Cascadia Mono"));
        graphicsContext.fillText(data, centerX, centerY);

        graphicsContext.setFill(board);
        graphicsContext.fillRect(x, y, w, topBoard);
        graphicsContext.fillRect(endX, y, rightBoard, h);
        graphicsContext.fillRect(x, endY, w, bottomBoard);
        graphicsContext.fillRect(x, y, leftBoard, h);
    }

    /**
     *
     * @param canvas 目标画布
     * @param table  String[indexOfRow][indexOfCol]
     */
    private void draw(Canvas canvas, String[][] table, int firstColWidth, int colWidth, int firstRowHeight, int rowHeight, int yOffset) {
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //画其他行
        for (int indexOfRow = 1; indexOfRow < table.length; indexOfRow++) {
            for (int indexOfCol = 0; indexOfCol < table[indexOfRow].length; indexOfCol++) {
                String data = table[indexOfRow][indexOfCol];
                if (data == null) data = "";
                boolean firstColumn = indexOfCol == 0;

                int x = firstColumn ?
                        0 :
                        firstColWidth + ((indexOfCol - 1) * colWidth);
                int y = yOffset + firstRowHeight + ((indexOfRow - 1) * rowHeight);
                int w = firstColumn ? firstColWidth : colWidth;
                int h = rowHeight;

                Color background = firstColumn ? HexConstant.FIRST_COL_BACKGROUND : HexConstant.ROW_BACKGROUND;
                Color board = HexConstant.FIRST_COL_BACKGROUND;
                drawText(graphicsContext, data, x, y, w, h, 0, 0, 1, 0, background, board);
            }
        }

        //画第一行
        for (int indexOfRow = 0, indexOfCol = 0; indexOfCol < table[0].length; indexOfCol++) {
            String data = table[indexOfRow][indexOfCol];
            if (data == null) data = "";
            boolean firstColumn = indexOfCol == 0;

            int x = firstColumn ?
                    0 :
                    firstColWidth + ((indexOfCol - 1) * colWidth);
            int y = 0;
            int w = (firstColumn ? firstColWidth : colWidth);
            int h = firstRowHeight;

            Color background = HexConstant.FIRST_ROW_BACKGROUND;
            Color board = Color.WHITE;
            drawText(graphicsContext, data, x, y, w, h, 0, 0, 0, 0, background, board);
        }
    }

    /**
     * 填满窗口最多所需
     */
    private int getMaxFillNumOfRowViewed() {
        double tmp = getMinFillNumOfRowViewed();
        if (tmp % 1 != 0) tmp += 1;
        return (int) (tmp + 1);
    }


    /**
     * 填满窗口最少所需
     */
    private double getMinFillNumOfRowViewed() {
        return (hBox.getHeight() - HexConstant.FIRST_ROW_HEIGHT) / HexConstant.ROW_HEIGHT;
    }

    private void update(Canvas canvas, String[] header, Function<Long, String[]> get, long length, ScrollBar bar, int firstColWidth, int colWidth, int firstRowHeight, int rowHeight) {
        //用第一列可用的最大偏移值,即距离可见范围第一列的行数,得出可见范围第一列的序号和偏移值
        double tmp = bar.getValue();

        long index = (long) tmp;
        double remainder = tmp - index;
        int yOffset = (int) -(remainder*rowHeight);

        String[][] table = new String[Math.toIntExact(Math.min((length - index), getMaxFillNumOfRowViewed())) + 1][];
        table[0] = header;
        for (int indexOfRow = 1; indexOfRow < table.length; indexOfRow++) {
            table[indexOfRow] = get.apply(index + indexOfRow - 1);
        }

        draw(canvas, table, firstColWidth, colWidth, firstRowHeight, rowHeight, yOffset);
    }

    private void update() {
        String[] hexHeader = new String[viewMode.numOfCol + 1];
        for (int i = 0; i < hexHeader.length; i++) {
            if (i == 0) {
                hexHeader[i] = "index";
                continue;
            }
            int index = i - 1;
            hexHeader[i] = String.format("%X", index);
        }

        update(
                hexCanvas,
                hexHeader,
                hexCache::get,
                getTotalNumOfRow(),
                bar,
                HexConstant.HEX_FIRST_COLUMN_WIDTH,
                HexConstant.HEX_COL_WIDTH,
                HexConstant.FIRST_ROW_HEIGHT,
                HexConstant.ROW_HEIGHT
        );
        update(
                asciiCanvas,
                new String[viewMode.numOfCol + 1],
                asciiCache::get,
                getTotalNumOfRow(),
                bar,
                0,
                HexConstant.ASCII_COL_WIDTH,
                HexConstant.FIRST_ROW_HEIGHT,
                HexConstant.ROW_HEIGHT
        );
    }

    private void resize() {
        double h = pane.getHeight() - 100;
        bar.setPrefHeight(h);

        hexCanvas.setWidth(HexConstant.HEX_FIRST_COLUMN_WIDTH + viewMode.numOfCol * HexConstant.HEX_COL_WIDTH);
        asciiCanvas.setWidth(viewMode.numOfCol * HexConstant.ASCII_COL_WIDTH);
        hexCanvas.setHeight(0);
        asciiCanvas.setHeight(0);

        Platform.runLater(() -> {
            hexCanvas.setHeight(hBox.getHeight());
            asciiCanvas.setHeight(hBox.getHeight());
        });

        Platform.runLater(() -> {
            bar.setMin(0);
            bar.setMax(Math.max(getTotalNumOfRow() - getMinFillNumOfRowViewed(), 0));
            bar.setVisible(getTotalNumOfRow() >= getMinFillNumOfRowViewed());//如果不足一页,则隐藏滚动条
            bar.setVisibleAmount(getMinFillNumOfRowViewed());
            setBarValue(bar.getValue());

            update();
        });
    }

    private void reinitialize() {
        clearCache();
        resize();
    }

    private void loadData() {
        fileService.open();
    }

    private LRUMapCache<Long, String[]> hexCache;
    private LRUMapCache<Long, String[]> asciiCache;

    private void clearCache() {
        hexCache.clear();
        asciiCache.clear();
    }

    private boolean unmatch(String str) {
        try {
            Long.parseLong(str, 16);
        } catch (NumberFormatException e) {
            return true;
        }
        return !str.matches("[0-9A-F]*");
    }

    private void initialCanvas() {
        hexCache = new LRUMapCache<>(HexConstant.TABLE_CACHE_SIZE, indexOfRow -> {
            String[] row = new String[viewMode.numOfCol + 1];
            long phyIndex = indexOfRow * viewMode.numOfCol;
            byte[] bytes = fileService.getBytes(phyIndex, viewMode.numOfCol);
            row[0] = String.format("%016x", phyIndex);
            for (int i = 0; i < viewMode.numOfCol; i++) {
                String tmp;
                if (fileService.isOutOfRange(phyIndex + i)) {
                    tmp = "";
                } else {
                    tmp = String.format("%02x", bytes[i]);
                }
                row[i + 1] = tmp;
            }
            return row;
        });
        asciiCache = new LRUMapCache<>(HexConstant.TABLE_CACHE_SIZE, indexOfRow -> {
            String[] row = new String[viewMode.numOfCol + 1];
            long phyIndex = indexOfRow * viewMode.numOfCol;
            byte[] bytes = fileService.getBytes(phyIndex, viewMode.numOfCol);
            row[0] = "";
            for (int i = 0; i < viewMode.numOfCol; i++) {
                String tmp;
                if (fileService.isOutOfRange(phyIndex + i)) {
                    tmp = "";
                } else {
                    char c = (char) bytes[i];
                    tmp = Character.isISOControl(c) ?
                            "□" : //U+25A1
                            String.valueOf(c);
                }
                row[i + 1] = tmp;
            }
            return row;
        });

        bar.valueProperty().addListener((_, _, newValue) -> {
            checkBarValue(newValue.doubleValue());
            Platform.runLater(this::update);
        });
    }

    private void initialStage() {
        pane.heightProperty().addListener(_ -> resize());
        stage.setOnCloseRequest(_ -> fileService.close());
    }

    private void initialInputs() {
        List.of(jmpAddrInput, fromInput, toInput).forEach(input -> {
            input.setText("0");
            input.textProperty().addListener((_, oldValue, newValue) -> {
                newValue = newValue.toUpperCase();
                if (unmatch(newValue)) {
                    if (unmatch(oldValue)) oldValue = "0";
                    input.setText(oldValue);
                } else {
                    input.setText(newValue);
                }
            });
        });
        toInput.setText(String.format("%X", fileService.getFileLength()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
        initialCanvas();
        initialStage();
        initialInputs();
        Platform.runLater(this::reinitialize);
    }

    public enum ViewMode {
        /**
         * 32列一行
         */
        COL32(
                32
        ),
        /**
         * 64列一行
         */
        COL64(
                64
        );

        private final int numOfCol;

        ViewMode(int numOfCol) {
            this.numOfCol = numOfCol;
        }
    }
}
