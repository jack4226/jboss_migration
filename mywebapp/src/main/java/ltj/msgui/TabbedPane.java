package ltj.msgui;

import javax.faces.event.ActionEvent;
 
public class TabbedPane {
   private int index;
   private static final int JEFFERSON_INDEX = 0;
   private static final int ROOSEVELT_INDEX = 1; 
   private static final int LINCOLN_INDEX = 2;
   private static final int WASHINGTON_INDEX = 3;

//   private String[] tabs = { "jeffersonTabText", "rooseveltTabText",
//         "lincolnTabText",  "washingtonTabText", };

   private String[] tabTooltips = { "jeffersonTooltip", "rooseveltTooltip",
         "lincolnTooltip",  "washingtonTooltip" };

   public TabbedPane() {
      index = JEFFERSON_INDEX;
   }

   // action listeners that set the current tab

   public void jeffersonAction(ActionEvent e) { index = JEFFERSON_INDEX;  }
   public void rooseveltAction(ActionEvent e) { index = ROOSEVELT_INDEX;  }
   public void lincolnAction(ActionEvent e) { index = LINCOLN_INDEX;    }
   public void washingtonAction(ActionEvent e) { index = WASHINGTON_INDEX; }

   // CSS styles

   public String getJeffersonStyle() { return getCSS(JEFFERSON_INDEX);  }
   public String getRooseveltStyle() { return getCSS(ROOSEVELT_INDEX);  }
   public String getLincolnStyle() { return getCSS(LINCOLN_INDEX);    }
   public String getWashingtonStyle() { return getCSS(WASHINGTON_INDEX); }

   private String getCSS(int forIndex) {
      return forIndex == index ? "tabbedPaneTextSelected" : "tabbedPaneText"; 
   }

   // methods for determining the current tab

   public boolean isJeffersonCurrent() { return index == JEFFERSON_INDEX;  }
   public boolean isRooseveltCurrent() { return index == ROOSEVELT_INDEX;  }
   public boolean isLincolnCurrent() { return index == LINCOLN_INDEX;    }
   public boolean isWashingtonCurrent() { return index == WASHINGTON_INDEX; }

   // methods that get tool tips for titles

   public String getJeffersonTooltip() { 
      return ltj.msgui.util.Messages.getString(
            "ltj.msgui.messages", tabTooltips[JEFFERSON_INDEX], null); 
   }
   public String getRooseveltTooltip() { 
      return ltj.msgui.util.Messages.getString(
            "ltj.msgui.messages", tabTooltips[ROOSEVELT_INDEX], null); 
   }
   public String getLincolnTooltip() { 
      return ltj.msgui.util.Messages.getString(
            "ltj.msgui.messages", tabTooltips[LINCOLN_INDEX], null); 
   }
   public String getWashingtonTooltip() { 
      return ltj.msgui.util.Messages.getString(
            "ltj.msgui.messages", tabTooltips[WASHINGTON_INDEX], null); 
   }
}
