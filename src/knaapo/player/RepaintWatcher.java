package knaapo.player;

import java.util.ArrayList;


public class RepaintWatcher implements Runnable {

    boolean mIsRun = true;
    ArrayList<ArrayList<Boolean>> mIsItemsBlinked = null;

    RepaintListener mRepaintListener = null;


    public RepaintWatcher(int groupsCount, ArrayList<Integer> itemsCount) {
        mIsItemsBlinked = new ArrayList<ArrayList<Boolean>>();

        for (int i = 0; i < groupsCount; i++) {
            mIsItemsBlinked.add(new ArrayList<Boolean>());
            for (int j = 0; j < itemsCount.get(i); j++) {
                mIsItemsBlinked.get(i).add(false);
            }
        }
    }

    @Override
    public void run() {

        while (mIsRun) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (mRepaintListener == null)
                continue;

            for (int i = 0; i < mIsItemsBlinked.size(); i++) {
                for (int j = 0; j < mIsItemsBlinked.get(i).size(); j++)
                    if (mIsItemsBlinked.get(i).get(j)) {
                        mRepaintListener.repaintNeeded(i, j, true);
                    }
            }
        }

        return;
    }


    public void setRepaintListener(RepaintListener repaintListener) {
        mRepaintListener = repaintListener;
        return;
    }

    public void setIsItemBlinked(int groupID, int itemID, boolean isBlinked) {
        mIsItemsBlinked.get(groupID).set(itemID, isBlinked);
        return;
    }


    public void stop() {
        mIsRun = false;
        return;
    }

}
