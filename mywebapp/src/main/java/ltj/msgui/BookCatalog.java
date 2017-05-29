package ltj.msgui;

import java.util.Map;

public class BookCatalog {
   private String name;
   private Map<String, Book> map;
   
   // PROPERTY: name
   public void setName(String name) { this.name = name; }
   public String getName() { return name; }

   // PROPERTY: books
   public Map<String, Book> getBooks() { return map; }
   public void setBooks(Map<String, Book> map) { this.map = map; }

   // PROPERTY: book
   public Book getBook(String title) { return map.get(title); }
}
