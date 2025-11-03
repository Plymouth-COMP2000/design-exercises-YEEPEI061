package com.example.softwareengineering;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupHelper {

    public static void showPopup(Context context, int iconRes, int iconColor, String message, Runnable onConfirm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_popup_message, null);

        ImageView icon = view.findViewById(R.id.popupIcon);
        TextView text = view.findViewById(R.id.popupMessage);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);

        icon.setImageResource(iconRes);
        icon.setColorFilter(iconColor);
        text.setText(message);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.rounded_card_bg_with_stroke);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.6f;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(lp);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null)
                new Handler(Looper.getMainLooper()).postDelayed(onConfirm, 150);
        });
    }

}
