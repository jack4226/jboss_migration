// <!--
// set cursor position to the beginning of the textarea
function setSelRange(inputEl, selStart, selEnd) { 
	if (inputEl.setSelectionRange) {
		inputEl.focus();
		inputEl.setSelectionRange(selStart, selEnd);
	}
	else if (inputEl.createTextRange) {
		var range = inputEl.createTextRange(); 
		range.collapse(true);
		range.moveEnd('character', selEnd); 
		range.moveStart('character', selStart); 
		range.select();
	}
}
//
function insertIntoBody_DoNotUse() {
  var targetField = document.getElementById('mlstcomp:bodytext');
  var insertField = document.getElementById('mlstcomp:vname');
  var myValue = '\${' + insertField.value + '}';
  insertAtCursor(targetField, myValue);
  //targetField.focus();
}
//
function insertIntoBody(target_field, varbl_field) {
  var targetField = document.getElementById(target_field);
  var insertField = document.getElementById(varbl_field);
  var myValue = '\${' + insertField.value + '}';
  insertAtCursor(targetField, myValue);
  //targetField.focus();
}
// insert text at the cursor position
function insertAtCursor(myField, myValue) {
	// Save scroll position
	var scrollPos = myField.scrollTop;
	//IE support
	if (document.selection) {
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart >= 0) {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos)
			+ myValue
			+ myField.value.substring(endPos, myField.value.length);
		// position the cursor to the end of the insert
		setSelRange(myField, startPos + myValue.length, startPos + myValue.length);
	}
	//UNKNOWN browser 
	else {
		myField.value += myValue;
	}
	// Reset scroll position 
	myField.scrollTop = scrollPos;
}
/*
I was looking for a solution to the problem that in Firefox 0.9, with wordpress 1.2,
the view changes to the top of the post entry field and ended up here.

The cursor does not jump, but the scrollbar at the text entry field goes right to the
top and I have to scroll down to see where I was at.

I'd appreciate a solution.

Answer:
Use save and reset the scrollTop property of the textarea object.
*/

/*
Maybe it's trivial, but I've added a bit to this snippet such that the added text
becomes highlighted / selected once inserted. :)
*/
function insertAtCursor2(myField, myValue) {
	//IE support
	if (document.selection)	{
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
		sel.moveStart('character', -myValue.length);
		sel.select();
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value =
			myField.value.substring(0, startPos)
			+ myValue
			+ myField.value.substring(endPos, myField.value.length);
		myField.selectionStart = startPos;
		myField.selectionEnd = startPos + myValue.length;
		// position the cursor to the end of the insert
		setSelRange(myField, startPos, myField.selectionEnd);
	}
	//Anyone else.
	else {
		myField.value += myValue;
	}
}

// calling the function
//insertAtCursor(document.formName.fieldName, 'this value');

/*
In mozilla, one can use "textarea.selectionStart" to retrieve
the caret position by Javascript. But what is the case of IE?
I've seen a method that sets end point of a dummy range to
document.selection.createRange() object, but it is not working
for me. Another method uses for-loop repeatedly to test the end
point of these two range objects, which seems to be naive. Here
I propose a simple but a-little-dirty method to retrieve caret
position in textarea for IE. Following is the code:
*/
// returns cursor position
function getCursorPosition(node) {
	//node.focus(); 
	/* without node.focus() IE will returns -1 when focus is not on node */
	if(node.selectionStart) // mozilla
		return node.selectionStart;
	else if(!document.selection)
		return 0;
	// IE
	var c = "\001";
	var sel	= document.selection.createRange();
	var dul	= sel.duplicate();
	var len	= 0;
	dul.moveToElementText(node);
	sel.text = c;
	len = (dul.text.indexOf(c));
	sel.moveStart('character',-1);
	sel.text = "";
	return len;
}
/*
to remove the line feed you have to make sure your text area html looks like this:

textarea name="tarea" rows="" cols="" style="width:300px;height:120px;"
               onselect="setCaret(this);"
               onclick="setCaret(this);"
               onkeyup="setCaret(this);"
*/
// Only required for IE
function setCaret(inputEl) {
	if (inputEl.createTextRange) {
		inputEl.caretPos = document.selection.createRange().duplicate();
	}
}
// -->