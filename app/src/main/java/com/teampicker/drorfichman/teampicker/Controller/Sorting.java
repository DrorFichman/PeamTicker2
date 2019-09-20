package com.teampicker.drorfichman.teampicker.Controller;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.teampicker.drorfichman.teampicker.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Sorting {

    public interface sortingCallbacks {
        void refresh();
    }

    public interface Sortable {
        String name();

        int gamesWithCount();

        int successWith();

        int winRateWith();

        int successVs();

        int gamesVsCount();

        int winRateVs();

        int grade();

        int suggestedGrade();

        int age();

        boolean attributes();

        boolean coming();

        int winRate();

        int games();

        int success();
    }

    private sortType sort;
    private boolean originalOrder = true;
    private sortingCallbacks handler;

    public Sorting(sortingCallbacks handle, sortType defaultSort) {
        sort = defaultSort;
        handler = handle;
    }

    public void sort(ArrayList<? extends Sortable> players) {
        // TODO improve sorting with lambda compare and secondary
        Collections.sort(players, new Comparator<Sortable>() {
            @Override
            public int compare(Sortable first, Sortable second) {
                Sortable p1 = first;
                Sortable p2 = second;
                if (!originalOrder) {
                    p1 = second;
                    p2 = first;
                }

                switch (sort) {

                    // Main
                    case name:
                        return byName(p1, p2);
                    case grade:
                        return byGrade(p1, p2);
                    case suggestedGrade:
                        return bySuggestedGrade(p1, p2);
                    case coming:
                        return byComing(p1, p2);
                    case age:
                        return byAge(p1, p2);
                    case attributes:
                        return byAttributes(p1, p2);

                    // Statistics
                    case success:
                        return bySuccess(p1, p2);
                    case winPercentage:
                        return byWinRate(p1, p2);
                    case games:
                        return byGames(p1, p2);

                    // Participation
                    case gamesWith:
                        return byGamesWith(p1, p2);
                    case successWith:
                        return bySuccessWith(p1, p2);
                    case gamesVs:
                        return byGamesVs(p1, p2);
                    case successVs:
                        return bySuccessVs(p1, p2);
                    default:
                        return byName(p1, p2);
                }
            }
        });
    }

    private int byWinRate(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.winRate(), p1.winRate());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int bySuccess(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.success(), p1.success());
        return (i != 0) ? i : byWinRate(p1, p2);
    }

    private int byAttributes(Sortable p1, Sortable p2) {
        int i = Boolean.compare(p2.attributes(), p1.attributes());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int byAge(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.age(), p1.age());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int byComing(Sortable p1, Sortable p2) {
        int i = Boolean.compare(p2.coming(), p1.coming());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int bySuggestedGrade(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.suggestedGrade(), p1.suggestedGrade());
        return (i != 0) ? i : byGrade(p1, p2);
    }

    private int byGrade(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.grade(), p1.grade());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int byGames(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.games(), p1.games());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int byName(Sortable p1, Sortable p2) {
        return p1.name().compareTo(p2.name());
    }

    private int bySuccessVs(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.successVs(), p1.successVs());
        return (i != 0) ? i : byWinRateVs(p1, p2);
    }

    private int byWinRateVs(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.winRateVs(), p1.winRateVs());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int byWinRateWith(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.winRateWith(), p1.winRateWith());
        return (i != 0) ? i : byName(p1, p2);
    }

    private int bySuccessWith(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.successWith(), p1.successWith());
        return (i != 0) ? i : byWinRateWith(p1, p2);
    }

    private int byGamesVs(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.gamesVsCount(), p1.gamesVsCount());
        return (i != 0) ? i : bySuccessVs(p1, p2);
    }

    private int byGamesWith(Sortable p1, Sortable p2) {
        int i = Integer.compare(p2.gamesWithCount(), p1.gamesWithCount());
        return (i != 0) ? i : bySuccessWith(p1, p2);
    }

    private HashMap<Integer, sortType> headlines = new HashMap<Integer, sortType>();
    private Activity activity;

    public void setHeadlineSorting(Activity ctx, int textField, String headline, final sortType sorting) {

        if (headline != null)
            ((TextView) ctx.findViewById(textField)).setText(headline);

        // TODO indicate up/down for sorted fields
        headlines.put(textField, sorting);
        activity = ctx;

        ctx.findViewById(textField).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
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
