package ltj.msgui.tiles;

import java.util.*;
import javax.faces.model.SelectItem;

import ltj.msgui.Book;

import javax.faces.event.ValueChangeEvent;

public class Library {
	private Map<String, Book> bookMap = new HashMap<String, Book>();
	private Book initialBook = null;
	private List<SelectItem> bookItems = null;
	private String book = null;
	private String selectedBook = null;

	public Library() {
		Book peterpan = new Book();
		Book aliceInWonderland = new Book();

		initialBook = peterpan;

		aliceInWonderland.setDirectory("books/aliceInWonderland");
		aliceInWonderland.setTitleKey("aliceInWonderland");
		aliceInWonderland.setImage("books/aliceInWonderland/cheshire.jpg");
		aliceInWonderland.setNumChapters(12);

		peterpan.setDirectory("books/peterpan");
		peterpan.setTitleKey("peterpan");
		peterpan.setImage("books/peterpan/peterpan.jpg");
		peterpan.setNumChapters(15);

		bookMap.put("aliceInWonderland", aliceInWonderland);
		bookMap.put("peterpan", peterpan);
	}

	public void setBook(String book) {
		this.book = book;
	}

	public String getBook() {
		return book;
	}

	public Map<String, Book> getBooks() {
		return bookMap;
	}

	public void bookSelected(ValueChangeEvent e) {
		selectedBook = (String) e.getNewValue();
	}

	public Book getSelectedBook() {
		return selectedBook != null ? bookMap.get(selectedBook) : initialBook;
	}

	public List<SelectItem> getBookItems() {
		if (bookItems == null) {
			bookItems = new LinkedList<SelectItem>();
			Iterator<Book> it = bookMap.values().iterator();
			while (it.hasNext()) {
				Book book = it.next();
				bookItems.add(new SelectItem(book.getTitleKey(), getBookTitle(book.getTitleKey())));
			}
		}
		return bookItems;
	}

	private String getBookTitle(String key) {
		return ltj.msgui.util.Messages.getString("ltj.msgui.messages", key, null);
	}
}
