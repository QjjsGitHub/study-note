package com.example.study.recyclerView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study.R;
import com.example.study.databinding.ActivityRecycleViewBinding;

import java.util.LinkedList;

public class RecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        ActivityRecycleViewBinding binding = ActivityRecycleViewBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinkedList<String> linkedList = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            linkedList.add("element: " + i);
        }
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(linkedList);

        RecyclerView recyclerView = binding.recyclerView;


        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        recyclerView.setAdapter(recyclerViewAdapter);

        binding.button35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //recyclerView.scrollToPosition(Integer.parseInt(binding.editTextText.getText().toString()));

                recyclerView.smoothScrollToPosition(Integer.parseInt(binding.editTextText.getText().toString()));
            }
        });


        recyclerView.addItemDecoration(new DividerItemDecoration(this));


    }


    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        private Drawable divider;

        public DividerItemDecoration(Context context) {
            /*final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            divider = styledAttributes.getDrawable(1);
            styledAttributes.recycle();*/

            divider = AppCompatResources.getDrawable(context, R.drawable.girl);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + 600;
                //int bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, 600);
        }
    }

}