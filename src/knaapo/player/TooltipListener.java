package knaapo.player;

import java.util.ArrayList;

public interface TooltipListener {
    public boolean IsDialogShowing();
    public int getElementID();
    public int getGroupID();

    public void displayData(int elementID, int groupID, ArrayList<String> parametersString);
}
