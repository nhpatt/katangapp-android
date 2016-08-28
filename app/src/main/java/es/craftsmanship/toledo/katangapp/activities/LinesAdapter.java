package es.craftsmanship.toledo.katangapp.activities;

import es.craftsmanship.toledo.katangapp.models.RouteResult;
import es.craftsmanship.toledo.katangapp.utils.KatangaFont;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.support.v7.widget.RecyclerView;

import android.widget.TextView;

import java.text.NumberFormat;

import java.util.List;
import java.util.Locale;

/**
 * author Cristóbal Hermida
 * author Manuel de la Peña
 */
public class LinesAdapter extends RecyclerView.Adapter<LinesAdapter.LinesHolder> {

    private List<RouteResult> lines;

    public LinesAdapter(List<RouteResult> lines) {
        this.lines = lines;
    }

    @Override
    public LinesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LinesHolder(parent);
    }

    @Override
    public void onBindViewHolder(LinesHolder holder, int position) {
        holder.bind(lines.get(position));
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    static class LinesHolder extends RecyclerView.ViewHolder {

        private final TextView lineText;
        private final ViewGroup parent;
        private final TextView timeText;

        public LinesHolder(ViewGroup parent) {
            super(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false));

            this.parent = parent;

            lineText = (TextView) itemView.findViewById(R.id.line);
            timeText = (TextView) itemView.findViewById(R.id.time);
        }

        public void bind(RouteResult route) {
            lineText.setText(route.getIdl());

            formatTimeTextStyles(timeText, route.getTime());
        }

        private void formatTimeTextStyles(TextView textView, long time) {
            int color = Color.BLACK;
            float textSize = KatangaFont.DEFAULT_FONT_SIZE;

            if (time <= 5) {
                color = Color.parseColor("#FF4B45");
                textSize *= 1.2;
            }
            else if (time < 10) {
                color = Color.parseColor("#FFB300");
                textSize *= 1.1;
            }

            textView.setTextColor(color);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            if (time == 0) {
                CharSequence text = parent.getContext().getText(R.string.proximo);

                textView.setText(String.valueOf(text).toUpperCase());

                return;
            }

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ES"));

            String formattedTime = numberFormat.format(time);

            CharSequence minutesText = parent.getContext().getText(R.string.minutes);

            textView.setText(formattedTime + " " + minutesText);
        }

    }

}
