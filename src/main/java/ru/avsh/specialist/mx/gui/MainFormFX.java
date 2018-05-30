package ru.avsh.specialist.mx.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.avsh.specialist.mx.root.SpecialistMX;
import ru.avsh.specialist.mx.units.memory.sub.ScreenFx;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import static ru.avsh.specialist.mx.helpers.Constants.*;

/**
 * Класс для формирования главного окна приложения (меню, кнопоки обработчики событий).
 *
 * @author -=AVSh=-
 */
public class MainFormFX extends Application {
    private static final double IMAGE_WIDTH         = ScreenFx.SCREEN_WIDTH ;
    private static final double IMAGE_HEIGHT        = ScreenFx.SCREEN_HEIGHT;
    private static final double IMAGE_DOUBLE_WIDTH  = ScreenFx.SCREEN_WIDTH  << 1;
    private static final double IMAGE_DOUBLE_HEIGHT = ScreenFx.SCREEN_HEIGHT << 1;

    private static final String INI_OPTION_FORM_WIDTH  = "MainFormWidth" ;
    private static final String INI_OPTION_FORM_HEIGHT = "MainFormHeight";
    private static final String DISK_A   =   "[A:] - ";
    private static final String DISK_B   =   "[B:] - ";
    private static final String NO_DISK  = "нет диска";
    private static final String ROM_PREF =  "[ROM] - ";

    private final SpecialistMX fSpMX;

    /**
     * Конструктор.
     */
    public MainFormFX() {
        fSpMX = new SpecialistMX();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final      MenuItem  openItem = new      MenuItem("Открыть…");
        final CheckMenuItem   romItem = new CheckMenuItem(ROM_PREF);
        final CheckMenuItem diskAItem = new CheckMenuItem(DISK_A.concat(NO_DISK));
        final CheckMenuItem diskBItem = new CheckMenuItem(DISK_B.concat(NO_DISK));
        final      MenuItem  saveItem = new      MenuItem("Сохранить блок...");
        final      MenuItem resetItem = new      MenuItem("Сбросить");
        final      MenuItem  exitItem = new      MenuItem("Выход");
        final      Menu      fileMenu = new      Menu    ("Файл", null,
                                          openItem,
                new SeparatorMenuItem(),   romItem,
                new SeparatorMenuItem(), diskAItem, diskBItem,
                new SeparatorMenuItem(),  saveItem,
                new SeparatorMenuItem(), resetItem,
                new SeparatorMenuItem(),  exitItem);

        final      MenuItem   infoItem = new      MenuItem("Состояние CPU/RAM");
        final      MenuItem  debugItem = new      MenuItem("Запуск отладчика");
        final CheckMenuItem size11Item = new CheckMenuItem("Размер 1:1");
        final CheckMenuItem size21Item = new CheckMenuItem("Размер 2:1");
        final RadioMenuItem modeMXItem = new RadioMenuItem("Клавиатура \"".concat(SPMX_NAME).concat("\""));
        final RadioMenuItem modeSTItem = new RadioMenuItem("Клавиатура \"Специалист\"");
        final      Menu       viewMenu = new      Menu    ("Просмотр", null,
                infoItem,  debugItem,
                new SeparatorMenuItem(), size11Item, size21Item,
                new SeparatorMenuItem(), modeMXItem, modeSTItem);

        final   ToggleGroup       group = new ToggleGroup();
        modeMXItem.setToggleGroup(group);
        modeSTItem.setToggleGroup(group);
        modeMXItem.setSelected(!fSpMX.isKeyboardMode());
        modeSTItem.setSelected( fSpMX.isKeyboardMode());

        final MenuItem aboutItem = new MenuItem("О программе...");
        final Menu     aboutMenu = new Menu    ("О программе", null, aboutItem);

        final MenuBar menuBar = new MenuBar(fileMenu, viewMenu, aboutMenu);

        final ImageView imageView = new ImageView(fSpMX.getScreen());
        imageView.setPreserveRatio(true );
        imageView.setSmooth       (false);
        imageView.setCache        (true );

        final Button      openBtn = new Button("Открыть"  );
        final Button      saveBtn = new Button("Сохранить");
        final Button     resetBtn = new Button("Сбросить" );
        final Button     debugBtn = new Button("Отладчик" );
        final ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(openBtn, saveBtn, resetBtn, debugBtn);
        buttonBar.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));

         openBtn.setTooltip(new Tooltip("Открывает (загружает и запускает) файлы *.rom, *.mon, *.cpu(i80), *.rks, *.odi"));
         saveBtn.setTooltip(new Tooltip("Сохраняет блок данных в файлы *.cpu(i80) и *.rks"));
        resetBtn.setTooltip(new Tooltip("Сбрасывает эмулятор в исходное состояние"));
        debugBtn.setTooltip(new Tooltip("Запускает отладчик"));

        final VBox  root  = new VBox(menuBar, imageView, buttonBar);
        final Scene scene = new Scene(root, -1, -1);
        primaryStage.setScene(scene);
        primaryStage.show();
        //--------------------------------------------------------------------------------------------------------------

        primaryStage.setMinWidth (primaryStage.getWidth ());
        primaryStage.setMinHeight(primaryStage.getHeight());

        final double deltaW = primaryStage.getWidth () - IMAGE_WIDTH ;
        final double deltaH = primaryStage.getHeight() - IMAGE_HEIGHT;

        imageView.fitWidthProperty().addListener((obs, oldValue, newValue) -> {
            size11Item.setSelected((double) newValue == IMAGE_WIDTH       );
            size21Item.setSelected((double) newValue == IMAGE_DOUBLE_WIDTH);
        });

        imageView.setFitWidth (IMAGE_WIDTH );
        imageView.setFitHeight(IMAGE_HEIGHT);

        primaryStage.widthProperty ().addListener((obs, oldValue, newValue) -> imageView.setFitWidth ((Double) newValue - deltaW));
        primaryStage.heightProperty().addListener((obs, oldValue, newValue) -> imageView.setFitHeight((Double) newValue - deltaH));

        // -= Изменение размеров окна программы =-
        final EventHandler<ActionEvent> sizeEventHandler = event -> {
            final CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();
            if (size11Item.equals(checkMenuItem)) {
                primaryStage.setWidth (deltaW + IMAGE_WIDTH );
                primaryStage.setHeight(deltaH + IMAGE_HEIGHT);
            } else {
                primaryStage.setWidth (deltaW + IMAGE_DOUBLE_WIDTH );
                primaryStage.setHeight(deltaH + IMAGE_DOUBLE_HEIGHT);
            }
            primaryStage.centerOnScreen();
        };
        size11Item.setOnAction(sizeEventHandler);
        size21Item.setOnAction(sizeEventHandler);

        exitItem.setOnAction(event -> primaryStage.fireEvent(new WindowEvent(primaryStage, WINDOW_CLOSE_REQUEST)));

        // -= Режим работы клавиатуры =-
        modeMXItem.setOnAction(event -> fSpMX.setKeyboardMode(false));
        modeSTItem.setOnAction(event -> fSpMX.setKeyboardMode(true ));

        // -= О программе =-
        aboutItem.setOnAction(event -> {
            String name      = "Эмулятор \"".concat(SPMX_NAME).concat("\"");
            String version   = "x.x.x.x";
            String copyright = "Copyright © 2018 \"AVSh Software\" (Александр Шевцов)";

            final InputStream is = getResourceAsStream(SPMX_PROP_FILE);
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is, "UTF-8")) {
                    final Properties property = new Properties();
                    property.load(isr);
                    name      = property.getProperty("productName"  , name     );
                    version   = property.getProperty("versionNumber", version  );
                    copyright = property.getProperty("copyright"    , copyright);
                } catch (IOException ex) {
                    //
                }
            }
            final Alert alert = new Alert(INFORMATION);
            alert.setTitle      ("Информация");
            alert.setHeaderText (null);
            alert.setContentText(String.format("%s v%s%n%n%s", name, version, copyright));
            alert.showAndWait   ();
        });

        // -= Обработка событий клавиатуры (нажатие) =-
        scene.setOnKeyPressed(event -> {
            final KeyCode keyCode = event.getCode();
            switch (keyCode) {
                // Нажатие на клавишу "Pause" приостанавливает компьютер
                case PAUSE:
                    if (fSpMX.isPaused()) {
                        fSpMX.pause(false, true);
                        primaryStage.setTitle("");
                    } else {
                        fSpMX.pause(true, true);
                        primaryStage.setTitle(" (пауза)");
                    }
                    break;
                // Нажатие на клавишу "Scroll Lock" запускает отладчик
                case SCROLL_LOCK:
                    fSpMX.startDebugger();
                    break;
                default:
                    fSpMX.keyCodeReceiver(true, keyCode);
            }
        });

        // -= Обработка событий клавиатуры (отпускание) =-
        scene.setOnKeyReleased(event -> fSpMX.keyCodeReceiver(false, event.getCode()));

        // Обработчик вызывается, когда окно закрывается - для завершения работы программы
        primaryStage.setOnCloseRequest(event -> {
            // Запоминаем размеры формы в ini-файле
            fSpMX.putIni(INI_SECTION_CONFIG, INI_OPTION_FORM_WIDTH, primaryStage.getWidth ());
            fSpMX.putIni(INI_SECTION_CONFIG, INI_OPTION_FORM_HEIGHT, primaryStage.getHeight());
            // Сохраняем ini-файл
            fSpMX.storeIni();
            // Завершаем приложение JavaFX
            Platform.exit();
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // Останавливаем компьютер
        fSpMX.pause(true, true);
        // Закрываем открытые ресурсы запоминающих устройств
        fSpMX.getMemoryUnitManager().close();
        // Завершаем приложение
        System.exit(0);
    }
}
