package com.teampicker.drorfichman.teampicker.Controller;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Sorting {

    private HashMap<Integer, sortType> headlines = new HashMap<>();
    private Activity activity;

    public interface sortingCallbacks {
        void refresh();
    }

    private sortType sort;
    private boolean originalOrder = true;
    private sortingCallbacks handler;

    public Sorting(sortingCallbacks handle, sortType defaultSort) {
        sort = defaultSort;
        handler = handle;
    }

    public void sort(ArrayList<? extends Sortable> players) {

        Comparator<Sortable> comparator = Comparator.comparing(Sortable::name);

        switch (sort) {

            // Main
            case name:
                comparator = Comparator.comparing(Sortable::name);
                break;
            case grade:
                comparator = Comparator.comparing(Sortable::grade);
                break;
            case suggestedGrade:
                comparator = Comparator.comparing(Sortable::suggestedGrade).
                        thenComparing(Sortable::grade);
                break;
            case age:
                comparator = Comparator.comparing(Sortable::age);
                break;
            case attributes:
                comparator = Comparator.comparing(Sortable::attributes);
                break;

            // Statistics
            case success:
                comparator = Comparator.comparing(Sortable::success).
                        thenComparing(Sortable::winRate);
                break;
            case winPercentage:
                comparator = Comparator.comparing(Sortable::winRate);
                break;
            case games:
                comparator = Comparator.comparing(Sortable::games);
                break;

            // Participation
            case gamesWith:
                comparator = Comparator.comparing(Sortable::gamesWithCount).
                        thenComparing(Sortable::successWith).
                        thenComparing(Sortable::winRateWith);
                break;
            case successWith:
                comparator = Comparator.comparing(Sortable::successWith).
                        thenComparing(Sortable::winRateWith);
                break;
            case gamesVs:
                comparator = Comparator.comparing(Sortable::gamesVsCount).
                        thenComparing(Sortable::successVs).
                        thenComparing(Sortable::winRateVs);
                break;
            case successVs:
                comparator = Comparator.comparing(Sortable::successVs).
                        thenComparing(Sortable::winRateVs);
                break;
            default:
        }

        comparator = comparator.thenComparing(Sortable::name);

        // Original order = from high to low -> reversed compare
        if (originalOrder) comparator = comparator.reversed();
        players.sort(comparator);
    }

    public void setHeadlineSorting(Activity ctx, int textField, String headline, final sortType sorting) {

        if (headline != null)
            ((TextView) ctx.findViewById(textField)).setText(headline);

        headlines.put(textField, sorting);
        activity = ctx;

        ctx.findViewById(textField).setOnClickListener(view -> {
            if (sort == sorting) {
                originalOrder = !originalOrder;
                if (originalOrder) setSorted(view.getId());
                else setReverseSorted(view.getId());
            } else {
                originalOrder = true;
                sort = sorting;
                setSorted(view.getId());
            }
            handler.refresh();
        });
    }

    private void setSorted(int sortingView) {
        TextView view = getTextView(sortingView);
        if (view != null) view.setTextAppearance(R.style.greenHeadline);
        resetSorting(sortingView);
    }

    private void setReverseSorted(int reversedSortingView) {
        TextView view = getTextView(reversedSortingView);
        if (view != null) view.setTextAppearance(R.style.redHeadline);
        resetSorting(reversedSortingView);
    }

    private void resetSorting(int exceptView) {
        for (Integer otherHeadlines : headlines.keySet()) {
            if (otherHeadlines != exceptView) {
                TextView view = getTextView(otherHeadlines);
                if (view != null) view.setTextAppearance(R.style.regularHeadline);
            }
        }
    }

    private TextView getTextView(int id) {
        View view = activity.findViewById(id);
        if (view instanceof TextView) return (TextView) view;
        else return null;
    }
}
