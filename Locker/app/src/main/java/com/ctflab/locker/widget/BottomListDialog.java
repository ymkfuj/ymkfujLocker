package com.ctflab.locker.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.ctflab.locker.R;

/**
 * Created by wuwei on 2016/1/6.
 */
public class BottomListDialog extends PopupWindow {

    private View mMenuView;
    private Button btnCancel;
    private LinearLayout lstDialogItems;
    private LayoutInflater mInflater;

    public BottomListDialog(Activity context) {
        super(context);
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void initDialog(String[] list, int defaultIndex, OnClickListener itemsOnClick) {
        mMenuView = mInflater.inflate(R.layout.dialog_list, null);
        //取消按钮
        btnCancel = (Button) mMenuView.findViewById(R.id.btnDialogCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });

        lstDialogItems = (LinearLayout) mMenuView.findViewById(R.id.lstDialogItems);

        for (int i = 0; i < list.length; i++) {
            View convertView = mInflater.inflate(R.layout.listitem_dialog, lstDialogItems, false);
            ((TextView) convertView.findViewById(R.id.txtItemLabel)).setText(list[i]);
            if (i == defaultIndex) {
                convertView.findViewById(R.id.imgItemSelected).setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.imgItemSelected).setVisibility(View.GONE);
            }
            convertView.setTag(i);
            convertView.setOnClickListener(itemsOnClick);
            lstDialogItems.addView(convertView, i);
        }

        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
//        //设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.AnimBottom);
//        //实例化一个ColorDrawable颜色为半透明
//        ColorDrawable dw = new ColorDrawable(0x80000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        mMenuView.setBackgroundColor(Color.argb(0x80, 0x00, 0x00, 0x00));
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    public interface IDialogSelectListener {
        public void onSelected(int selected_index);
    }
}
