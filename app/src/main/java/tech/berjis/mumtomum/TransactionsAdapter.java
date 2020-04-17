package tech.berjis.mumtomum;

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

    public TransactionsAdapter(List<Transactions> listData) {
        this.listData = listData;
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

        if(ld.getStatus().equals("success")){
            holder.narration.setTextColor(R.color.success);
        }
        if(ld.getStatus().equals("error")){
            holder.narration.setTextColor(R.color.error);
        }
        if(ld.getStatus().equals("cancelled")){
            holder.narration.setTextColor(R.color.cancelled);
        }

        holder.narration.setText(ld.getType());

        Date df = new java.util.Date((ld.getEnd_time() * 1000));
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        String year = new SimpleDateFormat("yyyy").format(df);
        String vv = "";

        if (year.equals(String.valueOf(thisYear))) {
            vv = new SimpleDateFormat("MMM dd").format(df);
        } else {
            vv = new SimpleDateFormat("MMM dd, yyyy").format(df);
        }

        holder.time.setText(vv);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String output = nf.format(ld.getAmount());

        if(ld.getType().equals("deposit")){
            holder.amount.setText("+kshs " + output);
            holder.amount.setTextColor(R.color.info);
        }
        if(ld.getType().equals("-deposit")){
            holder.amount.setText("kshs " + output);
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView narration, time, amount;
        View mView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            narration = itemView.findViewById(R.id.narration);
            time = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.amount);
            mView = itemView;
        }
    }
}
