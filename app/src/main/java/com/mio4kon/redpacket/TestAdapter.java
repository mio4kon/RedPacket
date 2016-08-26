package com.mio4kon.redpacket;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by mio4kon on 16/8/19.
 */
public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

    private ItemClickListener mListener;

    @Override
    public TestAdapter.TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, null);
        return new TestViewHolder(root);
    }

    @Override
    public void onBindViewHolder(TestAdapter.TestViewHolder holder, final int position) {
        //重置被动画的view
        holder.moneyTV.setVisibility(View.VISIBLE);
        holder.scoresTV.setVisibility(View.VISIBLE);
        holder.titleTV.setText(position % 2 == 0 ? "掉落吸金" : "红包冒泡");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }
                mListener.clickItem(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 1000;
    }


    public void setClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public View moneyTV;
        public View scoresTV;
        public TextView titleTV;

        public TestViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            titleTV = (TextView) itemView.findViewById(R.id.tv_title);
            moneyTV = itemView.findViewById(R.id.tv_money);
            scoresTV = itemView.findViewById(R.id.tv_scores);
        }


    }

    interface ItemClickListener {
        void clickItem(View v, int position);
    }


}
