package tech.berjis.mumtomum;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private List<Transactions> listData;
    private String type, symbol;

    public TransactionsAdapter(List<Transactions> listData, String type, String symbol) {
        this.listData = listData;
        this.type = type;
        this.symbol = symbol;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Transactions ld = listData.get(position);


        long time = ld.getEnd_time() * 1000;
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String ago = prettyTime.format(new Date(time));

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        String output = nf.format(ld.getAmount());

        if (!ld.getStatus().equals("success")) {
            holder.mView.setAlpha(0.5f);
        }

        if (type.equals("group")) {
            if (ld.getStatus().equals("success")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_good);
            }
            if (ld.getStatus().equals("error")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_warning);
            }
            if (ld.getStatus().equals("cancelled")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_bad);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.username.setText(Html.fromHtml(ld.getNarration() + "<br /><small>" + ago + "</small>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.username.setText(Html.fromHtml(ld.getNarration() + "<br /><small>" + ago + "</small>"));
            }
        }

        if (type.equals("wallet")) {
            if (ld.getStatus().equals("success")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_good);
            }
            if (ld.getStatus().equals("error")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_warning);
            }
            if (ld.getStatus().equals("cancelled")) {
                holder.indicator.setBackgroundResource(R.drawable.indicator_bad);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.username.setText(Html.fromHtml(ld.getType() + "<br /><small>" + ago + "</small>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                holder.username.setText(Html.fromHtml(ld.getType() + "<br /><small>" + ago + "</small>"));
            }
        }

        if (ld.getType().equals("deposit")) {
            if (ld.getStatus().equals("success")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.amount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.amount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.amount.setText(Html.fromHtml("<s><small>" + symbol + " " + output + "</small></s>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.amount.setText(Html.fromHtml("<s><small>" + symbol + " " + output + "</small></s>"));
                }
            }
            holder.amount.setTextColor(Color.parseColor("#18a3fe"));
        }
        if (ld.getType().equals("withdraw")) {
            if (ld.getStatus().equals("success")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.amount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.amount.setText(Html.fromHtml("<small>" + symbol + "</small> " + output));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.amount.setText(Html.fromHtml("<s><small>" + symbol + " " + output + "</small></s>", Html.FROM_HTML_MODE_COMPACT));
                } else {
                    holder.amount.setText(Html.fromHtml("<s><small>" + symbol + " " + output + "</small></s>"));
                }
            }
            holder.amount.setTextColor(Color.parseColor("#FE18A3"));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context tContext = holder.mView.getContext();
                Intent tIntent = new Intent(tContext, GroupTransactionActivity.class);
                Bundle tBundle = new Bundle();
                tBundle.putString("t_id", ld.getText_ref());
                tIntent.putExtras(tBundle);
                tContext.startActivity(tIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, amount;
        View mView, indicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            amount = itemView.findViewById(R.id.amount);
            indicator = itemView.findViewById(R.id.indicator);
            mView = itemView;
        }
    }
}
