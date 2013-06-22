package knaapo.player;

import java.util.ArrayList;

public interface TooltipListener {
    public void generateTooltip(int elementID);

    public void displayData(int elementID, ArrayList<String> parametersString);
}
