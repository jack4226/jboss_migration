package ltj.msgui.tabbedpane;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;

public class UITabbedPane extends UICommand {
   private String content;
   
   public String getContent() { return content; }
   public void setContent(String newValue) { content = newValue; }

   // Comment out these two methods to see what happens 
   // when a component does not properly save its state.
   public Object saveState(FacesContext context) {
      Object values[] = new Object[3];
      values[0] = super.saveState(context);
      values[1] = content;
      return values;
   }

   public void restoreState(FacesContext context, Object state) {
      Object values[] = (Object[]) state;
      super.restoreState(context, values[0]);
      content = (String) values[1];
   }
}
