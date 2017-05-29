package ltj.msgui;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

public class DateBackingBean {
   private int day;
   private int month;
   private int year;
   private UIInput dayInput;
   private UIInput monthInput;
   private UIInput yearInput;

   // PROPERTY: day
   public int getDay() { return day; }
   public void setDay(int newValue) { day = newValue; }

   // PROPERTY: month
   public int getMonth() { return month; }
   public void setMonth(int newValue) { month = newValue; }

   // PROPERTY: year
   public int getYear() { return year; }
   public void setYear(int newValue) { year = newValue; }

   // PROPERTY: dayInput
   public UIInput getDayInput() { return dayInput; }
   public void setDayInput(UIInput newValue) { dayInput = newValue; }
   
   // PROPERTY: monthInput
   public UIInput getMonthInput() { return monthInput; }
   public void setMonthInput(UIInput newValue) { monthInput = newValue; }

   // PROPERTY: yearInput
   public UIInput getYearInput() { return yearInput; }
   public void setYearInput(UIInput newValue) { yearInput = newValue; }

   public void validateDate(FacesContext context, UIComponent component, 
      Object value) {
      int d = ((Integer) dayInput.getLocalValue()).intValue();
      int m = ((Integer) monthInput.getLocalValue()).intValue();
      int y = ((Integer) value).intValue();

      if (!isValidDate(d, m, y)) {
         FacesMessage message 
            = ltj.msgui.util.Messages.getMessage(
               "ltj.msgui.messages", "invalidDate", null);
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         throw new ValidatorException(message);
      }
   }

   private static boolean isValidDate(int d, int m, int y) {
      if (d < 1 || m < 1 || m > 12) return false;
      if (m == 2) {
         if (isLeapYear(y)) return d <= 29;
         else return d <= 28;
      }
      else if (m == 4 || m == 6 || m == 9 || m == 11)
         return d <= 30;
      else 
         return d <= 31;
   }
   
   private static boolean isLeapYear(int y) {
      return y % 4 == 0 && (y % 400 == 0 || y % 100 != 0); 
   }
}
