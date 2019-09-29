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

        Comparator<Sortable> comparator = getSortableComparator();

        if (comparator != null) { // last order is always by name
            comparator = comparator.thenComparing((p1, p2) -> p2.name().compareTo(p1.name()));
        }

        // Original order = from high to low -> reversed compare
        if (originalOrder) comparator = comparator.reversed();
        players.sort(comparator);
    }

    private Comparator<Sortable> getSortableComparator() {

        switch (sort) {

            // Main
            case name:
                return (p1, p2) -> p2.name().compareTo(p1.name());
            case grade:
                return Comparator.comparing(Sortable::grade);
            case suggestedGrade:
                return Comparator.comparing(Sortable::suggestedGrade).
                        thenComparing(Sortable::grade);
            case age:
                return Comparator.comparing(Sortable::age);
            case attributes:
                return Comparator.comparing(Sortable::attributes);
            case coming:
                return Comparator.comparing(Sortable::coming);

                // Statistics
            case success:
                return Comparator.comparing(Sortable::success).
                        thenComparing(Sortable::winRate);
            case winPercentage:
                return Comparator.comparing(Sortable::winRate);
            case games:
                return Comparator.comparing(Sortable::games);

            // Participation
            case gamesWith:
                return Comparator.comparing(Sortable::gamesWithCount).
                        thenComparing(Sortable::successWith).
                        thenComparing(Sortable::winRateWith);
            case successWith:
                return Comparator.comparing(Sortable::successWith).
                        thenComparing(Sortable::winRateWith);
            case gamesVs:
                return Comparator.comparing(Sortable::gamesVsCount).
                        thenComparing(Sortable::successVs).
                        thenComparing(Sortable::winRateVs);
            case successVs:
                return Comparator.comparing(Sortable::successVs).
                        thenComparing(Sortable::winRateVs);
            default:
                return null;
        }
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
