package item;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import knaapo.player.RepaintListener;
import knaapo.player.TooltipListener;

import parameter.Parameter;
import parameter.ParameterType;
import record.Record;

import element_interface.ElementInterface;
import element_interface.ElementInterfaceExtraParameter;
import element_interface.ElementInterfaceParameter;
import element_logic.ElementLogic;
import element_logic.ElementLogicType;
import figure.Figure;
import figure.FigureType;
import gdi_object.GDIObject;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;


public class Item extends View {

    // Значение смещения для прорисовки боковых граней
    // Если для элемента установлена область conflictOffset, то mPaintOffset == conflictOffset
    private static final int PAINT_OFFSET = 5;

    //static final float selectionLine = 5.5f;
    //static final float contourLine = 4.75f;
    static final float outerLine = 2.75f;
    //static final float innerLine = 1.5f;

    int mMarginLeft = 0;
    int mMarginTop = 0;

    // Логика
    private ElementLogic mElementLogic = null;

    // Интерфейс
    private ElementInterface mElementInterface = null;

    // Смещение для корректировки расположения элемента на сцене
    private int mElementOffsetX = 0;
    private int mElementOffsetY = 0;

    // Суммарное смещение в элементе (mOffset = mBaseOffset + mCenterOffset)
    private int mOffsetX = 0;
    private int mOffsetY = 0;

    // Смещение в элементе относительно центра
    private int mCenterOffsetX = 0;
    private int mCenterOffsetY = 0;

    // Смещение для прорисовки боковых граней (либо PAINT_OFFSET, либо WARNING_OFFSET)
    private int mBaseOffsetX = 0;
    private int mBaseOffsetY = 0;

    // Масштаб
    private float mScaleValue = 1;

    // Текущие индексы цветов элемента
    private ArrayList<Integer> mCurrentColors = null;

    private boolean mIsRepaintNeeded = false;
    private boolean mIsDataFromServer = false;

    // Значения параметров (высчитываются во время получения новых данных)
    // По строкам - параметры, по столбцам - биты для этих параметров
    private ArrayList<ArrayList<Boolean>> mParametersValues = null;
    private ArrayList<String> mParametersString = null;

    // Данные с сервера
    private byte[] mData;

    // Идентификационные данные элемента
    int mGroupID;
    int mElementID;

    RepaintListener mRepaintListener = null;
    TooltipListener mTooltipListener = null;






    public Item(Context context, int elementID, int groupID, ElementLogic elementLogic, ElementInterface elementInterface) {
        super(context);

        initialize(elementID, groupID, elementLogic, elementInterface);
    }


    private void initialize(int elementID, int groupID, ElementLogic elementLogic, ElementInterface elementInterface) {

        mElementID = elementID;
        mGroupID = groupID;
        setElementLogic(elementLogic);
        setElementInterface(elementInterface);
        calculateSize();
        setOnClickListener(mOnClickListener);
    }


//    public Item(Context context, AttributeSet attrs, int elementID, int groupID, ElementLogic elementLogic, ElementInterface elementInterface) {
//        super(context, attrs);
//
//        initialize(elementID, groupID, elementLogic, elementInterface);
//    }


//    public Item(Context context, AttributeSet attrs, int defStyle, int elementID, int groupID, ElementLogic elementLogic, ElementInterface elementInterface) {
//        super(context, attrs, defStyle);
//
//        initialize(elementID, groupID, elementLogic, elementInterface);
//    }

    public void setRepaintListener(RepaintListener repaintListener) {
        mRepaintListener = repaintListener;
    }

    public void setTooltipListener(TooltipListener tooltipListener) {
        mTooltipListener = tooltipListener;
    }


    public ElementInterface getElementInterface() {

        return mElementInterface;
    }


    public void setData(byte[] data) {



        // Установка нового состояния
        mData = data;

        mRepaintListener.parseAndRepaintNeeded(mGroupID, mElementID);
    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mIsDataFromServer && mTooltipListener != null) {
                mTooltipListener.displayData(mElementID, mGroupID, mParametersString);
            }
        }
    };

    public void setScaleValue(float scaleValue)
    {
        mScaleValue = scaleValue;
    }


    public void parseData() {

        mIsDataFromServer = true;

        mParametersValues.clear();
        mParametersString.clear();

        // Преобразование byte[] в ArrayList<Boolean>
        Vector<Boolean> state = new Vector<Boolean>(mData.length);
        for (byte currentByte : mData) {
            final int bitsInByte = 8;
            for (int j = 0; j < bitsInByte; j++) {
                int value = 1 << j;
                if ((currentByte & value) > 0)
                    state.add(true);
                else
                    state.add(false);
            }
        }

        // Количество зарегистрированных параметров в элементе
        ArrayList<ElementInterfaceParameter> mElementInterfaceParameters = mElementInterface.getParameters();
        ArrayList<Parameter> mElementLogicParameters = mElementLogic.getParameters();
        int parametersCount = mElementInterfaceParameters.size();

        List<Boolean> temp;
        for (int i = 0; i < parametersCount; i++)
        {
            if (mElementInterfaceParameters.get(i).isUsed()) {
                final int bitsInByte = 8;
                // Определение границ данных, относящихся к текущему параметру
                int startBit = (mElementInterfaceParameters.get(i).getStartByte() -
                        mElementInterface.getMinByte()) * bitsInByte +
                        mElementInterfaceParameters.get(i).getStartBit();
                // Состояние основного параметра
                // Копируем из массива набор битов, начиная с mainStartBit
                // в количестве parameter.bitsCount()
                temp = state.subList(startBit, startBit + mElementInterfaceParameters.get(i).getBitsCount());
            } else {
                temp = new ArrayList<Boolean>();
            }

            mParametersValues.add(new ArrayList<Boolean>(temp));

            if (mElementLogicParameters.get(i).getParameterType() == ParameterType.Common) {
                if (temp.size() == 0)
                    mParametersString.add("");
                else
                    mParametersString.add(String.valueOf(temp.get(0) ? 1 : 0));
            } else if (mElementLogicParameters.get(i).getParameterType() == ParameterType.Number) {
                mParametersString.add("0.00");
            } else {
                mParametersString.add("");
            }
        }
    }


    // Определение размеров элемента
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        calculateSize();
    }


    private void calculateSize() {
        boolean isCommon = mElementLogic.getElementLogicType() == ElementLogicType.Common;
        boolean isFuelDensity = mElementLogic.getElementLogicType() == ElementLogicType.FuelDensity;
        boolean isSingleIndicator = mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicator;
        boolean isDoubleIndicator = mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicator;
        boolean isSingleIndicatorDark = mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicatorDark;
        boolean isDoubleIndicatorDark = mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicatorDark;
        boolean isFlap = mElementLogic.getElementLogicType() == ElementLogicType.Flap;
        boolean isFlapDark = mElementLogic.getElementLogicType() == ElementLogicType.FlapDark;
        boolean isBus = mElementLogic.getElementLogicType() == ElementLogicType.Bus;
        boolean isBusDark = mElementLogic.getElementLogicType() == ElementLogicType.BusDark;

        // Использование базовых значений при расчете смещения
        mOffsetX = mBaseOffsetX;
        mOffsetY = mBaseOffsetY;
        mCenterOffsetX = 0;
        mCenterOffsetY = 0;

        int width = 0;
        int height = 0;

        // Расчет ограничивающей рамки
        if (isCommon || isFuelDensity) {
            width = (int)(mElementLogic.getSizeWidth() * mElementInterface.getSizeModificator() * mScaleValue + mBaseOffsetX * 2);
            height = (int)(mElementLogic.getSizeHeight() * mElementInterface.getSizeModificator() * mScaleValue + mBaseOffsetY * 2);
        } else if (isSingleIndicator || isSingleIndicatorDark || isDoubleIndicator || isDoubleIndicatorDark ||
                isFlap || isFlapDark) {
            width = (int) (mElementInterface.getSizeWidth() * mScaleValue + mBaseOffsetX * 2);
            height = (int) (mElementInterface.getSizeHeight() * mScaleValue + mBaseOffsetY * 2);
        } else if (isBus || isBusDark) {
            final double coeffWidth = 1;
            final double coeffHeight = 0.45;
            width = (int) ((mElementInterface.getSizeWidth() + mElementInterface.getSizeHeight() * (1 + coeffWidth * 2)) * mScaleValue + mBaseOffsetX * 2);
            height = (int) (mElementInterface.getSizeHeight() * (1 + coeffHeight * 2) * mScaleValue + mBaseOffsetY * 2);
        }

        if (mElementInterface.getAngle() != 0 || isFlap || isFlapDark) {
            // Максимальный размер элемента по X или Y
            int maxSideSize = width > height ? width : height;

            if (isFlap || isFlapDark) {
                int sign = 0;
                int pointNumber = Integer.parseInt(mElementInterface.getExtraParameters().get(0).getValue());
                if (pointNumber == 1)
                    sign = 1;
                else if (pointNumber == 2)
                    sign = -1;


                // Корректирующие значения смещения элемента на сцене (внешние координаты)
                mCenterOffsetX = (int) (sign * Math.cos(Math.toRadians(mElementInterface.getAngle())) * width / 2);
                mCenterOffsetY = (int) (sign * Math.sin(Math.toRadians(mElementInterface.getAngle())) * width / 2);
                mElementOffsetX = ((maxSideSize * 2 - width) / 2) + mCenterOffsetX;
                mElementOffsetY = ((maxSideSize * 2 - height) / 2) + mCenterOffsetY;
                // Расчетные размеры
                width = maxSideSize * 2;
                height = maxSideSize * 2;
            } else {
                // Корректирующие значения смещения элемента на сцене (внешние координаты)
                mElementOffsetX = (maxSideSize - width) / 2 ;
                mElementOffsetY = (maxSideSize - height) / 2;
                // Расчетные размеры
                width = maxSideSize;
                height = maxSideSize;
            }

            // Смещение внутренних координат
            mOffsetX += mElementOffsetX;
            mOffsetY += mElementOffsetY;
        }

        setMeasuredDimension(width, height);
    }


    protected void onDraw(Canvas canvas) {


        canvas.save();

        if (mElementInterface.getAngle() != 0) {
            if (mElementLogic.getElementLogicType() == ElementLogicType.Flap || mElementLogic.getElementLogicType() == ElementLogicType.FlapDark) {

                canvas.rotate(mElementInterface.getAngle(), getWidth() / 2 + mCenterOffsetX, getHeight() / 2 + mCenterOffsetY);
            } else {
                canvas.rotate(mElementInterface.getAngle(), getWidth() / 2, getHeight() / 2);
            }
        }



        if (mElementLogic.getElementLogicType() == ElementLogicType.Common ||
                mElementLogic.getElementLogicType() == ElementLogicType.FuelDensity) {
            drawStandartElement(canvas);
        } else if (mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicator ||
                mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicatorDark) {
            drawSingleIndicator(canvas);
        } else if (mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicator ||
                mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicatorDark) {
            drawDoubleIndicator(canvas);
        } else if (mElementLogic.getElementLogicType() == ElementLogicType.Flap ||
                mElementLogic.getElementLogicType() == ElementLogicType.FlapDark) {
            drawFlap(canvas);
        } else if (mElementLogic.getElementLogicType() == ElementLogicType.Bus ||
                mElementLogic.getElementLogicType() == ElementLogicType.BusDark) {
            drawBus(canvas);
        }

        canvas.restore();

        if (mTooltipListener.IsDialogShowing() && mTooltipListener.getGroupID() == mGroupID && mTooltipListener.getElementID() == mElementID)
            mTooltipListener.displayData(mElementID, mGroupID, mParametersString);
    }








    private void drawSingleIndicator(Canvas canvas) {

        // Набор точек для прорисовки фигуры
        ArrayList<Integer> points = new ArrayList<Integer>();
        points.add(0);
        points.add(0);
        points.add(mElementInterface.getSizeWidth());
        points.add(mElementInterface.getSizeHeight());

        // Список всех фигур
        ArrayList<Figure> figures = mElementLogic.getFigures();
        // Рамка
        figures.get(0).setPoints(points);
        // Текст
        figures.get(1).setPoints(points);

        // Объект рисования
        // Карандаш
        Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.TRANSPARENT);
        pen.setStrokeWidth(outerLine);
        // Кисть
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        brush.setStyle(Paint.Style.FILL);
        brush.setColor(Color.TRANSPARENT);
        // Текст
        String drawingText = null;

        // Дополнительные параметры элемента - текст (0), цвет1 (1) и цвет2 (2)
        final ArrayList<ElementInterfaceExtraParameter> interfaceExtraParameter = mElementInterface.getExtraParameters();

        // Секция прорисовки при получении данных с сервера
        if (mIsDataFromServer) {
            // Параметр единственный, т.ч. получаем его без проверки индексов
            if (mParametersValues.get(0).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(2).getValue()));
            } else {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(1).getValue()));
            }
        }
        // Прорисовка по умолчанию
        else {
            if (mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicator)
                brush.setColor(Color.WHITE);
            else if (mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicatorDark)
                brush.setColor(Color.BLACK);
        }

        // Установка цвета карандаша (контура)
        if (mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicator)
            pen.setColor(Color.BLACK);
        else if (mElementLogic.getElementLogicType() == ElementLogicType.SingleIndicatorDark)
            pen.setColor(Color.WHITE);

        // Прорисовка индикатора
        drawFigure(canvas, figures.get(0), pen, brush, drawingText);

        // Выбор цвета текста в зависимости от яркости фонового цвета
        int brushColor = brush.getColor();
        if ((Color.red(brushColor) + Color.blue(brushColor) + Color.green(brushColor)) < 96)
            pen.setColor(Color.WHITE);
        else
            pen.setColor(Color.BLACK);

        // Текст индикатора
        drawingText = interfaceExtraParameter.get(0).getValue();

        // Вывод текста
        drawFigure(canvas, figures.get(1), pen, brush, drawingText);
    }



    private void drawDoubleIndicator(Canvas canvas) {

        // Набор точек для прорисовки фигуры
        ArrayList<Integer> points = new ArrayList<Integer>();
        points.add(0);
        points.add(0);
        points.add(mElementInterface.getSizeWidth());
        points.add(mElementInterface.getSizeHeight());

        // Список всех фигур
        ArrayList<Figure> figures = mElementLogic.getFigures();
        // Рамка
        figures.get(0).setPoints(points);
        // Текст
        figures.get(1).setPoints(points);

        // Объект рисования
        // Карандаш
        Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.TRANSPARENT);
        pen.setStrokeWidth(outerLine);
        // Кисть
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        brush.setStyle(Paint.Style.FILL);
        brush.setColor(Color.TRANSPARENT);
        // Текст
        String drawingText = null;

        // Дополнительные параметры элемента - текст (0), цвет00 (1), цвет01 (2), цвет10 (3), цвет11 (4)
        final ArrayList<ElementInterfaceExtraParameter> interfaceExtraParameter = mElementInterface.getExtraParameters();

        // Секция прорисовки при получении данных с сервера
        if (mIsDataFromServer) {
            // Параметр единственный, т.ч. получаем его без проверки индексов
            if (!mParametersValues.get(0).get(0) && !mParametersValues.get(1).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(1).getValue()));
            } else if (!mParametersValues.get(0).get(0) && mParametersValues.get(1).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(2).getValue()));
            } else if (mParametersValues.get(0).get(0) && !mParametersValues.get(1).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(3).getValue()));
            } else if (mParametersValues.get(0).get(0) && mParametersValues.get(1).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(4).getValue()));
            }
        }
        // Прорисовка по умолчанию
        else {
            if (mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicator)
                brush.setColor(Color.WHITE);
            else if (mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicatorDark)
                brush.setColor(Color.BLACK);
        }

        // Установка цвета карандаша (контура)
        if (mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicator)
            pen.setColor(Color.BLACK);
        else if (mElementLogic.getElementLogicType() == ElementLogicType.DoubleIndicatorDark)
            pen.setColor(Color.WHITE);

        // Прорисовка индикатора
        drawFigure(canvas, figures.get(0), pen, brush, drawingText);

        // Выбор цвета текста в зависимости от яркости фонового цвета
        int brushColor = brush.getColor();
        if ((Color.red(brushColor) + Color.blue(brushColor) + Color.green(brushColor)) < 96)
            pen.setColor(Color.WHITE);
        else
            pen.setColor(Color.BLACK);

        // Текст индикатора
        drawingText = interfaceExtraParameter.get(0).getValue();

        // Вывод текста
        drawFigure(canvas, figures.get(1), pen, brush, drawingText);
    }



    // Прорисовка створки
    private void drawFlap(Canvas canvas) {

        // Набор точек для прорисовки фигуры
        ArrayList<Integer> points = new ArrayList<Integer>();
        points.add(0);
        points.add(0);
        points.add(mElementInterface.getSizeWidth());
        points.add(mElementInterface.getSizeHeight());

        // Список всех фигур
        Figure figure = mElementLogic.getFigures().get(0);
        figure.setPoints(points);

        // Объект рисования
        // Карандаш
        Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.TRANSPARENT);
        pen.setStrokeWidth(outerLine);
        // Кисть
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        brush.setStyle(Paint.Style.FILL);
        brush.setColor(Color.TRANSPARENT);
        // Текст
        String drawingText = null;

        // Дополнительные параметры элемента - текст (0), цвет1 (1) и цвет2 (2)
        final ArrayList<ElementInterfaceExtraParameter> interfaceExtraParameter = mElementInterface.getExtraParameters();

        // Секция прорисовки при получении данных с сервера
        if (mIsDataFromServer) {
            // Параметр единственный, т.ч. получаем его без проверки индексов
            if (mParametersValues.get(0).get(0)) {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(3).getValue()));
            } else {
                brush.setColor(Color.parseColor(interfaceExtraParameter.get(2).getValue()));
            }
        }
        // Прорисовка по умолчанию
        else {
            if (mElementLogic.getElementLogicType() == ElementLogicType.Flap)
                brush.setColor(Color.WHITE);
            else if (mElementLogic.getElementLogicType() == ElementLogicType.FlapDark)
                brush.setColor(Color.BLACK);
        }

        // Установка цвета карандаша (контура)
        if (mElementLogic.getElementLogicType() == ElementLogicType.Flap)
            pen.setColor(Color.BLACK);
        else if (mElementLogic.getElementLogicType() == ElementLogicType.FlapDark)
            pen.setColor(Color.WHITE);

        int rotatePoint = Integer.valueOf(interfaceExtraParameter.get(0).getValue());
        int angle = Integer.valueOf(interfaceExtraParameter.get(1).getValue());
        int translateX = 0;
        int translateY = 0;

        if (mIsDataFromServer && mParametersValues.get(0).get(0)) {
            // Точка вращения с левой стороны фигуры
            if (rotatePoint == 1) {
                translateX = mOffsetX;
                translateY = mOffsetY + mElementInterface.getSizeHeight() / 2;
            } else if (rotatePoint == 2) {
                translateX = mOffsetX + mElementInterface.getSizeWidth();
                translateY = mOffsetY + mElementInterface.getSizeHeight() / 2;
            }

            canvas.rotate(-angle, translateX, translateY);
        }


        // Прорисовка створки
        drawFigure(canvas, figure, pen, brush, drawingText);
    }



    // Прорисовка шины
    private void drawBus(Canvas canvas) {

        // Коэффициенты размеров стрелок
        final double coeffWidth = 1;
        final double coeffHeight = 1;

        // Вычисление координат фигур
        int totalWidth = mElementInterface.getSizeWidth();
        int totalHeight = mElementInterface.getSizeHeight();

        // Основание стрелки
        Figure rectFigure = new Figure(FigureType.Rect);
        // Стрелки
        Figure arrow1 = new Figure(FigureType.Polygon);
        Figure arrow2 = new Figure(FigureType.Polygon);
        // Линии контура
        Figure line1 = new Figure(FigureType.Line);
        Figure line2 = new Figure(FigureType.Line);

        ArrayList<Integer> points;
        // Координаты основания
        points = new ArrayList<Integer>();
        points.add((int) (totalHeight * coeffWidth));
        points.add((int) (totalHeight * coeffHeight));
        points.add(mElementInterface.getSizeWidth());
        points.add(mElementInterface.getSizeHeight());
        rectFigure.setPoints(points);

        // Координаты стрелки №1
        points = new ArrayList<Integer>();
        points.add(0);
        points.add((int) (totalHeight / 2 + totalHeight * coeffHeight));
        points.add((int) (totalHeight * coeffWidth * 1.5));
        points.add(0);
        points.add((int) (totalHeight * coeffWidth * 1.5));
        points.add((int) (totalHeight + totalHeight * coeffHeight * 2));
        arrow1.setPoints(points);

        // Координаты стрелки №2
        points = new ArrayList<Integer>();
        points.add((int) (totalWidth + totalHeight * coeffWidth * 2));
        points.add((int) (totalHeight / 2 + totalHeight * coeffHeight));
        points.add((int) (totalWidth + totalHeight * coeffWidth * 2 - totalHeight * coeffWidth * 1.5));
        points.add(0);
        points.add((int) (totalWidth + totalHeight * coeffWidth * 2 - totalHeight * coeffWidth * 1.5));
        points.add((int) (totalHeight + totalHeight * coeffHeight * 2));
        arrow2.setPoints(points);

        // Координаты линии №1
        points = new ArrayList<Integer>();
        points.add((int) (totalHeight * coeffWidth * 1.5));
        points.add((int) (totalHeight * coeffHeight));
        points.add((int) (totalWidth + totalHeight * coeffWidth / 2));
        points.add((int) (totalHeight * coeffHeight));
        line1.setPoints(points);

        // Координаты линии №2
        points = new ArrayList<Integer>();
        points.add((int) (totalHeight * coeffWidth * 1.5));
        points.add((int) (totalHeight + totalHeight * coeffHeight));
        points.add((int) (totalWidth + totalHeight * coeffWidth / 2));
        points.add((int) (totalHeight + totalHeight * coeffHeight));
        line2.setPoints(points);

        // Объект рисования
        // Карандаш
        Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        pen.setStyle(Paint.Style.STROKE);
        pen.setColor(Color.TRANSPARENT);
        pen.setStrokeWidth(outerLine);
        // Модифицированный карандаш
        Paint modifiedPen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        modifiedPen.setStyle(Paint.Style.STROKE);
        modifiedPen.setColor(Color.TRANSPARENT);
        // Кисть
        Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        brush.setStyle(Paint.Style.FILL);
        brush.setColor(Color.TRANSPARENT);
        // Текст
        String drawingText = null;

        // Секция прорисовки при получении данных с сервера
        if (mIsDataFromServer) {
            // Параметр единственный, т.ч. получаем его без проверки индексов
            if (mParametersValues.get(0).get(0)) {
                brush.setColor(mElementLogic.getRecords().get(1).getGDIObjects().get(0).getColor(0));
            } else {
                brush.setColor(mElementLogic.getRecords().get(0).getGDIObjects().get(0).getColor(0));
            }
        }
        // Прорисовка по умолчанию
        else {
            if (mElementLogic.getElementLogicType() == ElementLogicType.Bus)
                brush.setColor(Color.WHITE);
            else if (mElementLogic.getElementLogicType() == ElementLogicType.BusDark)
                brush.setColor(Color.BLACK);
        }

        // Установка цвета и ситиля карандаша (контура)
        pen.setColor(mElementLogic.getDefaultRecords().get(0).getGDIObjects().get(0).getColor(0));
        pen.setStrokeWidth(outerLine);

        // Прорисовка фигур
        drawFigure(canvas, arrow1, pen, brush, drawingText);
        drawFigure(canvas, arrow2, pen, brush, drawingText);
        drawFigure(canvas, rectFigure, modifiedPen, brush, drawingText);
        drawFigure(canvas, line1, pen, brush, drawingText);
        drawFigure(canvas, line2, pen, brush, drawingText);
    }


    void drawFigure(Canvas canvas, Figure figure, Paint pen, Paint brush, String text) {

        // Массив точек, параметры фигуры
        ArrayList<Integer> points = figure.getPoints();

        if (points.size() == 0)
            return;

        // Установка объектов рисования
        RectF rect = new RectF(
                (int)(points.get(0) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetX),
                (int)(points.get(1) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetY),
                (int)((points.get(0) + points.get(2)) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetX),
                (int)((points.get(1) + points.get(3)) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetY));


        // Прорисовка фигур
        switch (figure.getFigureType()) {

            case Ellipse:
                canvas.drawOval(rect, brush);
                canvas.drawOval(rect, pen);
                break;

            case Rect:
                canvas.drawRect(rect, brush);
                canvas.drawRect(rect, pen);
                break;

            case RoundedRect:
                float xRadius = (float) (15 * points.get(2) * mElementInterface.getSizeModificator() / 560);
                float yRadius = (float) (12 * points.get(3) * mElementInterface.getSizeModificator() / 122);
                canvas.drawRoundRect(rect, xRadius, yRadius, brush);
                canvas.drawRoundRect(rect, xRadius, yRadius, pen);
                break;

            case Chord:
                break;

            case Line:
                //			canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, brush);
                canvas.drawLine((int)(points.get(0) * mElementInterface.getSizeModificator() * mScaleValue) + mOffsetX,
                        (int)(points.get(1) * mElementInterface.getSizeModificator() * mScaleValue) + mOffsetY,
                        (int)(points.get(2) * mElementInterface.getSizeModificator() * mScaleValue) + mOffsetX,
                        (int)(points.get(3) * mElementInterface.getSizeModificator() * mScaleValue) + mOffsetY,
                        pen);
                break;

            case Pie:
                break;

            case Arc:
                canvas.drawArc(rect, 180 + points.get(4), points.get(5), false, pen);
                break;

            case Polygon:
                Path path = new Path();
                for (int i = 0; i < points.size(); i+=2) {
                    if (i == 0) {
                        path.moveTo((float)(points.get(i) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetX),
                                (float)(points.get(i + 1) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetY));
                    } else {
                        path.lineTo((float)(points.get(i) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetX),
                                (float)(points.get(i + 1) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetY));
                    }

                }
                path.lineTo((float)(points.get(0) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetX + 1),
                        (float)(points.get(1) * mElementInterface.getSizeModificator() * mScaleValue + mOffsetY + 1));


                canvas.drawPath(path, brush);
                canvas.drawPath(path, pen);
                break;

            case Text:
                if (text != null) {

                    Paint font = new Paint(pen);
                    font.setStyle(Paint.Style.FILL);
                    Typeface tf = Typeface.create("Arial", 100);
                    font.setTypeface(tf);
                    font.setTextSize(font.getTextSize() * 2 * mScaleValue);
                    Rect textBoundingRect = new Rect();
                    font.getTextBounds(text, 0, text.length(), textBoundingRect);



                    font.setTextAlign(Align.CENTER);
                    canvas.drawText(text, rect.left + rect.width() / 2, rect.top + rect.height() / 2 + textBoundingRect.height() / 2, font);

                    //				pen.setColor(Color.BLUE);
                    //				canvas.drawRect(rect, pen);
                    //				//
                    //				pen.setColor(Color.MAGENTA);
                    //				canvas.drawRect(rect.left + rect.width() / 2 - textBoundingRect.width() / 2,
                    //						rect.top + rect.height() / 2 - textBoundingRect.height() / 2,
                    //						rect.left + rect.width() / 2 + textBoundingRect.width() / 2,
                    //						rect.top + rect.height() / 2 + textBoundingRect.height() / 2,
                    //						pen);
                    //				canvas.drawLine(0, rect.height(), rect.width(), rect.height(), pen);



                }
                break;

            case Undefined:
                break;
        }
    }








    void drawStandartElement(Canvas canvas) {

        // Флаг конфликта устанавливается в истину при каждом вызове функции
        boolean isConflict = false;

        // Количество зарегистрированных фигур в элементе
        final ArrayList<Figure> figures = mElementLogic.getFigures();
        // Количество зарегистрированных параметров в элементе
        final ArrayList<Parameter> parameters = mElementLogic.getParameters();
        final ArrayList<ElementInterfaceParameter> interfaceParameter = mElementInterface.getParameters();
        final ArrayList<ElementInterfaceExtraParameter> interfaceExtraParameter = mElementInterface.getExtraParameters();
        // Количество записей, зарегистрированных для элемента
        final ArrayList<Record> records = mElementLogic.getRecords();
        // Количество записей по умолчанию
        final ArrayList<Record> defaultRecords = mElementLogic.getDefaultRecords();

        boolean isRepaintNedeed = false;

        // Перебор фигур с учетом Z-индекса
        for (int i = 0; i < figures.size(); i++) {

            // Объект рисования
            // Карандаш
            Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            pen.setStyle(Paint.Style.STROKE);
            pen.setColor(Color.TRANSPARENT);
            // Кисть
            Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            brush.setStyle(Paint.Style.FILL);
            brush.setColor(Color.TRANSPARENT);
            // Отображаемый текст
            String drawingText = null;

            // Новый угол для поворота фигуры
            int newAngle = 0;
            int correctionAngle = 0;

            // Фигура для перерисовки
            Figure figure = figures.get(i);

            // Каждый бит - фигура. Если она прорисовывается дважды - возникает конфликт
            final int gdiObjectsCount = 3;
            ArrayList<Boolean> conflictFlags = new ArrayList<Boolean>(gdiObjectsCount);
            for (int j = 0; j < gdiObjectsCount; j++)
                conflictFlags.add(false);

            // Работа с параметрами только в том случае, если данные получены с сервера
            if (mIsDataFromServer) {

                // Перебор массива бит (и параметров)
                for (int j = 0; j < parameters.size(); j++) {

                    if (!interfaceParameter.get(j).isUsed())
                        continue;

                    Parameter parameter = parameters.get(j);

                    for (Record record : records) {

                        // Поиск необходимой записи
                        // Запись описывает нужную фигуру...
                        boolean exp1 = i == record.getFigureID();
                        // ... нужный параметр...
                        boolean exp2 = j == record.getParameterID();
                        // ... и в нужном состоянии
                        boolean exp3 = mParametersValues.get(j).equals(record.getState());
                        // Дополнительное условие - параметр текстовый
                        boolean exp4 = parameter.getParameterType() == ParameterType.Number;
                        // Продолжаем, если запись подходит
                        if (!((exp1 && exp2) && (exp3 || exp4)))
                            continue;

                        newAngle = record.getAngle();

                        // Получаем массив элементов GDI
                        ArrayList<GDIObject> gdiObjects = record.getGDIObjects();
                        // Перебор объектов GDI
                        for (GDIObject gdiObject : gdiObjects) {

                            if (gdiObject.isBlinked())
                                isRepaintNedeed = true;

                            // Условие проверки на конфликтные биты
                            if (conflictFlags.get(gdiObject.getGDIType().getType()))
                                isConflict = true;
                            conflictFlags.set(gdiObject.getGDIType().getType(), true);

                            // В соответствии с типом объекта, инициализируем соответствующий объект для рисования
                            switch (gdiObject.getGDIType()) {

                                case Pen:
                                    // Установка цвета карандаша
                                    pen.setColor(gdiObject.getColor(mCurrentColors.get(i)));
                                    // Установка толщины карандаша
                                    pen.setStrokeWidth((float) (gdiObject.getPenWidth() * mElementInterface.getSizeModificator() * mScaleValue));
                                    // Установка стиля карандаша
                                    // pen.setStyle(); TODO: Добавить обработчик стиля карандаша
                                    break;

                                case Brush:
                                    // Установка цвета кисти
                                    brush.setColor(gdiObject.getColor(mCurrentColors.get(i)));
                                    break;

                                case Text:
                                    // Установка отображаемого текста для числовоего параметра
                                    if (parameter.getParameterType() == ParameterType.Number) {

                                        final int coeffID = parameter.getExtraParameter() * 4;
                                        final int postfixID = parameter.getExtraParameter() * 4 + 1;
                                        final int precisionID = parameter.getExtraParameter() * 4 + 2;
                                        final int signID = parameter.getExtraParameter() * 4 + 3;

                                        // Константы точности вывода числа
                                        final int precisionMin = 0;
                                        final int precisionMax = 6;
                                        final int precisionDefault = 2;

                                        // Дополнительные параметры для элемента Number
                                        double coeff = Double.parseDouble(interfaceExtraParameter.get(coeffID).getValue());
                                        String postfix = interfaceExtraParameter.get(postfixID).getValue();
                                        boolean isSigned = Boolean.parseBoolean(interfaceExtraParameter.get(signID).getValue());
                                        int precision = Integer.getInteger(interfaceExtraParameter.get(precisionID).getValue(), precisionDefault);

                                        // Проверка значения точности числа
                                        if (precision < precisionMin || precision > precisionMax)
                                            precision = precisionDefault;

                                        // Обработка постфикса
                                        if (!postfix.isEmpty())
                                            postfix = " " + postfix;
                                        double number = parseNumber(mParametersValues.get(j), coeff, isSigned);

                                        // Расчет плотности
                                        if (mElementLogic.getElementLogicType() == ElementLogicType.FuelDensity) {
                                            final int densityID = interfaceExtraParameter.size() - 1;
                                            double density = Double.parseDouble(interfaceExtraParameter.get(densityID).getValue());
                                            number = calculateFuelDensity(density, number);
                                        }

                                        // Установка точности вывода значения
                                        mParametersString.set(j, String.format("%." + precision + "f", number));

                                        // Сохранение полученного числа
                                        drawingText = mParametersString.get(j)  + postfix;
//                                        Log.e("DrawingText!", "j=" + j + ", text=" + drawingText + "; " + mParametersValues.get(j).toString());

                                        // Обработка неиспользуемого связанного параметра
                                        if (!interfaceParameter.get(parameter.getExtraParameter()).isUsed()) {
                                            for (Record defaultRecord : defaultRecords) {
                                                if (defaultRecord.getFigureID() != i)
                                                    continue;
                                                final int defaultGDIObject = 1;
                                                final int defaultColorNumber = 0;
                                                pen.setColor(defaultRecord.getGDIObjects().get(defaultGDIObject).getColor(defaultColorNumber));
                                            }
                                        }
                                    } else {

                                        drawingText = gdiObject.getText();
                                    }

                                    // Настройка шрифта в соответствии с его типом
                                    // Заглушка
                                    break;

                                case Undefined:
                                    break;
                            }
                        }
                    }
                }
            }

            // Проверка, применен ли хоть один параметр GDI к фигуре
            boolean defaultPaint = true;
            for (int j = 0; j < gdiObjectsCount; j++)
            {
                if (conflictFlags.get(j))
                    defaultPaint = false;
            }
            if (defaultPaint) {

                for (Record record : defaultRecords) {

                    // Поиск необходимой записи. Запись описывает нужную фигуру...
                    if (i != record.getFigureID())
                        continue;

                    // Получаем массив элементов GDI
                    ArrayList<GDIObject> gdiObjects = record.getGDIObjects();
                    // Перебор объектов GDI
                    for (GDIObject gdiObject : gdiObjects) {

                        if (gdiObject.isBlinked())
                            isRepaintNedeed = true;

                        // В соответствии с типом объекта, инициализируем соответствующий объект для рисования
                        GDIObject object;
                        switch (gdiObject.getGDIType()) {

                            case Pen:
                                // Установка цвета карандаша
                                int currentColorIndex1 = mCurrentColors.get(i);
                                object = gdiObject;
                                int color1 = object.getColor(currentColorIndex1);
                                pen.setColor(color1);
                                // Установка толщины карандаша
                                pen.setStrokeWidth((float) (gdiObject.getPenWidth() * mElementInterface.getSizeModificator() * mScaleValue));
                                // Установка стиля карандаша
                                // pen.setStyle(); TODO: Добавить обработчик стиля карандаша
                                break;

                            case Brush:
                                // Установка цвета кисти
                                int currentColorIndex = mCurrentColors.get(i);
                                object = gdiObject;
                                int color = object.getColor(currentColorIndex);
                                brush.setColor(color);
                                break;

                            case Text:
                                // Настройка шрифта и текста
                                drawingText = gdiObject.getText();
                                // Заглушка
                                break;

                            case Undefined:
                                break;
                        }
                    }
                }
            }

            final int degreesInCircle = 360;
            if (figure.isStatic())
                correctionAngle = degreesInCircle - mElementInterface.getAngle();
            else
                correctionAngle = 0;

            int resultAngle = (newAngle < 0 ? 0 : newAngle) + correctionAngle;
            resultAngle  = resultAngle % degreesInCircle;
            if (resultAngle >= 0) {
                canvas.save();
                int boundingWidth = getWidth() / 2;
                int boundingHeight = getHeight() / 2;
                canvas.rotate(resultAngle, boundingWidth, boundingHeight);
            }

            drawFigure(canvas, figure, pen, brush, drawingText);

            if (resultAngle >= 0) {
                canvas.restore();
            }
        }

        if (isConflict) {

            // Прямоугольник с размерами области предупреждения
            // X, Y - отступ от нулевой координаты за вычетом размера области предупреждения
            // Width, Height - основной размер * модификатор + двойной отступ при предупреждении
            final RectF roundedRect = new RectF(
                    (float)(0),
                    (float)(0),
                    (float)(this.getWidth()),
                    (float)(this.getHeight()));

            final float xRadius = 5;
            final float yRadius = xRadius * roundedRect.width() / roundedRect.height();

            Paint paint = new Paint(); // Заглушка
            RadialGradient gradient = new RadialGradient(getWidth() / 2, getHeight() / 2, getWidth() / 2, Color.argb(160, 255, 127, 39), Color.argb(32, 255,
                    127, 39), TileMode.CLAMP);




            paint.setShader(gradient);
            canvas.drawRoundRect(roundedRect, xRadius, yRadius, paint);

        }

        if (mIsRepaintNeeded != isRepaintNedeed) {
            mIsRepaintNeeded = isRepaintNedeed;
            mRepaintListener.repaintFlagChanged(mGroupID, mElementID, mIsRepaintNeeded);
        }
    }


    // Установка логики для объекта
    private void setElementLogic(ElementLogic elementLogic) {

        mElementLogic = elementLogic;

        int currectColorSize = mElementLogic.getFigures().size();
        mCurrentColors = new ArrayList<Integer>(currectColorSize);
        for (int i = 0; i < currectColorSize; i++)
            mCurrentColors.add(0);

        mParametersValues = new ArrayList<ArrayList<Boolean>>(mElementLogic.getParameters().size());
        mParametersString = new ArrayList<String>(mElementLogic.getParameters().size());

        // Установка зоны предупреждения по X (mPaintOffset - минимум)
        if (mElementLogic.getWarningSizeWidth() < PAINT_OFFSET)
            mBaseOffsetX = PAINT_OFFSET;
        else
            mBaseOffsetX = mElementLogic.getWarningSizeWidth();

        // Установка зоны предупреждения по Y (mPaintOffset - минимум)
        if (mElementLogic.getWarningSizeWidth() < PAINT_OFFSET)
            mBaseOffsetY = PAINT_OFFSET;
        else
            mBaseOffsetY = mElementLogic.getWarningSizeHeight();
    }


    // Установка интерфейса для объекта
    private void setElementInterface(ElementInterface elementInterface) {

        mElementInterface = elementInterface;
    }








    public void changeColor() {

        final int totalColorsCount = 2;
        for (int i = 0; i < mCurrentColors.size(); i++)
            mCurrentColors.set(i, (mCurrentColors.get(i) + 1) % totalColorsCount);
    }









    // Функция расчета плотности топлива [ГОСТ 3900-85 (СЭВ 6754-89)]
    private double calculateFuelDensity(double baseDensity, double temperature) {
        // Данные таблицы
        final double arrayKey[] = {0.7, 0.71, 0.72, 0.73, 0.74, 0.75, 0.76, 0.77,
                0.78, 0.79, 0.80, 0.81, 0.82, 0.83, 0.84, 0.85};
        final double arrayValue[] = {0.000844, 0.000829, 0.000813, 0.000798, 0.000783,
                0.000769, 0.000756, 0.000743, 0.000731, 0.000718,
                0.000704, 0.000693, 0.000681, 0.000672, 0.000664,
                0.000658};
        // Размерность
        final int arraySize = 16;

        // Выполнялся ли расчет temperatureFix
        boolean isCalculate = false;
        // Старое значение плотности (на случай изменения параметров элемента)
        double oldBaseDensity = 0;
        // Температурная поправка
        double temperatureFix = 0;

        // Выполнялся ли расчет температурной поправки со значением плотности baseDensity
        if (Math.abs(oldBaseDensity - baseDensity) > 1e-6)
            isCalculate = false;

        if (!isCalculate)
        {
            isCalculate = true;
            oldBaseDensity = baseDensity;

            int key1 = -1;
            int key2 = -1;
            double roundedBaseDensity = Math.round(baseDensity * 100.0) / 100.0;

            // Поиск границ, в которых лежит базовая плотность (первая граница)
            for (int i = 0; i < arraySize; i++)
            {
                if (Math.abs(arrayKey[i] - roundedBaseDensity) < 1e-6)
                {
                    key1 = i;
                    break;
                }
            }

            // Вторая граница
            if (roundedBaseDensity < baseDensity)
                key2 = key1 + 1;
            else
                key2 = key1 - 1;

            // Если осуществлен выход за пределы заданных данных - возвращаем нуль
            if (key1 < 0 || key1 >= arraySize)
                return 0;
            if (key2 < 0 || key2 >= arraySize)
                return 0;

            // Расчет шага для температурной поправки
            double step = arrayValue[key1] - arrayValue[key2];
            // Множитель 100 - магическое число
            temperatureFix = arrayValue[key1] + step * (roundedBaseDensity - baseDensity) * 100;
        }

        final int baseTemperature = 20;
        double realDensity = baseDensity + temperatureFix * (baseTemperature - temperature);

        return realDensity;
    }


    private double parseNumber(ArrayList<Boolean> numberBits, double coeff,
                               boolean isSigned) {

        int number = 0;
        int maxNumber = 0;

        // Защита от дурака
        if (numberBits.size() == 0)
            return number;

        if (isSigned) {

            boolean signBit = numberBits.get(numberBits.size() - 1);
            for (int i = 0; i < numberBits.size() - 1; i++) {
                // Считаем единицы, если число положительное
                if (numberBits.get(i) && !signBit)
                    number += (1 << i);
                    // Считаем нули, если число отрицательное (после инверсии - единицы)
                else if (!numberBits.get(i) && signBit)
                    number += (1 << i);
            }

            if (signBit)
            {
                number++;
                number = -number;
            }

            maxNumber = 1 << (numberBits.size() - 2);
        } else {

            for (int i = 0; i < numberBits.size(); i++)
            {
                if (numberBits.get(i))
                    number += (1 << i);
            }

            maxNumber = 1;
        }

        return coeff / maxNumber * number;
    }


    public void setPosition(int x, int y) {
        mMarginLeft = x;
        mMarginTop = y;

    }

    public int getXPosition() {
        return mMarginLeft - mElementOffsetX;
    }

    public int getYPosition() {
        return mMarginTop  - mElementOffsetY;
    }


    public void resetState() {
        mIsDataFromServer = false;
    }
}