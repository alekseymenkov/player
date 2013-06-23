package knaapo.player;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import element_interface.ElementInterface;
import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import item.Item;
import item.ItemsCreator;
import logic.Project;
import logic.Scene;
import network.CommandPackageType;
import network.Network;
import network.NetworkListener;
import network.SocketState;
import network.SocketStateChangeListener;
import scroll_view.CustomScrollView;

public class MainActivity extends FragmentActivity implements TabListener, OnTaskCompleted {

    final static int INVALIDATE_WITHOUT_COLOR_CHANGE = 0;
    final static int INVALIDATE_WITH_COLOR_CHANGE = 1;
    final static int INVALIDATE_WITH_PARSING_DATA = 2;

    final static String LAST_OPENED_PROJECT = "lastOpenedProject";
    private String mCurrentProjectDirectory;

    // Меню
    static Menu mMenu;

    // Сетевой класс + поток
    static ArrayList<Network> mNetwork = new ArrayList<Network>();
    static ArrayList<Thread> mNetworkThread = new ArrayList<Thread>();

    // Поток для отслеживания перерисовки
    static RepaintWatcher mRepaintWatcher;
    Thread mRepaintWatcherThread;

    // Контекст
    static Context mContext;

    // Фабрика элементов
    ItemsCreator mItemsCreator;
    // Элементы
    private static ArrayList<ArrayList<Item>> mItems = new ArrayList<ArrayList<Item>>();

    // Проект
    Project mProject;
    static ArrayList<Scene> mSceneSettings = new ArrayList<Scene>();


    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    static ViewPager mViewPager;

    ActionBar mActionBar;

    private static final int REQ_CHOOSE_FILE = 1;

    AsyncImageProcessing mAsyncImageProcessing = null;

    private static NetworkPreferences mNetworkPreferences;


    private static boolean mIsProjectLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Сохранение контекста
        mContext = this;

        // Инициализация фабрики элементов
        mItemsCreator = new ItemsCreator(this);

        // Инициализация ActionBar
        mActionBar = getActionBar();
        // Отключение кнопки "Домой"
        mActionBar.setHomeButtonEnabled(false);
        // Включение отображения вкладок в ActionBar
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mNetworkPreferences = new NetworkPreferences(this);
        mNetworkPreferences.loadPreferences();

        // Загрузка директории предыдущего проекта
        loadSettings();

        // Загрузка проекта
        loadProject();
    }


    private void loadProject() {
        // Адаптер фрагментов
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        if (!mCurrentProjectDirectory.isEmpty()) {
            try {
                // Загрузка файла project.xml из директории проекта
                mProject = new Project(mCurrentProjectDirectory);
                // Установка количества вкладок в адаптере
                mAppSectionsPagerAdapter.setCount(mProject.getUnitsCount());

                // Установка заголовка
                this.setTitle(mProject.getProjectName());

                // Загрузка настроек сцены
                for (int i = 0; i < mProject.getUnitsCount(); i++) {
                    Scene scene = new Scene(mCurrentProjectDirectory, i);
                    mSceneSettings.add(scene);

                    mNetwork.add(null);
                    mNetworkThread.add(null);
                }

                // Загрузка элементов
                for (int i = 0; i < mProject.getUnitsCount(); i++) {
                    mItems.add(mItemsCreator.loadItems(mCurrentProjectDirectory, i));
                }

                // Структура данных для repaintWatcher
                ArrayList<Integer> itemsCount = new ArrayList<Integer>(mProject.getUnitsCount());
                for (int i = 0; i < mProject.getUnitsCount(); i++)
                    itemsCount.add(mItems.get(i).size());

                // Инициализация потока и слушателя, ответственных за перерисовку элементов по таймеру
                mRepaintWatcher = new RepaintWatcher(mProject.getUnitsCount(), itemsCount);
                mRepaintWatcher.setRepaintListener(mRepaintListenter);
                mRepaintWatcherThread = new Thread(mRepaintWatcher, "repaintWatcher");
                mRepaintWatcherThread.start();

                // Установка слушателей для элементов
                for (int i = 0; i < mProject.getUnitsCount(); i++) {
                    for (int j = 0; j < mItems.get(i).size(); j++) {
                        mItems.get(i).get(j).setRepaintListener(mRepaintListenter);
                        mItems.get(i).get(j).setTooltipListener(mTooltipListener);
                    }
                }

                // Создание вкладок для проекта
                for (int i = 0; i < mProject.getUnitsCount(); i++) {

                    Tab tab = mActionBar.newTab();
                    tab.setTabListener(this);
                    tab.setText(mSceneSettings.get(i).getSceneName());
                    mActionBar.addTab(tab);
                }

                // Запуск процедуры загрузки изображений
                mAsyncImageProcessing = new AsyncImageProcessing(mCurrentProjectDirectory, mProject.getUnitsCount());
                mAsyncImageProcessing.setOnTaskCompleteListener(this);
                mAsyncImageProcessing.execute(mSceneSettings);
            } catch (IOException exception) {
                Toast.makeText(this, getString(R.string.open_project_error), Toast.LENGTH_LONG).show();
                exception.printStackTrace();
            }
        }
    }


    private void openProject(String path) {

        if (mProject != null)
            closeProject();

        mCurrentProjectDirectory = path;
        saveSettings();

        loadProject();


    }

    private void closeProject() {

        if (!mIsProjectLoaded) return;

        // Удаление вкладкок
        mActionBar.removeAllTabs();

        // Таймеры перерисовки
        mRepaintWatcher.stop();
        mRepaintWatcher = null;
        mRepaintWatcherThread = null;


        for (int i = 0; i < mProject.getUnitsCount(); i++) {
            if (mNetwork.get(i) != null)
                mNetwork.get(i).close();
        }

        mItems.clear();

        mSceneSettings.clear();

        mNetwork.clear();
        mNetworkThread.clear();

        mAppSectionsPagerAdapter.setCount(0);
        mAppSectionsPagerAdapter = null;

        mProject = null;


        mViewPager.removeAllViews();


        // Установка заголовка
        this.setTitle(R.string.default_project_name);

        mIsProjectLoaded = false;

    }

    @Override
    public void onBackPressed() {
        Log.d("DEBUG", "Close the App");
        closeApplication();
    }


    @Override
    public void onTaskCompleted() {

        // Инициализация ViewPager, подключение адаптера с фрагментами и слушателя OnPageSelected
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(10);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Когда пользователь переключается между вкладками, выбор связанной вкладки
                // В принципе, мы можем так же использовать ActionBar.Tab#select(), если у нас есть ссылка на tab.
                mActionBar.setSelectedNavigationItem(position);

                if (mNetwork.get(position) == null || !mNetwork.get(position).isConnected())
                    if (mNetworkPreferences.isPreferencesCorrect())
                        connectToServer(mNetworkPreferences.getServerAddress(), mNetworkPreferences.getPort());
                    else
                        showNetworkParamsDialog(true);
            }
        });

        mIsProjectLoaded = true;

        Log.d("Network", "Auto-connection in first tab, settings: " + mNetworkPreferences.isPreferencesCorrect());

        // Подключение первой вкладки
        if (mNetworkPreferences.isPreferencesCorrect())
            connectToServer(mNetworkPreferences.getServerAddress(), mNetworkPreferences.getPort());
        else
            showNetworkParamsDialog(true);


    }

    // ========================================================================================
    // ======================= Асинхронная загрузка изображений ===============================
    // ========================================================================================

    public static class AsyncImageProcessing extends AsyncTask<ArrayList<Scene>, Integer, Boolean> {

        private String mDirectory;
        private ProgressDialog mProgressDialog;
        private OnTaskCompleted mOnTaskCompleteListener;
        ;

        public AsyncImageProcessing(String directory, int filesCount) {
            mDirectory = directory + "/";
            // Инициализация GUI
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle("Загрузка проекта");
            mProgressDialog.setMessage("Обработка изображений");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(200);
            mProgressDialog.setIndeterminate(false);
        }

        public void setOnTaskCompleteListener(OnTaskCompleted listener) {
            mOnTaskCompleteListener = listener;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(ArrayList<Scene>... params) {

            ArrayList<Scene> sceneSettings = params[0];


            // Перебор всех закладок
            for (int k = 0; k < sceneSettings.size(); k++) {


                // Информация о местоположении фонового изображения содержится с расширением .svg
                // Перед использованием строки расположения картинки необходимо удалить 4 символа с конца строки
                String incorrectPath = mDirectory + sceneSettings.get(k).getBackgroundImage();
                String correctPath = incorrectPath.substring(0, incorrectPath.length() - 4);

                // Создание директории
                File directory = new File(correctPath);
                directory.mkdir();

                // Проверка наличия файла с разметкой подложки
                File htmlFile = new File(correctPath + ".html");
                // Установка пути к файлу с разметкой
                mSceneSettings.get(k).setBackgroundHTML(htmlFile.getAbsolutePath());

                BitmapRegionDecoder brd = null;
                if (sceneSettings.get(k).getWidth() == 0 || sceneSettings.get(k).getHeight() == 0) {
                    try {
                        brd = BitmapRegionDecoder.newInstance(correctPath + ".png", true);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    mSceneSettings.get(k).setSize(brd.getWidth(), brd.getWidth());
                }

                if (!htmlFile.exists()) {
                    // 0 - текущий прогресс файлов, 1 - всего файлов, 2 - текущий прогресс изображений, 3 - всего изображений
                    publishProgress(k + 1, sceneSettings.size());

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;

                    int totalI = (int) Math.ceil(brd.getHeight() / 256.0);
                    int totalJ = (int) Math.ceil(brd.getWidth() / 256.0);
                    int totalFragmentsCount = (totalI - 1) * (totalJ - 1);
                    String html = "<body style='margin: 0; padding:0;'><table border = '0' cellpadding = '0' cellspacing = '0'>";
                    for (int i = 0; i < Math.ceil(brd.getHeight() / 256.0); i++) {
                        html += "<tr>";
                        for (int j = 0; j < Math.ceil(brd.getWidth() / 256.0); j++) {
                            // 0 - текущий прогресс файлов, 1 - всего файлов, 2 - текущий прогресс изображений, 3 - всего изображений
                            publishProgress(k + 1, sceneSettings.size(), i * totalJ + j + 1, totalFragmentsCount);

                            html += "<td><img src='" + correctPath + "/image-" + i + "-" + j + ".png'></td>";
                            int startX = j * 256;
                            int startY = i * 256;
                            int endX = (j + 1) * 256 > brd.getWidth() ? brd.getWidth() : (j + 1) * 256;
                            int endY = (i + 1) * 256 > brd.getHeight() ? brd.getHeight() : (i + 1) * 256;
                            Bitmap bitmap = brd.decodeRegion(new Rect(startX, startY, endX, endY), options);
                            savePicture(correctPath + "/image", i, j, bitmap);
                        }
                        html += "</tr>";
                    }
                    html += "</table></body>";

                    try {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(htmlFile), "UTF-8"));
                        writer.write(html);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }
            }

            return !isCancelled();

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            if (!mProgressDialog.isShowing())
                mProgressDialog.show();

            if (progress.length == 2) {
                mProgressDialog.setTitle("Обработка изображений (" + progress[0] + " из " + progress[1] + ")");

            } else if (progress.length == 4) {
                if (mProgressDialog.getMax() != progress[3]) {
                    mProgressDialog.setMax(progress[3]);

                }
                mProgressDialog.setProgress(progress[2]);

            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            if (mOnTaskCompleteListener != null)
                mOnTaskCompleteListener.onTaskCompleted();
        }


        private String savePicture(String fileName, int i, int j, Bitmap bitmap) {
            FileOutputStream fOut;

            try {
                File file = new File(fileName + "-" + i + "-" + j + ".png");
                fOut = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut); // сохранять картинку в jpeg-формате с 85% сжатия.
                fOut.flush();
                fOut.close();
            } catch (Exception e) // здесь необходим блок отслеживания реальных ошибок и исключений, общий Exception приведен в качестве примера
            {
                return e.getMessage();
            }
            return "";
        }

    }


    // ========================================================================================
    // ============================= Интерфейс TabListener ====================================
    // ========================================================================================

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // При смене вкладки в ActionBar, меняется соответствующая страница в ViewPager
        if (mViewPager != null) {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    // ========================================================================================
    // ========================= Классы для работы с ViewPager ================================
    // ========================================================================================

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private int mPagesCount;

        public AppSectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }


        @Override
        public Fragment getItem(int i) {

            SectionFragment fragment = new SectionFragment();
            fragment.setFragmentNumber(i);
            fragment.setRetainInstance(true);

            return fragment;


        }

        @Override
        public int getCount() {
            return mPagesCount;
        }

        public void setCount(int pagesCount) {
            mPagesCount = pagesCount;
        }
    }


    public static class SectionFragment extends Fragment {

        private int mPageNumber;

        public SectionFragment() {
        }

        public void setFragmentNumber(int pageNumber) {
            mPageNumber = pageNumber;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            View rootView = inflater.inflate(R.layout.base_fragment, container, false);
            FrameLayout baseFrameLayout = (FrameLayout) rootView.findViewById(R.id.baseFrameLayout);
            final CustomScrollView customScrollView = (CustomScrollView) rootView.findViewById(R.id.customScrollView);
            CustomWebView customWebView = (CustomWebView) rootView.findViewById(R.id.customWebView);
            baseFrameLayout.removeView(customScrollView);
            customWebView.addView(customScrollView, mSceneSettings.get(mPageNumber).getWidth(), mSceneSettings.get(mPageNumber).getHeight());

            class MyWebViewClient extends WebViewClient {
                @Override
                public void onScaleChanged(WebView wv, float oldScale, float newScale) {
                    for (int i = 0; i < mItems.get(mPageNumber).size(); i++) {
                        customScrollView.changeView(newScale, i);
                        mItems.get(mPageNumber).get(i).setScaleValue(newScale);
                        mItems.get(mPageNumber).get(i).requestLayout();
                    }

                }
            }

            customWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            customWebView.setWebViewClient(new MyWebViewClient());
            customWebView.getSettings().setBuiltInZoomControls(true);
            customWebView.getSettings().setSupportZoom(true);
            customWebView.getSettings().setDisplayZoomControls(true);
            customWebView.getSettings().setUseWideViewPort(true);
            //			customWebView.getSettings().setLoadWithOverviewMode(true);

            customWebView.getSettings().setDefaultTextEncodingName("utf-8");
            customWebView.loadUrl("file://" + mSceneSettings.get(mPageNumber).getBackgroundHTML());
            //customWebView.loadData("<html><body bgcolor=\"Black\"></body></html>","text/html", "utf-8");
            for (int i = 0; i < mItems.get(mPageNumber).size(); i++) {
                Item item = mItems.get(mPageNumber).get(i);
                customScrollView.addWidget(item, item.getXPosition(), item.getYPosition());
            }

            return rootView;
        }
    }


    private void saveSettings() {
        // Режим работы с настройками
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(LAST_OPENED_PROJECT, mCurrentProjectDirectory);
        editor.apply();
    }


    private void loadSettings() {
        // Загрузка настроек
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        mCurrentProjectDirectory = preferences.getString(LAST_OPENED_PROJECT, "");
    }


    private void openProjectPickerDialog() {
        Intent intent = new Intent(this, FileChooserActivity.class);
        intent.putExtra(FileChooserActivity._Theme, android.R.style.Theme_Dialog);
        startActivityForResult(intent, REQ_CHOOSE_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CHOOSE_FILE:
                if (resultCode == RESULT_OK) {

                    List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
                    for (File f : files) {
                        openProject(f.getAbsoluteFile().toString());
                    }
                }
                break;
        }
    }


    // Получение данных из сети
    NetworkListener mNetworkListener = new NetworkListener() {

        @Override
        public void dataPackageReceive(int id, int elementID, byte[] data) {
            if (mItems.size() > id && mItems.get(id).size() > elementID)
                mItems.get(id).get(elementID).setData(data);
        }

        @Override
        public void commandPackageReceive(int id, CommandPackageType commandPackageType) {
        }
    };


    // Слушатель изменений состояния сокета
    SocketStateChangeListener mSocketStateChangeListener = new SocketStateChangeListener() {

        @Override
        public void OnChangeSocketState(int id, SocketState socketState) {

            Message msg = mNetworkStateChangeHandler.obtainMessage(id, socketState);
            mNetworkStateChangeHandler.sendMessage(msg);

            // Соединение с сервером установлено
            // Регистрация клиентского приложения на сервере (MMF + элементы)
            if (socketState == SocketState.Connected) {
                mNetwork.get(id).sendMMFPackage(mSceneSettings.get(id).getMMFName());
                for (int i = 0; i < mItems.get(id).size(); i++) {
                    ArrayList<Integer> range = mItems.get(id).get(i).getElementInterface().calculateRange();
                    int[] startBytes = {range.get(0)};
                    int[] bytesCount = {range.get(1)};
                    mNetwork.get(id).sendRegInfoPackageWithDirectMode(i, startBytes, bytesCount);
                }
                mNetwork.get(id).sendCommandPackage(CommandPackageType.IsReady);
            }
        }
    };


    // Слушатель анимации
    static RepaintListener mRepaintListenter = new RepaintListener() {

        @Override
        public void repaintNeeded(int groupID, int elementID, boolean flag) {
            if (flag)
                mInvalidateHandler.sendMessage(mInvalidateHandler.obtainMessage(INVALIDATE_WITH_COLOR_CHANGE, groupID, elementID));
            else
                mInvalidateHandler.sendMessage(mInvalidateHandler.obtainMessage(INVALIDATE_WITHOUT_COLOR_CHANGE, groupID, elementID));
        }

        @Override
        public void parseAndRepaintNeeded(int groupID, int elementID) {
            mInvalidateHandler.sendMessage(mInvalidateHandler.obtainMessage(INVALIDATE_WITH_PARSING_DATA, groupID, elementID));
        }

        @Override
        public void repaintFlagChanged(int groupID, int elementID, boolean flag) {
            mRepaintWatcher.setIsItemBlinked(groupID, elementID, flag);
        }
    };


    // Слушатель всплывающей подсказки
    static TooltipListener mTooltipListener = new TooltipListener() {



        public void displayData(int elementID, ArrayList<String> parametersString) {

            final int currentTab = mViewPager.getCurrentItem();

            final Dialog dialog = new Dialog(mContext);
            final ElementInterface elementInterface = mItems.get(currentTab).get(elementID).getElementInterface();
            dialog.setContentView(R.layout.tooltip);
            dialog.setTitle(elementInterface.getElementName());
            TableLayout table = (TableLayout) dialog.findViewById(R.id.tblTooltip);



            int realParameterNum = -1;
            for (int i = -1; i < elementInterface.getParameters().size(); i++) {

                if (i >= 0 && !elementInterface.getParameters().get(i).isUsed()) {
                    continue;
                }

                TableRow tableRow = new TableRow(mContext);

                TextView bitTV = new TextView(mContext);
                bitTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                bitTV.setBackgroundResource(android.R.drawable.edit_text);
                if (i  < 0) {
                    bitTV.setText(R.string.toolTipParameter);
                } else {
                    bitTV.setText(String.valueOf(realParameterNum + 1));
                }
                tableRow.addView(bitTV);

                TextView valueTV = new TextView(mContext);
                valueTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                valueTV.setBackgroundResource(android.R.drawable.edit_text);
                if (i < 0) {
                    valueTV.setText(R.string.toolTipValue);
                } else {
                    valueTV.setText(parametersString.get(i));
                }
                tableRow.addView(valueTV);

                TextView descTV = new TextView(mContext);
                descTV.setBackgroundResource(android.R.drawable.edit_text);
                if (i < 0) {
                    descTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    descTV.setText(R.string.toolTipDesc);
                } else {
                    descTV.setGravity(Gravity.CENTER_VERTICAL);
                    descTV.setText(elementInterface.getParameters().get(i).getDescription());
                }
                tableRow.addView(descTV);

                table.addView(tableRow);

                realParameterNum++;
            }

            table.setColumnStretchable(2, true);

            dialog.show();

        }

        @Override
        public void generateTooltip(int elementID) {
            // TODO Auto-generated method stub

        }
    };


    // TODO: Добавить действие на отключение (отключение, а не вызов окна)
    // TODO: Отображение окна изменить внешний вид


    // Обработчик сообщений о состоянии подключения
    static Handler mNetworkStateChangeHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int id = msg.what;
            SocketState socketState = (SocketState) msg.obj;
            switch (socketState) {
                case Connecting:
                    Log.d("Network", "IOException, SocketState - Connecting");
                    break;
                case Connected:
                    mMenu.findItem(R.id.menu_connection).setTitle(R.string.menu_disconnect);
                    Toast.makeText(mContext, String.format(mContext.getString(R.string.connection_success), mNetworkPreferences.getServerAddress()), Toast.LENGTH_SHORT).show();
                    Log.d("Network", "IOException, SocketState - Connected");
                    break;
                case Disconnected:
                    mMenu.findItem(R.id.menu_connection).setTitle(mContext.getString(R.string.menu_connect));
                    Toast.makeText(mContext, String.format(mContext.getString(R.string.connection_fail), mNetworkPreferences.getServerAddress()), Toast.LENGTH_SHORT).show();
                    resetItemsState(id);
                    disconnectFromServer(id);
                    Log.d("Network", "IOException, SocketState - Disconnected");
                    break;
                case Aborted:
                    mMenu.findItem(R.id.menu_connection).setTitle(R.string.menu_connect);
                    Toast.makeText(mContext, String.format(mContext.getString(R.string.connection_aborted), mNetworkPreferences.getServerAddress()), Toast.LENGTH_SHORT).show();
                    resetItemsState(id);
                    disconnectFromServer(id);
                    Log.d("Network", "IOException, SocketState - Aborted");
                    break;
            }
        }

        ;
    };

    private static void resetItemsState(int id) {

        if (!mIsProjectLoaded) return;

        for (int i = 0; i < mItems.get(id).size(); i++) {
            mItems.get(id).get(i).resetState();
            mItems.get(id).get(i).invalidate();
        }
    }


    // Обработчик сообщений о перерисовке элементов
    static Handler mInvalidateHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            int arg1 = msg.arg1;
            int arg2 = msg.arg2;

            if (mItems.size() > arg1 && mItems.get(arg1).size() > arg2) {
                switch (msg.what) {
                    case INVALIDATE_WITHOUT_COLOR_CHANGE:
                        mItems.get(arg1).get(arg2).invalidate();
                        break;
                    case INVALIDATE_WITH_COLOR_CHANGE:
                        mItems.get(arg1).get(arg2).changeColor();
                        mItems.get(arg1).get(arg2).invalidate();
                        break;
                    case INVALIDATE_WITH_PARSING_DATA:
                        mItems.get(arg1).get(arg2).parseData();
                        mItems.get(arg1).get(arg2).invalidate();
                        break;
                }
            }
        }

        ;
    };


    OnTouchListener onWebViewTouchListener = new OnTouchListener() {

        float mScaleValue = 0.0f;

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            float scaleValue = ((WebView) view).getScale();
            if (Math.abs(scaleValue - mScaleValue) > 1e-6) {
                mScaleValue = scaleValue;

                for (int i = 0; i < 48; i++) {
                    //					mCustomScrollView.changeView(mScaleValue, i);
                    //										mItems.get(i).setScaleValue(mScaleValue);
                    //										mItems.get(i).requestLayout();
                }
            }
            return false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        mMenu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!mIsProjectLoaded) return true;

        // Текущая вкладка
        final int currentTab = mViewPager.getCurrentItem();

        Log.d("Network", "Options menu: " + ((mNetwork.get(currentTab) != null && mNetwork.get(currentTab).isConnected()) ? "disconnect" : "connect"));
        MenuItem item = menu.findItem(R.id.menu_connection);
        if (mNetwork.get(currentTab) != null && mNetwork.get(currentTab).isConnected())
            item.setTitle(R.string.menu_disconnect);
        else
            item.setTitle(R.string.menu_connect);

        return true;
    }


    // Обработка действий при выборе пунктов меню
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Открытие проекта
            case R.id.menu_open_project:
                Intent intent = new Intent(this, FileChooserActivity.class);
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
                startActivityForResult(intent, REQ_CHOOSE_FILE);
                break;

            // Закрытие проекта
            case R.id.menu_close_project:
                closeProject();
                break;

            // Подключение к сети / Вызов настроек проекта
            case R.id.menu_connection:
                if (!mIsProjectLoaded) break;
                final int id = mViewPager.getCurrentItem();
                if (mNetwork.get(id) != null && mNetwork.get(id).isConnected()) {
                    disconnectFromServer(id);
                } else {
                    showNetworkParamsDialog(true);
                }
                break;

            case R.id.menu_settings:
                if (!mIsProjectLoaded) break;
                showNetworkParamsDialog(false);
                break;

            case R.id.menu_exit:
                closeApplication();
        }

        return super.onOptionsItemSelected(item);
    }


    // Отобразить диалог с сетевыми настройками
    private void showNetworkParamsDialog(final boolean isConnect) {

        if (!mIsProjectLoaded) return;

        final int currentTab = mViewPager.getCurrentItem();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title));

        final View view = this.getLayoutInflater().inflate(R.layout.network_settings, null);

        final String positiveButtonText = isConnect ? getString(R.string.dialog_button_connect) : getString(R.string.dialog_button_save);
        builder.setView(view)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        String serverAddress = ((EditText) (view.findViewById(R.id.serverAddress))).getText().toString();
                        int port = Integer.parseInt(((EditText) (view.findViewById(R.id.serverPort))).getText().toString());
                        mSceneSettings.get(currentTab).setMMFName(((EditText) (view.findViewById(R.id.mmfName))).getText().toString());

                        // Сохранение сетевых настроек
                        mNetworkPreferences.setServerAddress(serverAddress);
                        mNetworkPreferences.setPort(port);
                        mNetworkPreferences.savePreferences();

                        // Подключение
                        if (isConnect)
                            connectToServer(serverAddress, port);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.cancel();
                    }
                });

        // Элементы UI
        EditText serverAddress = (EditText) view.findViewById(R.id.serverAddress);
        EditText port = (EditText) view.findViewById(R.id.serverPort);
        EditText mmfName = (EditText) view.findViewById(R.id.mmfName);

        // Установка базовых значений для элементов UI
        serverAddress.setText(mNetworkPreferences.getServerAddress());
        port.setText(String.valueOf(mNetworkPreferences.getPort()));
        mmfName.setText(mSceneSettings.get(currentTab).getMMFName());
        builder.create().show();
    }


    // Закрытие приложения
    // FIX: Варварский способ закрытия приложения, необходимо разобраться с Fragments
    private void closeApplication() {

        System.exit(0);
    }


    // Подключение к серверу
    private void connectToServer(String serverAddress, int port) {

        if (!mIsProjectLoaded) return;


        // Текущая открытая вкладка
        final int currentTab = mViewPager.getCurrentItem();
        Log.d("ViewPager", "Connect to server: " + currentTab);

        // Создание нового сетевого класса
        Network network = new Network(currentTab);
        network.setSocketStateChangeListener(mSocketStateChangeListener);
        network.setNetworkListener(mNetworkListener);
        mNetwork.set(currentTab, network);
        mNetworkThread.set(currentTab, new Thread(network, "network_" + currentTab));

        // Подключение к серверу
        mNetwork.get(currentTab).setServerAddress(serverAddress, port);
        mNetworkThread.get(currentTab).start();
    }


    // Отключение от сервера
    private static void disconnectFromServer(int id) {

        if (!mIsProjectLoaded) return;

        Log.d("ViewPager", "Disconnect from server: " + id);

        // Закрытие текущей сетевой сессии
        // TODO: Рефакторинг. Два раза вызывается участок кода при ручном отключении
        if (mNetwork.get(id) != null)
            mNetwork.get(id).close();
        mNetwork.set(id, null);
        mNetworkThread.set(id, null);
    }
}

