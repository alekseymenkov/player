package knaapo.player;

public interface RepaintListener {
    public void repaintNeeded(int groupID, int elementID, boolean flag);

    public void parseAndRepaintNeeded(int groupID, int elementID);

    public void repaintFlagChanged(int groupID, int elementID, boolean flag);
}
