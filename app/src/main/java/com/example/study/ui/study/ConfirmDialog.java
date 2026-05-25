package com.example.study.ui.study;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.study.R;
import com.example.study.Tools;

import java.lang.ref.WeakReference;

public class ConfirmDialog extends Dialog {
    private WeakReference<Context> context;
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCancel;
    private TextView tvSure;
    private String title, content, cancelString, sureString;

    public interface OnViewClickLiatener {
        void sureClick();

        void cancelClick();
    }

    public OnViewClickLiatener onViewClickLiatener;

    public void setOnViewClickLiatener(OnViewClickLiatener onViewClickLiatener) {
        this.onViewClickLiatener = onViewClickLiatener;
    }

    public ConfirmDialog(WeakReference<Context> context) {
        this(context, R.style.custom_dialog);
    }

    public ConfirmDialog(WeakReference<Context> context, int themeResId) {
        super(context.get(), themeResId);
        this.context = context;
    }

/*    public ConfirmDialog(WeakReference<Context> context, String title, String content, String cancelString, String sureString) {
        super(context.get(), R.style.custom_dialog);
        this.context = context;
        this.title = title;
        this.content = content;
        this.cancelString = cancelString;
        this.sureString = sureString;
    }*/

    public ConfirmDialog(WeakReference<Context> context, String title, String content, String cancelString, String sureString) {
        this(context, R.style.custom_dialog, title, content, cancelString, sureString);
    }

    public ConfirmDialog(WeakReference<Context> context, int themeResId, String title, String content, String cancelString, String sureString) {
        super(context.get(), themeResId);
        this.context = context;
        this.title = title;
        this.content = content;
        this.cancelString = cancelString;
        this.sureString = sureString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) (Tools.getInstance().getScreenWidth((Activity) context.get()) * 0.8f);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(R.color.trans);
        initView();
        setData();
    }

    public void initView() {
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvContent = (TextView) findViewById(R.id.tv_content);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvSure = (TextView) findViewById(R.id.tv_sure);
    }

    public void setData() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        if (!TextUtils.isEmpty(cancelString)) {
            tvCancel.setText(cancelString);
        }
        if (!TextUtils.isEmpty(sureString)) {
            tvSure.setText(sureString);
        }
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (onViewClickLiatener != null) {
                    onViewClickLiatener.cancelClick();
                }
            }
        });
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (onViewClickLiatener != null) {
                    onViewClickLiatener.sureClick();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        if (context == null || ((Activity) context.get()).isDestroyed() || ((Activity) context.get()).isFinishing()) {
            return;
        }
        super.dismiss();

    }

    @Override
    public void show() {
        if (context == null || ((Activity) context.get()).isDestroyed() || ((Activity) context.get()).isFinishing()) {
            return;
        }
        super.show();
    }

}

