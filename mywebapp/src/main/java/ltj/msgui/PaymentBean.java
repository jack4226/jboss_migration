package ltj.msgui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;


public class PaymentBean {
   private double amount;
   private CreditCard card = new CreditCard("");
   private Date date = new Date();

   // PROPERTY: amount
   public void setAmount(double newValue) { amount = newValue; }
   public double getAmount() { return amount; }

   // PROPERTY: card
   public void setCard(CreditCard newValue) { card = newValue; }
   public CreditCard getCard() { return card; }

   // PROPERTY: date
   public void setDate(Date newValue) { date = newValue; }
   public Date getDate() { return date; }
   
   /**
    * a sample implementation of Validating with Bean Methods
    * @param context
    * @param component
    * @param value
    */
   public void checkExpirationDate(FacesContext context, UIComponent component, Object value) {
		if (value == null)
			return;
		Date expr;
		if (value instanceof Date)
			expr = (Date) value;
		else
			expr = getDate(value.toString());
		if (expr==null || expired(expr)) {
			FacesMessage message = ltj.msgui.util.Messages.getMessage(
					"ltj.msgui.messages", "cardExpired", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}

	private static boolean expired(Date date) {
		Calendar rightNow = Calendar.getInstance();
		Calendar expr = Calendar.getInstance();
		expr.setTime(date);
		if (rightNow.get(Calendar.YEAR) < expr.get(Calendar.YEAR)) {
			return false;
		}
		else if (rightNow.get(Calendar.YEAR) == expr.get(Calendar.YEAR)
				&& rightNow.get(Calendar.MONTH) <= expr.get(Calendar.MONTH)) {
			return false;
		}
		else {
			return true;
		}
	}

	private static Date getDate(String s) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Date exprDate = null;
		try {
			exprDate = sdf.parse(s);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return exprDate;
	}
}
