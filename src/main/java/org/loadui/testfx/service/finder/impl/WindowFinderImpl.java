package org.loadui.testfx.service.finder.impl;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import javafx.scene.Scene;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.loadui.testfx.service.finder.WindowFinder;

public class WindowFinderImpl implements WindowFinder {

    //---------------------------------------------------------------------------------------------
    // FIELDS.
    //---------------------------------------------------------------------------------------------

    private Window lastTargetWindow;

    //---------------------------------------------------------------------------------------------
    // METHODS.
    //---------------------------------------------------------------------------------------------

    public Window getLastTargetWindow() {
        return lastTargetWindow;
    }

    public void setLastTargetWindow(Window window) {
        lastTargetWindow = window;
    }

    @SuppressWarnings("deprecation")
    public List<Window> listWindows() {
        List<Window> windows = Lists.newArrayList(Window.impl_getWindows());
        return ImmutableList.copyOf(Lists.reverse(windows));
    }

    public List<Window> listOrderedWindows() {
        List<Window> windows = listWindows();
        List<Window> orderedWindows = orderWindowsByProximityTo(lastTargetWindow, windows);
        return orderedWindows;
    }

    public Window window(int windowNumber) {
        List<Window> windows = listWindows();
        return windows.get(windowNumber);
    }

    public Window window(String stageTitleRegex) {
        List<Window> windows = listWindows();
        return Iterables.find(windows, hasStageTitlePredicate(stageTitleRegex));
    }

    public Window window(Scene scene) {
        return scene.getWindow();
    }

    //---------------------------------------------------------------------------------------------
    // PRIVATE METHODS.
    //---------------------------------------------------------------------------------------------

    private List<Window> orderWindowsByProximityTo(Window targetWindow, List<Window> windows) {
        return Ordering.natural()
            .onResultOf(calculateWindowProximityFunction(targetWindow))
            .immutableSortedCopy(windows);
    }

    private Function<Window, Integer> calculateWindowProximityFunction(final Window targetWindow) {
        return new Function<Window, Integer>() {
            @Override
            public Integer apply(Window window) {
                return calculateWindowProximityTo(targetWindow, window);
            }
        };
    }

    private int calculateWindowProximityTo(Window targetWindow, Window window) {
        if (window == targetWindow) {
            return 0;
        }
        if (this.isOwnerOf(window, targetWindow)) {
            return 1;
        }
        return 2;
    }

    private boolean isOwnerOf(Window window, Window targetWindow) {
        Window ownerWindow = retrieveOwnerOf(window);
        if (ownerWindow == targetWindow) {
            return true;
        }
        return ownerWindow != null && isOwnerOf(ownerWindow, targetWindow);
    }

    private Window retrieveOwnerOf(Window window) {
        if (window instanceof Stage) {
            return ((Stage) window).getOwner();
        }
        if (window instanceof PopupWindow) {
            return ((PopupWindow) window).getOwnerWindow();
        }
        return null;
    }

    private Predicate<Window> hasStageTitlePredicate(final String stageTitleRegex) {
        return new Predicate<Window>() {
            @Override
            public boolean apply(Window window) {
                return window instanceof Stage &&
                    hasStageTitle((Stage) window, stageTitleRegex);
            }
        };
    }

    private boolean hasStageTitle(Stage stage, String stageTitleRegex) {
        return stage.getTitle().matches(stageTitleRegex);
    }

}
