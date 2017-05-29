package ltj.msgui.tiles;

import java.util.Map;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.preparer.PreparerException;
import org.apache.tiles.preparer.ViewPreparer;

import ltj.msgui.Book;

public class LibraryTilePreparer implements ViewPreparer {
	public void execute(TilesRequestContext tilesContext, AttributeContext attributeContext)
			throws PreparerException {

		Map<String, String> params = tilesContext.getParam();
		String chapter = params.get("chapter");
		
		Map<String, Object> session = tilesContext.getSessionScope();
		session.put("chapter", chapter == null || "".equals(chapter) ? "chapter1" : chapter);

		Library library = (Library) session.get("library");

		if (library == null) {
			library = new Library();
			session.put("library", library);
		}

		Book selectedBook = library.getSelectedBook();
		if (selectedBook != null) {
			session.put("book", selectedBook);
		}
	}
}
