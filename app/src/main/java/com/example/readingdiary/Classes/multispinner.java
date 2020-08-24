package com.example.readingdiary.Classes;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class multispinner extends Spinner implements
        OnMultiChoiceClickListener, OnCancelListener
{
    private List<String> items; // Элементы
    private boolean[] checked; // Выбрано\нет
    multispinner.multispinnerListener listener;
    private int checkType=0;
    // Объявление
    public multispinner(Context context)
    {
        super(context);
//        listener = (multispinnerListener) context;
    }

    public void setType(int checkType){
        this.checkType = checkType;
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

        public multispinner(Context arg0, AttributeSet arg1)
    {
        super(arg0, arg1);
//        listener = (multispinnerListener) arg0;
    }

    public multispinner(Context arg0, AttributeSet arg1, int arg2)
    {
        super(arg0, arg1, arg2);
//        listener = (multispinnerListener) arg0;
    }

    // Клик по элементу
    @Override
    public void onClick(DialogInterface dialog, int ans, boolean isChecked)
    {
        if (isChecked)
            checked[ans] = true;
        else
            checked[ans] = false;
    }





    // Закрытие спинера
//    @Override
//    public void onCancel(DialogInterface dialog)
//    {
//        ArrayList<String> checkedItems = new ArrayList<>()
//
////        String str="Selected values are: ";
//
//        for (int i = 0; i < items.size(); i++)
//        {
//            if (checked[i] == true)
//            {
//                checkedItems.add(items.get(i));
////                str=str+"   "+listitems.get(i);
//            }
//
//        }
//
////        AlertDialog.Builder alert1 = new AlertDialog.Builder(getContext());
////
////        alert1.setTitle("Items:");
////
////        alert1.setMessage(str);
////
////        alert1.setPositiveButton("Ok", null);
////
////        alert1.show();
//    }


    // Открытие спиннера
    @Override
    public boolean performClick()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final boolean[] oldCheck = checked.clone();
        builder.setMultiChoiceItems(
                items.toArray(new CharSequence[items.size()]), checked, this);
        builder.setPositiveButton("ОК",
                new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        acceptCheck();
                    }
                })
                .setNeutralButton("Очистить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearChecked();
//                        dialog.cancel()
//                        dialog.clear();

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelCheck(oldCheck);
//                        dialog.cancel()
                    }
                });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void acceptCheck(){
        listener.onItemsChecked(checked, checkType);
    }
    public void clearChecked(){
        for (int i = 0; i < checked.length; i++){
            checked[i] = true;
        }
        listener.onItemsChecked(checked, checkType);
    }

    public void cancelCheck(boolean[] oldChecked){
        checked = oldChecked.clone();
        listener.onItemsChecked(checked, checkType);
    }

    // Задание элементов
    public void setItems(List<String> items, boolean[] checked, String allText,
                         multispinnerListener listener)
    {
        this.items = items;
        this.listener = listener;

        this.checked = checked;
//        for (int i = 0; i < checked.length; i++)
//            checked[i] =true;


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[] { allText });
        setAdapter(adapter);
    }

    public interface multispinnerListener
    {
        public void onItemsChecked(boolean[] checked, int checkType);
    }

}

