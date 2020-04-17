package tech.berjis.mumtomum;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private List<Transactions> listData;
    private String type;

    public TransactionsAdapter(List<Transactions> listData, String type) {
        this.listData = listData;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transactions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Transactions ld = listData.get(position);

        if (type.equals("complete")) {
            if (ld.getStatus().equals("success")) {
                //holder.narration.setTextColor(Color.parseColor("#007e33"));
                holder.half.setBackgroundColor(Color.parseColor("#007e33"));
                holder.narration.setText(ld.getNarration() + " " + ld.getType() + ": Successful");
            }
            if (ld.getStatus().equals("error")) {
                //holder.narration.setTextColor(Color.parseColor("#cc0000"));
                holder.half.setBackgroundColor(Color.parseColor("#cc0000"));
                holder.narration.setText(ld.getNarration() + " " + ld.getType() + ": Unsuccessful");
            }
            if (ld.getStatus().equals("cancelled")) {
                //holder.narration.setTextColor(Color.parseColor("#ff8800"));
                holder.half.setBackgroundColor(Color.parseColor("#ff8800"));
                holder.narration.setText(ld.getNarration() + " " + ld.getType() + ": Cancelled");
            }
        }

        if (type.equals("wallet")) {
            if (ld.getStatus().equals("success")) {
                //holder.narration.setTextColor(Color.parseColor("#007e33"));
                holder.half.setBackgroundColor(Color.parseColor("#007e33"));
                holder.narration.setText(ld.getType() + ": Successful");
            }
            if (ld.getStatus().equals("error")) {
                //holder.narration.setTextColor(Color.parseColor("#cc0000"));
                holder.half.setBackgroundColor(Color.parseColor("#cc0000"));
                holder.narration.setText(ld.getType() + ": Unsuccessful");
            }
            if (ld.getStatus().equals("cancelled")) {
                //holder.narration.setTextColor(Color.parseColor("#ff8800"));
                holder.half.setBackgroundColor(Color.parseColor("#ff8800"));
                holder.narration.setText(ld.getType() + ": Cancelled");
            }
        }

        Date df = new java.util.Date((ld.getEnd_time() * 1000));
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);

        String year = new SimpleDateFormat("yyyy").format(df);
        String vv = "";

        if (year.equals(String.valueOf(thisYear))) {
            vv = new SimpleDateFormat("MMM dd").format(df) + "\n" + new SimpleDateFormat("H:m:a").format(df);
            ;
        } else {
            vv = new SimpleDateFormat("MMM dd").format(df) + "\n" + new SimpleDateFormat("yyyy").format(df);
        }

        holder.time.setText(vv);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String output = nf.format(ld.getAmount());

        if (ld.getType().equals("deposit")) {
            holder.amount.setText("+kshs " + output);
            holder.amount.setTextColor(Color.parseColor("#0d47a1"));
        }
        if (ld.getType().equals("withdraw")) {
            holder.amount.setText("-kshs " + output);
            holder.amount.setTextColor(Color.parseColor("#cc0000"));
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView narration, time, amount;
        View mView, half;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            narration = itemView.findViewById(R.id.narration);
            time = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.amount);
            half = itemView.findViewById(R.id.half);
            mView = itemView;
        }
    }
}
